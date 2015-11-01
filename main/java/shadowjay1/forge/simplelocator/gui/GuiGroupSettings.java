package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import shadowjay1.forge.simplelocator.GroupConfiguration;
import shadowjay1.forge.simplelocator.GroupUpdateThread;
import shadowjay1.forge.simplelocator.LocatorSettings;
import shadowjay1.forge.simplelocator.SimpleLocator;

public class GuiGroupSettings extends GuiScreen {
	private String screenTitle = "Group settings";
	
	private GuiScreen parent;
	
	private GroupConfiguration group;
	
	private GuiButton trackOnlineButton = null;
	private GuiDistanceOverrideSlider renderDistance = null;
	private GuiExpirationOverrideSlider expirationTime = null;
	private GuiColoredButton groupColorButton = null;
	private GuiButton deleteColorButton = null;
	private GuiTextField updateURLField = null;
	private GuiButton updateNowButton = null;
	private GuiCheckbox trustCheckbox = null;
	
	public GuiGroupSettings(GuiScreen parent, GroupConfiguration group) {
		this.parent = parent;
		this.group = group;
	}
	
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		
		trackOnlineButton = new GuiButton(1, this.width / 2 - 100, this.height / 2 - 96, 200, 20, "");
		renderDistance = new GuiDistanceOverrideSlider(group, 2, this.width / 2 - 100, this.height / 2 - 72, 15000, true, "Max render distance", 1F);
		expirationTime = new GuiExpirationOverrideSlider(group, 6, this.width / 2 - 100, this.height / 2 - 48, 60, false, "Expiration time", 1F);
		groupColorButton = new GuiColoredButton(3, this.width / 2 - 100, this.height / 2 - 24, 99, 20, "Randomize color");
		deleteColorButton = new GuiButton(4, this.width / 2 + 1, this.height / 2 - 24, 99, 20, "Delete color");
		updateURLField = new GuiTextField(this.width / 2, this.fontRendererObj, this.width / 2 - 100, this.height / 2 + 48, 128, 20);
		updateURLField.setMaxStringLength(200);
		String url = group.getUpdateURL();
		if(url != null)
			updateURLField.setText(url);
		updateNowButton = new GuiButton(5, this.width / 2 + 30, this.height / 2 + 48, 70, 20, "Update now");
		trustCheckbox = new GuiCheckbox(6, this.width / 2 - 100, this.height / 2, 200, 20, "Trust group:");
		trustCheckbox.setChecked(group.isTrusted());
		
		groupColorButton.setColor(group.getColor());
		
		updateButtons();
		
		this.buttonList.add(trackOnlineButton);
		this.buttonList.add(renderDistance);
		this.buttonList.add(expirationTime);
		this.buttonList.add(groupColorButton);
		this.buttonList.add(deleteColorButton);
		this.buttonList.add(updateNowButton);
		this.buttonList.add(trustCheckbox);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 2 + 88, 200, 20, "Done"));
	}
	
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
    }
	
	protected void keyTyped(char par1, int par2) {
        if(this.updateURLField.isFocused()) {
            this.updateURLField.textboxKeyTyped(par1, par2);
            
            group.setUpdateURL(updateURLField.getText());
        }
    }
	
	public void updateScreen() {
		updateURLField.updateCursorCounter();
	}
	
	protected void actionPerformed(GuiButton par1GuiButton)
    {
		LocatorSettings settings = SimpleLocator.settings;
		
        if(par1GuiButton.enabled)
        {
            if(par1GuiButton.id == 1)
            {
            	group.setTrackingOnline(!group.isTrackingOnline());
                updateButtons();
            }
            
            if(par1GuiButton.id == 3)
            {
            	group.randomizeColor();
            	groupColorButton.setColor(group.getColor());
            }
            
            if(par1GuiButton.id == 4)
            {
            	group.setColor(null);
            	groupColorButton.setColor(group.getColor());
            }
            
            if(par1GuiButton.id == 5)
            {
            	new GroupUpdateThread(group).start();
            }
            
            if(par1GuiButton.id == 6)
            {
            	group.setTrusted(!group.isTrusted());
            	trustCheckbox.setChecked(group.isTrusted());
            }
            
            if(par1GuiButton.id == 100)
            {
            	mc.displayGuiScreen(parent);
            }
            
            SimpleLocator.saveConfiguration();
        }
    }
	
	protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        this.updateURLField.mouseClicked(par1, par2, par3);
    }
	
	private void updateButtons() {
		LocatorSettings settings = SimpleLocator.settings;
		
		trackOnlineButton.displayString = "Track log-ins/log-outs: " + (group.isTrackingOnline() ? "enabled" : "disabled");
	}
	
	public void drawScreen(int par1, int par2, float par3)
    {
		this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(par1, par2, par3);
        String s = "Auto-update URL";
        fontRendererObj.drawString(s, (this.width / 2) - (fontRendererObj.getStringWidth(s) / 2), this.height / 2 + 7 + 24, 0xffffffff);
        updateURLField.drawTextBox();
    }
}
