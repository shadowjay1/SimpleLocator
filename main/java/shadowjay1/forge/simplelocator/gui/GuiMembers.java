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
public class GuiMembers extends GuiScreen
{
	private GuiScreen parentScreen;

	private GuiMemberSlot memberSlotContainer;

	private int selectedMember = -1;
	
	private GuiButton deleteButton;

	private int ticksOpened;

	private MemberList memberList;

	public GuiMembers(GuiScreen par1GuiScreen, MemberList memberList)
	{
		this.parentScreen = par1GuiScreen;
		this.memberList = memberList;
	}

	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		this.memberSlotContainer = new GuiMemberSlot(this);

		this.initGuiControls();
	}

	public void initGuiControls()
	{
		this.buttonList.add(new GuiButton(10, this.width / 2 - 107, this.height - 40, 70, 20, "Add"));
		this.buttonList.add(deleteButton = new GuiButton(12, this.width / 2 - 35, this.height - 40, 70, 20, "Delete"));
		this.buttonList.add(new GuiButton(14, this.width / 2 + 37, this.height - 40, 70, 20, "Cancel"));
		updateButtons();
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.memberSlotContainer.handleMouseInput();
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
			if (par1GuiButton.id == 12)
			{
				if(this.selectedMember > -1 && this.selectedMember < this.memberList.countGroups()) {
					this.memberList.remove(this.selectedMember);
				}

				this.selectedMember = -1;
			}
			else if (par1GuiButton.id == 10) {
				this.mc.displayGuiScreen(new GuiAddMember(this, this.memberList));
				this.selectedMember = -1;
			}
			else if (par1GuiButton.id == 11) {
				if(this.selectedMember > -1 && this.selectedMember < this.memberList.countGroups()) {
					//Minecraft.getMinecraft().displayGuiScreen(new GuiMemberSettings(this.groupList.get(this.selectedGroup)));
				}
			}
			else if (par1GuiButton.id == 14)
			{
				this.mc.displayGuiScreen(this.parentScreen);
			}
			else
			{
				this.memberSlotContainer.actionPerformed(par1GuiButton);
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
		int j = this.selectedMember;

		if (isShiftKeyDown() && par2 == 200)
		{
			if (j > 0 && j < this.memberList.countGroups())
			{
				this.memberList.swapGroups(j, j - 1);
				--this.selectedMember;

				if (j < this.memberList.countGroups() - 1)
				{
					this.memberSlotContainer.scrollBy(-this.memberSlotContainer.getSlotHeight());
				}
			}
		}
		else if (isShiftKeyDown() && par2 == 208)
		{
			if (j >= 0 & j < this.memberList.countGroups() - 1)
			{
				this.memberList.swapGroups(j, j + 1);
				++this.selectedMember;

				if (j > 0)
				{
					this.memberSlotContainer.scrollBy(this.memberSlotContainer.getSlotHeight());
				}
			}
		}
		else if (par2 != 28 && par2 != 156)
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
		this.memberSlotContainer.drawScreen(par1, par2, par3);
		this.drawCenteredString(this.fontRendererObj, "Edit members", this.width / 2, 20, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	/**
	 * Join server by slot index
	 */
	private void joinServer(int par1)
	{
		if (par1 < this.memberList.countGroups())
		{

		}
		else
		{
			par1 -= this.memberList.countGroups();
		}
	}

	public void updateButtons() {
		boolean flag = this.selectedMember >= 0 && this.selectedMember < SimpleLocator.settings.getGroups().size();
		this.deleteButton.enabled = flag;
	}

	static MemberList getMemberList(GuiMembers GuiMembers)
	{
		return GuiMembers.memberList;
	}

	static int getSelectedGroup(GuiMembers GuiMembers)
	{
		return GuiMembers.selectedMember;
	}

	static int getAndSetSelectedGroup(GuiMembers GuiMembers, int par1)
	{
		return GuiMembers.selectedMember = par1;
	}

	static GuiButton getButtonDelete(GuiMembers GuiMembers)
	{
		return GuiMembers.deleteButton;
	}

	static int getTicksOpened(GuiMembers GuiMembers)
	{
		return GuiMembers.ticksOpened;
	}
}
