package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import shadowjay1.forge.simplelocator.LocatorSettings;
import shadowjay1.forge.simplelocator.MemberList;
import shadowjay1.forge.simplelocator.SimpleLocator;

public class GuiSetEncryptionKey extends GuiScreen {
	private String screenTitle = "Set encryption key";
	
	private GuiScreen parent;
	private GuiTextField passphrase;
	
	public GuiSetEncryptionKey(GuiScreen parent) {
		this.parent = parent;
	}
	
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		
		passphrase = new GuiTextField(this.width / 2, this.fontRendererObj, this.width / 2 - 100, this.height / 2, 200, 20);
		passphrase.setText(SimpleLocator.settings.getEncryptionPassphrase());
		passphrase.setFocused(true);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 2 + 88, 200, 20, "Done"));
	}
	
	public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
	
	protected void keyTyped(char par1, int par2) {
        if(this.passphrase.isFocused()) {
            this.passphrase.textboxKeyTyped(par1, par2);
        }
        
        if(par2 == Keyboard.KEY_RETURN) {
        	SimpleLocator.settings.setEncryptionPassphrase(passphrase.getText());
        	SimpleLocator.networkThread.setEncryptPassword(passphrase.getText());
        	
        	mc.displayGuiScreen(parent);
        }
    }
	
	public void updateScreen() {
		passphrase.updateCursorCounter();
	}
	
	protected void actionPerformed(GuiButton par1GuiButton)
    {
		LocatorSettings settings = SimpleLocator.settings;
		
        if(par1GuiButton.enabled) {
            if(par1GuiButton.id == 100) {
            	SimpleLocator.settings.setEncryptionPassphrase(passphrase.getText());
            	SimpleLocator.networkThread.setEncryptPassword(passphrase.getText());
            	
            	mc.displayGuiScreen(parent);
            }
            
            SimpleLocator.saveConfiguration();
        }
    }
	
	protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        this.passphrase.mouseClicked(par1, par2, par3);
    }
	
	public void drawScreen(int par1, int par2, float par3)
    {
		this.drawDefaultBackground();
		this.passphrase.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
