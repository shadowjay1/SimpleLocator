package shadowjay1.forge.simplelocator.gui;

import java.util.HashMap;
import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import shadowjay1.forge.simplelocator.LocatorListener;
import shadowjay1.forge.simplelocator.LocatorLocation;
import shadowjay1.forge.simplelocator.LocatorSettings;
import shadowjay1.forge.simplelocator.SimpleLocator;

public class GuiLocatorSettings extends GuiScreen {
	private String screenTitle = "Locator settings";
	
	private GuiButton sendOwnButton = null;
	private GuiButton sendOthersButton = null;
	private GuiButton renderButton = null;
	private GuiButton showOfflineButton = null;
	private GuiDistanceSlider renderDistance = null;
	private GuiExpirationSlider expirationTime = null;
	private GuiScaleSlider scale = null;
	private GuiOpacitySlider opacity = null;
	private GuiButton groupsButton = null;
	private GuiColoredButton colorButton = null;
	private GuiButton deleteColorButton = null;
	private GuiButton viewAllLocationsButton = null;
	private GuiButton viewLocalOfflineButton = null;
	private GuiButton setPassphraseButton = null;
	private GuiButton decryptionKeysButton = null;
	
	public void initGui() {
		sendOwnButton = new GuiButton(1, this.width / 2 - 205, this.height / 2 - 60 - 24, 200, 20, "");
		sendOthersButton = new GuiButton(2, this.width / 2 - 205, this.height / 2 - 36 - 24, 200, 20, "");
		renderButton = new GuiButton(3, this.width / 2 - 205, this.height / 2 - 12 - 24, 200, 20, "");
		showOfflineButton = new GuiButton(6, this.width / 2 - 205, this.height / 2 + 12 - 24, 200, 20, "");
		renderDistance = new GuiDistanceSlider(4, this.width / 2 + 5, this.height / 2 - 60 - 24, 15000, true, "Max render distance", 1F);
		expirationTime = new GuiExpirationSlider(7, this.width / 2 + 5, this.height / 2 - 36 - 24, 60, false, "Expiration time", 1F);
		scale = new GuiScaleSlider(8, this.width / 2 + 5, this.height / 2 - 12 - 24, 2, false, "Scale", 1F);
		opacity = new GuiOpacitySlider(11, this.width / 2 + 5, this.height / 2 + 12 - 24, 1F, false, "Opacity", 1F);
		colorButton = new GuiColoredButton(9, this.width / 2 - 205, this.height / 2 + 36 - 24, 200, 20, "Randomize default color");
		deleteColorButton = new GuiButton(10, this.width / 2 + 5, this.height / 2 + 36 - 24, 200, 20, "Delete default color");
		groupsButton = new GuiButton(5, this.width / 2 - 100, this.height / 2 + 84, 200, 20, "Group settings");
		viewAllLocationsButton = new GuiButton(12, this.width / 2 - 205, this.height / 2 + 36, 200, 20, "View all locations");
		viewLocalOfflineButton = new GuiButton(13, this.width / 2 + 5, this.height / 2 + 36, 200, 20, "Local offline locations");
		setPassphraseButton = new GuiButton(14, this.width / 2 - 205, this.height / 2 + 60, 200, 20, "Set encryption key");
		decryptionKeysButton = new GuiButton(15, this.width / 2 + 5, this.height / 2 + 60, 200, 20, "Decryption keys");
		updateButtons();
		
		this.buttonList.add(sendOwnButton);
		this.buttonList.add(sendOthersButton);
		this.buttonList.add(renderButton);
		this.buttonList.add(showOfflineButton);
		this.buttonList.add(renderDistance);
		this.buttonList.add(expirationTime);
		this.buttonList.add(scale);
		this.buttonList.add(opacity);
		this.buttonList.add(groupsButton);
		this.buttonList.add(colorButton);
		this.buttonList.add(deleteColorButton);
		this.buttonList.add(viewAllLocationsButton);
		this.buttonList.add(viewLocalOfflineButton);
		this.buttonList.add(setPassphraseButton);
		this.buttonList.add(decryptionKeysButton);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 2 + 112, 200, 20, "Done"));
	}
	
	protected void actionPerformed(GuiButton par1GuiButton)
    {
		LocatorSettings settings = SimpleLocator.settings;
		
        if(par1GuiButton.enabled)
        {
            if(par1GuiButton.id == 1)
            {
            	settings.setSendOwnEnabled(!settings.isSendOwnEnabled());
                updateButtons();
            }
            
            if(par1GuiButton.id == 2)
            {
            	settings.setSendOthersEnabled(!settings.isSendOthersEnabled());
            	updateButtons();
            }
            
            if(par1GuiButton.id == 3)
            {
            	settings.setRenderEnabled(!settings.isRenderEnabled());
            	updateButtons();
            }
            
            if(par1GuiButton.id == 6)
            {
            	settings.setShowExactOffline(!settings.isShowExactOfflineEnabled());
            	updateButtons();
            }
            
            if(par1GuiButton.id == 5) {
            	Minecraft.getMinecraft().displayGuiScreen(new GuiGroups(null));
            }
            
            if(par1GuiButton.id == 9)
            {
            	settings.randomizeDefaultColor();
            	colorButton.setColor(settings.getDefaultColor());
            }
            
            if(par1GuiButton.id == 10)
            {
            	settings.setDefaultColor(null);
            	colorButton.setColor(settings.getDefaultColor());
            }
            
            if(par1GuiButton.id == 12)
            {
				Minecraft.getMinecraft().displayGuiScreen(new GuiLocationViewer(this, LocatorListener.locations));
            }
            
            if(par1GuiButton.id == 13)
            {
				Minecraft.getMinecraft().displayGuiScreen(new GuiLocationViewer(this, new Callable<HashMap<String, LocatorLocation>>() {
            		public HashMap<String, LocatorLocation> call() {
            			return LocatorListener.offlineLocations.get(mc.getNetHandler().getNetworkManager().getRemoteAddress().toString());
            		}
            	}));
            }
            
            if(par1GuiButton.id == 14) {
            	Minecraft.getMinecraft().displayGuiScreen(new GuiSetEncryptionKey(this));
            }
            
            if(par1GuiButton.id == 15) {
            	Minecraft.getMinecraft().displayGuiScreen(new GuiDecryptionKeys(this));
            }
            
            if(par1GuiButton.id == 100)
            {
            	mc.displayGuiScreen(null);
            }
            
            SimpleLocator.saveConfiguration();
        }
    }
	
	private void updateButtons() {
		LocatorSettings settings = SimpleLocator.settings;
		
		sendOwnButton.displayString = "Transmit own location: " + (settings.isSendOwnEnabled() ? "enabled" : "disabled");
		sendOthersButton.displayString = "Transmit others' locations: " + (settings.isSendOthersEnabled() ? "enabled" : "disabled");
		renderButton.displayString = "Render locations: " + (settings.isRenderEnabled() ? "enabled" : "disabled");
		showOfflineButton.displayString = "Exact offline locations: " + (settings.isShowExactOfflineEnabled() ? "enabled" : "disabled");
	}
	
	public void drawScreen(int par1, int par2, float par3)
    {
		this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
