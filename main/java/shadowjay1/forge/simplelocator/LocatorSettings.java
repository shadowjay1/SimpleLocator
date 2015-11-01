package shadowjay1.forge.simplelocator;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocatorSettings {
	public static final Color[] defaultColors = new Color[] {
		Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
	};
	private boolean renderEnabled = true;
	private boolean sendOwnEnabled = true;
	private boolean sendOthersEnabled = true;
	private boolean showExactOffline = false;
	private int maxViewDistance = 3000;
	private int expirationTime = 30 * 60 * 1000;
	private float scale = 1.0F;
	private float opacity = 0.5F;
	private GroupList groups = new GroupList();
	private Color defaultColor = null;
	private String encryptionPassphrase = "";
	public LinkedHashMap<String, String> decryptionPassphrases = new LinkedHashMap<String, String>();

	public LocatorSettings() {
		
	}
	
	public boolean isRenderEnabled() {
		return renderEnabled;
	}

	public void setRenderEnabled(boolean value) {
		this.renderEnabled = value;
	}

	public boolean isSendOwnEnabled() {
		return sendOwnEnabled;
	}

	public void setSendOwnEnabled(boolean value) {
		this.sendOwnEnabled = value;
	}

	public boolean isSendOthersEnabled() {
		return sendOthersEnabled;
	}

	public void setSendOthersEnabled(boolean value) {
		this.sendOthersEnabled = value;
	}
	
	public boolean isShowExactOfflineEnabled() {
		return showExactOffline;
	}
	
	public void setShowExactOffline(boolean value) {
		this.showExactOffline = value;
	}
	
	public int getExpirationTime() {
		return expirationTime;
	}
	
	public void setExpirationTime(int value) {
		this.expirationTime = value;
	}
	
	public int getMaxViewDistance() {
		return maxViewDistance;
	}

	public void setMaxViewDistance(int value) {
		this.maxViewDistance = value;
	}
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float value) {
		this.scale = value;
	}
	
	public float getOpacity() {
		return opacity;
	}
	
	public void setOpacity(float value) {
		this.opacity = value;
	}
	
	public GroupList getGroups() {
		if(groups == null) {
			groups = new GroupList();
		}
		
		return groups;
	}
	
	public Color getDefaultColor() {
		return defaultColor;
	}
	
	public void setDefaultColor(Color value) {
		this.defaultColor = value;
	}
	
	public void randomizeDefaultColor() {
		Random random = new Random();
		this.defaultColor = defaultColors[random.nextInt(defaultColors.length)];
	}
	
	public String getEncryptionPassphrase() {
		return encryptionPassphrase;
	}
	
	public void setEncryptionPassphrase(String value) {
		this.encryptionPassphrase = value;
	}
	
	public LinkedHashMap<String, String> getDecryptionPassphrases() {
		return decryptionPassphrases;
	}

	public void save(File file) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		try {
			String json = gson.toJson(this);
			
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static LocatorSettings load(File file) {
		Gson gson = new Gson();
		
		try {
			LocatorSettings settings = gson.fromJson(new FileReader(file), LocatorSettings.class);
			
			return settings;
		}
		catch(Exception e) {
			e.printStackTrace();
			
			return new LocatorSettings();
		}
	}
}
