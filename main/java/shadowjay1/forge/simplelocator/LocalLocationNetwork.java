package shadowjay1.forge.simplelocator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocalLocationNetwork extends Thread {
	public LocalLocationNetwork() {

	}

	public void run() {
		try {
			while(true) {
				Socket socket = new Socket(InetAddress.getLocalHost(), 45454);
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				oos.writeObject(LocatorListener.offlineLocations);
				LocatorListener.offlineLocations = (HashMap<String, HashMap<String, LocatorLocation>>) ois.readObject();
				socket.close();
				Thread.sleep(250);
			}
		}
		catch(ConnectException e) {
			new LocalLocationServer();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public class LocalLocationServer {
		private ServerSocket serverSocket;

		public LocalLocationServer() {
			try
			{
				serverSocket = new ServerSocket(45454);

				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							serverSocket.close();
						} 
						catch (IOException e) {
							e.printStackTrace();
						}

						saveLocations();
					}
				});

				(new Thread() {
					public void run() {
						while(true) {
							try {
								Thread.sleep(15000);
							}
							catch (InterruptedException e) {
								e.printStackTrace();
							}

							saveLocations();
						}
					}
				}).start();

				(new Thread() {
					@Override
					public void run() {
						while(true) {
							try {
								Socket s = serverSocket.accept();
								ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
								ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
								HashMap<String, HashMap<String, LocatorLocation>> locations = (HashMap<String, HashMap<String, LocatorLocation>>) ois.readObject();
								LocatorListener.offlineLocations = mergeLocationsMap(LocatorListener.offlineLocations, locations);
								oos.writeObject(LocatorListener.offlineLocations);
								s.close();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private HashMap<String, HashMap<String, LocatorLocation>> mergeLocationsMap(HashMap<String, HashMap<String, LocatorLocation>> one, HashMap<String, HashMap<String, LocatorLocation>> two) {
			HashMap<String, HashMap<String, LocatorLocation>> three = new HashMap<String, HashMap<String, LocatorLocation>>();
			
			for(String ip : one.keySet()) {
				if(two.containsKey(ip)) {
					three.put(ip, mergeLocations(one.get(ip), two.get(ip)));
				}
				else {
					three.put(ip, one.get(ip));
				}
			}
			
			for(String ip : two.keySet()) {
				if(!one.containsKey(ip)) {
					three.put(ip, two.get(ip));
				}
			}
			
			return three;
		}
		
		private HashMap<String, LocatorLocation> mergeLocations(HashMap<String, LocatorLocation> one, HashMap<String, LocatorLocation> two) {
			HashMap<String, LocatorLocation> three = new HashMap<String, LocatorLocation>();
			
			for(String username : one.keySet()) {
				if(!two.containsKey(username)) {
					three.put(username, one.get(username));
				}
				else {
					if(two.get(username).compareTo(one.get(username)) == 1) {
						three.put(username, two.get(username));
					}
					else {
						three.put(username, one.get(username));
					}
				}
			}
			
			for(String username : two.keySet()) {
				if(!one.containsKey(username)) {
					three.put(username, two.get(username));
				}
			}
			
			return three;
		}

		public void saveLocations() {
			Gson gson = new GsonBuilder().create();
			String json;
			synchronized(LocatorListener.offlineLocations) {
				json = gson.toJson(LocatorListener.offlineLocations);
			}
			try {
				FileWriter writer = new FileWriter(SimpleLocator.offlineLocationsFile);
				writer.write(json);
				writer.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
