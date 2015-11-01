package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import shadowjay1.forge.simplelocator.MemberList;
import shadowjay1.forge.simplelocator.SimpleLocator;

@SideOnly(Side.CLIENT)
public class GuiDecryptionKeys extends GuiScreen
{
	private GuiScreen parentScreen;

	private GuiDecryptionKeysSlot decryptionKeysSlotContainer;

	private int selectedUsername = -1;
	
	private GuiButton editButton;
	private GuiButton deleteButton;

	private int ticksOpened;

	public GuiDecryptionKeys(GuiScreen par1GuiScreen)
	{
		this.parentScreen = par1GuiScreen;
	}

	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		this.decryptionKeysSlotContainer = new GuiDecryptionKeysSlot(this);

		this.initGuiControls();
	}

	public void initGuiControls()
	{
		this.buttonList.add(new GuiButton(10, this.width / 2 - 143, this.height - 40, 70, 20, "Add"));
		this.buttonList.add(editButton = new GuiButton(12, this.width / 2 - 71, this.height - 40, 70, 20, "Edit"));
		this.buttonList.add(deleteButton = new GuiButton(13, this.width / 2 + 1, this.height - 40, 70, 20, "Delete"));
		this.buttonList.add(new GuiButton(14, this.width / 2 + 73, this.height - 40, 70, 20, "Cancel"));
		updateButtons();
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.decryptionKeysSlotContainer.handleMouseInput();
    }

	public void updateScreen()
	{
		super.updateScreen();
		++this.ticksOpened;
	}

	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.enabled)
		{
			if (par1GuiButton.id == 13)
			{
				if(this.selectedUsername > -1 && this.selectedUsername < this.decryptionKeysSlotContainer.getSize()) {
					String username = SimpleLocator.settings.decryptionPassphrases.keySet().toArray(new String[0])[this.selectedUsername];
					SimpleLocator.settings.decryptionPassphrases.remove(username);
					SimpleLocator.networkThread.setDecryptPassword(username, null);
				}

				this.selectedUsername = -1;
			}
			else if (par1GuiButton.id == 12) {
				if(this.selectedUsername > -1 && this.selectedUsername < this.decryptionKeysSlotContainer.getSize()) {
					String username = SimpleLocator.settings.decryptionPassphrases.keySet().toArray(new String[0])[this.selectedUsername];
					String passphrase = SimpleLocator.settings.decryptionPassphrases.get(username);
					SimpleLocator.settings.decryptionPassphrases.remove(username);
					SimpleLocator.networkThread.setDecryptPassword(username, null);
					this.mc.displayGuiScreen(new GuiAddKey(this, username, passphrase));
				}
			}
			else if (par1GuiButton.id == 10) {
				this.mc.displayGuiScreen(new GuiAddKey(this));
				this.selectedUsername = -1;
			}
			else if (par1GuiButton.id == 14)
			{
				this.mc.displayGuiScreen(this.parentScreen);
			}
			else
			{
				this.decryptionKeysSlotContainer.actionPerformed(par1GuiButton);
			}

			updateButtons();
			SimpleLocator.saveConfiguration();
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 * @throws IOException 
	 */
	protected void keyTyped(char par1, int par2) throws IOException
	{
		int j = this.selectedUsername;
		
		if (par2 != 28 && par2 != 156)
		{
			super.keyTyped(par1, par2);
		}
		else
		{
			this.actionPerformed((GuiButton)this.buttonList.get(2));
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3)
	{
		this.decryptionKeysSlotContainer.drawScreen(par1, par2, par3);
		this.drawCenteredString(this.fontRendererObj, "Decryption keys", this.width / 2, 20, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	/**
	 * Join server by slot index
	 */
	private void joinServer(int par1)
	{
		if (par1 < this.decryptionKeysSlotContainer.getSize())
		{

		}
		else
		{
			par1 -= this.decryptionKeysSlotContainer.getSize();
		}
	}

	public void updateButtons() {
		boolean flag = this.selectedUsername >= 0 && this.selectedUsername < this.decryptionKeysSlotContainer.getSize();
		this.editButton.enabled = flag;
		this.deleteButton.enabled = flag;
	}

	static int getSelectedGroup(GuiDecryptionKeys gui)
	{
		return gui.selectedUsername;
	}

	static int getAndSetSelectedGroup(GuiDecryptionKeys gui, int par1)
	{
		return gui.selectedUsername = par1;
	}
	
	static GuiButton getButtonEdit(GuiDecryptionKeys gui)
	{
		return gui.editButton;
	}

	static GuiButton getButtonDelete(GuiDecryptionKeys gui)
	{
		return gui.deleteButton;
	}

	static int getTicksOpened(GuiDecryptionKeys gui)
	{
		return gui.ticksOpened;
	}
}
