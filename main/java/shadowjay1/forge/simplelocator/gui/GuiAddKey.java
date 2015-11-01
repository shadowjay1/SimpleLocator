package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import shadowjay1.forge.simplelocator.LocatorSettings;
import shadowjay1.forge.simplelocator.MemberList;
import shadowjay1.forge.simplelocator.SimpleLocator;

public class GuiAddKey extends GuiScreen {
	private String screenTitle = "Add decryption key";
	
	private GuiScreen parent;
	private GuiTextField username;
	private GuiTextField passphrase;
	private String _username = null;
	private String _passphrase = null;
	
	public GuiAddKey(GuiScreen parent) {
		this.parent = parent;
	}
	
	public GuiAddKey(GuiScreen parent, String username, String passphrase) {
		this.parent = parent;
		this._username = username;
		this._passphrase = passphrase;
	}
	
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		
		username = new GuiTextField(this.width / 2, this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 22, 200, 20);
		passphrase = new GuiTextField(this.width / 2, this.fontRendererObj, this.width / 2 - 100, this.height / 2 + 12, 200, 20);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 2 + 88, 200, 20, "Done"));
		
		if(_username != null && _passphrase != null) {
			username.setText(_username);
			passphrase.setText(_passphrase);
		}
	}
	
	public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
	
	protected void keyTyped(char par1, int par2) {
        if(this.username.isFocused()) {
            this.username.textboxKeyTyped(par1, par2);
        }
        if(this.passphrase.isFocused()) {
        	this.passphrase.textboxKeyTyped(par1, par2);
        }
        
        if(par2 == Keyboard.KEY_TAB) {
        	if(this.username.isFocused()) {
        		this.username.setFocused(false);
        		this.passphrase.setFocused(true);
        	}
        	else {
        		this.username.setFocused(true);
        		this.passphrase.setFocused(false);
        	}
        }
        if(par2 == Keyboard.KEY_RETURN) {
        	if(!username.getText().trim().isEmpty()) {
        		if(this.passphrase.getText().trim().isEmpty()) {
        			SimpleLocator.settings.getDecryptionPassphrases().remove(username.getText().trim());
        			SimpleLocator.networkThread.setDecryptPassword(username.getText().trim(), null);
        		}
        		else {
        			SimpleLocator.settings.getDecryptionPassphrases().put(username.getText().trim(), this.passphrase.getText().trim());
        			SimpleLocator.networkThread.setDecryptPassword(username.getText().trim(), this.passphrase.getText().trim());
        		}
        	}
        	
        	mc.displayGuiScreen(parent);
        }
    }
	
	public void updateScreen() {
		username.updateCursorCounter();
		passphrase.updateCursorCounter();
	}
	
	protected void actionPerformed(GuiButton par1GuiButton)
    {
		LocatorSettings settings = SimpleLocator.settings;
		
        if(par1GuiButton.enabled) {
            if(par1GuiButton.id == 100) {
            	if(!username.getText().trim().isEmpty()) {
            		if(this.passphrase.getText().trim().isEmpty()) {
            			SimpleLocator.settings.getDecryptionPassphrases().remove(username.getText().trim());
            			SimpleLocator.networkThread.setDecryptPassword(username.getText().trim(), null);
            		}
            		else {
            			SimpleLocator.settings.getDecryptionPassphrases().put(username.getText().trim(), this.passphrase.getText().trim());
            			SimpleLocator.networkThread.setDecryptPassword(username.getText().trim(), this.passphrase.getText().trim());
            		}
            	}
            	
            	mc.displayGuiScreen(parent);
            }
            
            SimpleLocator.saveConfiguration();
        }
    }
	
	protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        this.username.mouseClicked(par1, par2, par3);
        this.passphrase.mouseClicked(par1, par2, par3);
    }
	
	public void drawScreen(int par1, int par2, float par3)
    {
		this.drawDefaultBackground();
		this.username.drawTextBox();
		this.passphrase.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        this.drawString(this.fontRendererObj, "Username", this.width / 2 - 100, this.height / 2 - 32, 0xffffffff);
        this.drawString(this.fontRendererObj, "Password", this.width / 2 - 100, this.height / 2 + 2, 0xffffffff);
        super.drawScreen(par1, par2, par3);
    }
}
