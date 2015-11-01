package shadowjay1.forge.simplelocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.CryptManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuthenticationThread extends Thread {
	public static AuthenticationThread lastThread = null;
	public static String sessionId = null;
	public static boolean authenticated = false;
	private static boolean firstError = true;
	private SecureRandom random = new SecureRandom();

	public void run() {
		lastThread = this;

		try {
			if(sessionId == null)
				this.getSession();

			if(!authenticated)
				this.authenicate();
		}
		catch (Exception e) {
			if(firstError) {
				firstError = false;

				e.printStackTrace();
			}

			System.out.println("Error while connecting to locatornet: authentication failed");
		}
	}

	public void authenicate() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] sharedSecret = new byte[16];
		random.nextBytes(sharedSecret);

		@SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost handshake = new HttpPost("http://locatornet.aws.af.cm/handshake");

		JsonObject contentObj = new JsonObject();
		contentObj.addProperty("sessionId", sessionId);
		contentObj.addProperty("username", Minecraft.getMinecraft().getSession().getUsername());
		contentObj.addProperty("version", SimpleLocator.version);

		Gson gson = new GsonBuilder().create();
		JsonParser parser = new JsonParser();

		System.out.println(gson.toJson(contentObj));

		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("content", gson.toJson(contentObj)));
		handshake.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		HttpResponse response = httpclient.execute(handshake);
		HttpEntity entity = response.getEntity();

		if(entity != null) {
			InputStream is = entity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {
				StringBuilder content = new StringBuilder();

				String line = null;

				while((line = reader.readLine()) != null) {
					content.append(line);
				}

				JsonObject jsonResponse = (JsonObject) parser.parse(content.toString());

				String serverId = jsonResponse.get("serverId").getAsString();
				System.out.println(jsonResponse.get("publicKey"));
				PublicKey publicKey = CryptManager.decodePublicKey(getAsByteArray((JsonArray) jsonResponse.get("publicKey")));
				byte[] verifyToken = getAsByteArray((JsonArray) jsonResponse.get("verifyToken"));

				System.out.println("handshake:" + jsonResponse);

				requestJoin(serverId, publicKey, verifyToken, sharedSecret);
			}
			finally {
				is.close();
			}
		}
	}

	public void requestJoin(String serverId, PublicKey publicKey, byte[] verifyToken, byte[] sharedSecret) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}

		md.update(serverId.getBytes());
		md.update(sharedSecret);
		md.update(publicKey.getEncoded());

		String digest = mcHexDigest(md);
		
		Gson gson = new GsonBuilder().create();
		JsonParser parser = new JsonParser();

		@SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet handshake = new HttpGet("http://session.minecraft.net/game/joinserver.jsp?user=" + Minecraft.getMinecraft().getSession().getUsername() + "&sessionId=" + Minecraft.getMinecraft().getSession().getSessionID() + "&serverId=" + digest);

		JsonObject contentObj = new JsonObject();
		contentObj.addProperty("user", Minecraft.getMinecraft().getSession().getUsername());
		contentObj.addProperty("sessionId", Minecraft.getMinecraft().getSession().getSessionID());
		contentObj.addProperty("serverId", digest);

		HttpResponse response = httpclient.execute(handshake);
		HttpEntity entity = response.getEntity();

		if(entity != null) {
			InputStream is = entity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {
				StringBuilder content = new StringBuilder();

				String line = null;

				while((line = reader.readLine()) != null) {
					content.append(line);
				}

				System.out.println("session: " + content);
			}
			finally {
				is.close();
			}
		}

		HttpPost keyResponse = new HttpPost("http://locatornet.aws.af.cm/login");

		contentObj = new JsonObject();
		contentObj.addProperty("sessionId", sessionId);
		contentObj.add("sharedSecret", convertToJson(CryptManager.encryptData(publicKey, sharedSecret)));
		contentObj.add("verifyToken", convertToJson(CryptManager.encryptData(publicKey, verifyToken)));

		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("content", gson.toJson(contentObj)));
		keyResponse.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		response = httpclient.execute(keyResponse);
		entity = response.getEntity();

		if(entity != null) {
			InputStream is = entity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {
				StringBuilder content = new StringBuilder();

				String line = null;

				while((line = reader.readLine()) != null) {
					content.append(line);
				}

				authenticated = true;

				System.out.println("login: " + content);
			}
			finally {
				is.close();
			}
		}
	}

	public void getSession() throws IOException {
		@SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet sessionid = new HttpGet("http://locatornet.aws.af.cm/getsessionid");

		HttpResponse response = httpclient.execute(sessionid);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream is = entity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {
				StringBuilder content = new StringBuilder();

				String line = null;

				while((line = reader.readLine()) != null) {
					content.append(line);
				}

				this.sessionId = content.toString();
			}
			finally {
				is.close();
			}
		}
	}
	
	public static byte[] getAsByteArray(JsonArray array) {
		byte[] bArray = new byte[array.size()];
		
		for(int i = 0; i < bArray.length; i++) {
			bArray[i] = array.get(i).getAsByte();
		}
		
		return bArray;
	}

	public static byte[] getAsByteArray(JsonObject object) {
		int length = object.get("length").getAsInt();
		byte[] array = new byte[length];

		for(int i = 0; i < length; i++) {
			array[i] = object.get(Integer.toString(i)).getAsByte();
		}

		return array;
	}

	public static JsonObject convertToJson(byte[] array) {
		JsonObject object = new JsonObject();

		object.addProperty("length", array.length);

		for(int i = 0; i < array.length; i++) {
			object.addProperty(Integer.toString(i), array[i]);
		}

		return object;
	}

	public static String mcHexDigest(MessageDigest md) {
		byte[] array = md.digest();
		boolean negative = array[0] < 0;

		if(negative) performTwosCompliment(array);
		String digest = byteArrayToHex(array);

		System.out.println(Arrays.toString(array));

		digest = digest.replaceAll("^0+", "");
		if (negative) digest = '-' + digest;
		return digest;
	}

	public static byte[] performTwosCompliment(byte[] array) {
		boolean carry = true;
		int i;
		byte newByte, value;

		for (i = array.length - 1; i >= 0; --i) {
			value = array[i];
			newByte = (byte) (~value & 0xff);

			if (carry) {
				carry = newByte == 0xff;
				array[i] = (byte) ((newByte + 1) & 0xff);
			}
			else {
				array[i] = newByte;
			}
		}

		return array;
	}

	public static String byteArrayToHex(byte[] array) {
		StringBuilder sb = new StringBuilder();

		for(byte b : array)
			sb.append(String.format("%02x", b & 0xff));

		return sb.toString();
	}
}
