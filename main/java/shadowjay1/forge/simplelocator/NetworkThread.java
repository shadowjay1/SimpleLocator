package shadowjay1.forge.simplelocator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import shadowjay1.forge.simplelocator.LocatorPacket.Action;

public class NetworkThread extends Thread {
	private static boolean running = true;
	private static byte[] salt = new byte[]{58,123,-49,20,33,-120,7,100};
	private static byte[] iv = new byte[]{0,8,-113,37,39,33,-44,-121,12,-63,71,-101,23,-81,28,-73};
	private long lastStart = 0;
	
	private Cipher encryptCipher = null;
	private HashMap<String, Cipher> decryptCiphers = new HashMap<String, Cipher>();
	
	private int lastAuthenticationAttempt = 0;
	
	public void run() {
		this.setEncryptPassword(SimpleLocator.settings.getEncryptionPassphrase());
		for(String username : SimpleLocator.settings.decryptionPassphrases.keySet()) {
			this.setDecryptPassword(username, SimpleLocator.settings.decryptionPassphrases.get(username));
		}
		
		while(running) {
			sendPacket();
		}
	}
	
	public void sendPacket() {
		try {
			long sleepAmount = 2000 - (System.currentTimeMillis() - lastStart);
			
			if(sleepAmount > 0) {
				Thread.sleep(sleepAmount);
			}
			
			lastStart = System.currentTimeMillis();
			
			if(AuthenticationThread.sessionId == null || AuthenticationThread.authenticated == false) {
				if(AuthenticationThread.lastThread != null && AuthenticationThread.lastThread.isAlive()) {
					return;
				}
				
				if(lastAuthenticationAttempt <= 0) {
					lastAuthenticationAttempt = 30;
					
					AuthenticationThread thread = new AuthenticationThread();
					thread.start();
				}
				else {
					lastAuthenticationAttempt--;
				}
				
				return;
			}

			Gson gson = new GsonBuilder().create();

			Minecraft mc = Minecraft.getMinecraft();

			NetHandlerPlayClient nch = mc.getNetHandler();

			if(nch == null) return;
			if(nch.getNetworkManager() == null) return;

			SocketAddress address = nch.getNetworkManager().getRemoteAddress();

			if(!(address instanceof InetSocketAddress)) return;

			InetSocketAddress inetaddress = (InetSocketAddress) address;
			
			HashMap<String, LocatorLocation> locationsCopy = new HashMap<String, LocatorLocation>();
			
			synchronized(LocatorListener.locations) {
				locationsCopy.putAll(LocatorListener.locations);
			}

			HashMap<String, LocatorLocation> activeLocations = new HashMap<String, LocatorLocation>();
			HashMap<String, LocatorLocation> nLocations = new HashMap<String, LocatorLocation>();
			if(SimpleLocator.settings.isSendOthersEnabled()) {
				for(Object o : mc.theWorld.playerEntities) {
					if(o instanceof EntityOtherPlayerMP) {
						EntityOtherPlayerMP player = (EntityOtherPlayerMP) o;
						
						activeLocations.put(SimpleLocator.filterChatColors(player.getName()), encrypt(new LocatorLocation(player)));
					}
				}
				
				for(String username : locationsCopy.keySet()) {
					if(activeLocations.containsKey(username))
						continue;
					
					LocatorLocation location = locationsCopy.get(username);
					
					if(location.getType() == LocationType.EXACT) {
						nLocations.put(username, encrypt(location));
					}
				}
			}
			
			ArrayList<String> trustedUsers = new ArrayList<String>();
			for(GroupConfiguration group : SimpleLocator.settings.getGroups()) {
				if(group.isTrusted()) {
					trustedUsers.addAll(group.getUsernames());
				}
			}
			
			LocatorPacket sendPacket = new LocatorPacket(AuthenticationThread.sessionId, inetaddress.getAddress().getHostAddress(), inetaddress.getPort(), activeLocations, nLocations, trustedUsers);
			
			if(SimpleLocator.settings.isSendOwnEnabled())
				sendPacket.setOwnLocation(encrypt(new LocatorLocation(mc.thePlayer).setType(encryptCipher == null ? LocationType.EXACT : LocationType.DOWNLOADED)));
			
			@SuppressWarnings("resource")
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://locatornet.aws.af.cm/data");

			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("content", gson.toJson(sendPacket)));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream is = entity.getContent();

				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				StringBuilder content = new StringBuilder();
				
				try {
					String line = null;

					while((line = reader.readLine()) != null) {
						content.append(line);
					}
					
					String contentString = content.toString();
					
					if(contentString.equals("invalid session")) {
						AuthenticationThread.sessionId = null;
						AuthenticationThread.authenticated = false;
						
						return;
					}
					
					if(contentString.equals("not authenticated")) {
						AuthenticationThread.authenticated = false;
						
						return;
					}
					
					LocatorPacket receivedPacket = gson.fromJson(contentString, LocatorPacket.class);

					if(receivedPacket.getAction() == Action.RESPONSE) {
						Map<String, LocatorLocation> locations = receivedPacket.getPlayerLocations();
						
						long timeOffset = System.currentTimeMillis() - receivedPacket.getLocalTime();
						
						synchronized(LocatorListener.locations) {
							for(String key : locations.keySet()) {
								LocatorLocation newLocation = decrypt(locations.get(key));
								
								newLocation.setCreationTime(newLocation.getCreationTime() + timeOffset);
								
								if(LocatorListener.locations.containsKey(key)) {
									LocatorLocation location = LocatorListener.locations.get(key);
									
									if(newLocation.getCreationTime() > location.getCreationTime()) {
										if(newLocation.getType() != LocationType.DOWNLOADED) {
											newLocation = newLocation.setType(LocationType.DOWNLOADED_RADAR);
										}
										
										LocatorListener.locations.put(key, newLocation);
									}
								}
								else {
									if(newLocation.getType() != LocationType.DOWNLOADED) {
										newLocation = newLocation.setType(LocationType.DOWNLOADED_RADAR);
									}

									LocatorListener.locations.put(key, newLocation);
								}
								
								//System.out.println(key + ": " + newLocation.getX() + ", " + newLocation.getY() + ", " + newLocation.getZ());
							}
						}
					}
				}
				catch(Exception e) {
					System.out.println(content.toString());
					e.printStackTrace();
				}
				finally {
					is.close();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public LocatorLocation encrypt(LocatorLocation location) throws GeneralSecurityException {
		if(encryptCipher == null) {
			return location;
		}
		
		byte[] worldName = location.getWorld().getBytes();
		
		ByteBuffer buffer = ByteBuffer.allocate(25 + worldName.length);
		buffer.putDouble(location.getX());
		buffer.putDouble(location.getY());
		buffer.putDouble(location.getZ());
		buffer.put(location.getType().getIndex());
		buffer.put(worldName);
		
		return new LocatorLocation(location.getCreationTime(), location.getSourceUser(), encryptCipher.doFinal(buffer.array()));
	}
	
	public LocatorLocation decrypt(LocatorLocation location) throws GeneralSecurityException {
		if(location.getEncryptedData() == null) {
			return location;
		}
		
		String username = location.getSourceUser();
		
		if(username == null) {
			return null;
		}
		
		if(!decryptCiphers.containsKey(username)) {
			return null;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(decryptCiphers.get(username).doFinal(location.getEncryptedData()));
		
		if(buffer.capacity() < 25) {
			return null;
		}
		
		double x = buffer.getDouble();
		double y = buffer.getDouble();
		double z = buffer.getDouble();
		LocationType type = LocationType.getByIndex(buffer.get());
		if(type == null) return null;
		byte[] worldBytes = new byte[buffer.remaining()];
		buffer.get(worldBytes, 0, worldBytes.length);
		String worldName = new String(worldBytes);
		
		return new LocatorLocation(x, y, z, type, location.getCreationTime(), worldName, location.getSourceUser());
	}
	
	public void setDecryptPassword(String username, String password) {
		if(password == null || password.isEmpty()) {
			if(decryptCiphers.containsKey(username))
				decryptCiphers.remove(username);
			return;
		}
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
			
			decryptCiphers.put(username, cipher);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setEncryptPassword(String password) {
		if(password == null || password.isEmpty()) {
			encryptCipher = null;
			return;
		}
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
			encryptCipher = cipher;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
