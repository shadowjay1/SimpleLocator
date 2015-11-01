package shadowjay1.forge.simplelocator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class GroupConfiguration {
	private ArrayList<String> usernames = new ArrayList<String>();
	private int maxViewDistanceOverride = Integer.MAX_VALUE;
	private int expirationTimeOverride = 30 * 60 * 1000;
	private boolean trackOnline = false;
	private Color groupColor = null;
	private String updateURL = "";
	private boolean trusted = false;
	
	public ArrayList<String> getUsernames() {
		if(usernames == null) {
			usernames = new ArrayList<String>();
		}
		
		return usernames;
	}

	public int getMaxViewDistanceOverride() {
		return maxViewDistanceOverride;
	}

	public void setMaxViewDistanceOverride(int value) {
		this.maxViewDistanceOverride = value;
	}
	
	public int getExpirationTimeOverride() {
		return expirationTimeOverride;
	}
	
	public void setExpirationTimeOverride(int value) {
		this.expirationTimeOverride = value;
	}
		
	
	public boolean isTrackingOnline() {
		return trackOnline;
	}
	
	public void setTrackingOnline(boolean value) {
		this.trackOnline = value;
	}

	public Color getColor() {
		return groupColor;
	}

	public void setColor(Color groupColor) {
		this.groupColor = groupColor;
	}
	
	public void randomizeColor() {
		Random random = new Random();
		this.groupColor = LocatorSettings.defaultColors[random.nextInt(LocatorSettings.defaultColors.length)];
	}
	
	public String getUpdateURL() {
		return updateURL;
	}
	
	public void setUpdateURL(String url) {
		this.updateURL = url;
	}
	
	public boolean isTrusted() {
		return trusted;
	}
	
	public void setTrusted(boolean value) {
		this.trusted = value;
	}
	
	public String getName() {
		if(usernames.size() == 0) {
			return "<empty group>";
		}
		
		StringBuilder name = new StringBuilder();
		
		int i = 0;
		
		for(; i < usernames.size() && name.length() < 20; i++) {
			if(i > 0) {
				name.append(", ");
			}
			
			name.append(usernames.get(i));
		}
		
		if(i < usernames.size()) {
			name.append(", ...");
		}
		
		return name.toString();
	}
	
	public String getConfigSummary() {
		String shortenedURL = updateURL.length() > 20 ? updateURL.substring(0, 18) + "..." : updateURL;
		
		return shortenedURL;
	}
}
