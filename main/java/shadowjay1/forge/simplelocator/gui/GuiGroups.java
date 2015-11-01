package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import shadowjay1.forge.simplelocator.GroupConfiguration;
import shadowjay1.forge.simplelocator.GroupList;
import shadowjay1.forge.simplelocator.LocatorListener;
import shadowjay1.forge.simplelocator.LocatorLocation;
import shadowjay1.forge.simplelocator.MemberList;
import shadowjay1.forge.simplelocator.SimpleLocator;

@SideOnly(Side.CLIENT)
public class GuiGroups extends GuiScreen
{
	private GuiScreen parentScreen;

	private GuiGroupSlot groupSlotContainer;
	private GroupList groupList;

	private int selectedGroup = -1;

	private GuiButton editMembersButton;
	private GuiButton viewLocationsButton;
	private GuiButton editButton;
	private GuiButton deleteButton;

	private int ticksOpened;

	public GuiGroups(GuiScreen par1GuiScreen)
	{
		this.parentScreen = par1GuiScreen;
	}

	public void initGui()
	{
		this.groupList = SimpleLocator.settings.getGroups();

		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		this.groupSlotContainer = new GuiGroupSlot(this);

		this.initGuiControls();
	}

	public void initGuiControls()
	{
		this.buttonList.add(editMembersButton = new GuiButton(13, this.width / 2 - 143, this.height - 52, 142, 20, "Edit members"));
		this.buttonList.add(viewLocationsButton = new GuiButton(15, this.width / 2 + 1, this.height - 52, 142, 20, "View locations"));
		this.buttonList.add(new GuiButton(10, this.width / 2 - 143, this.height - 28, 70, 20, "Add"));
		this.buttonList.add(editButton = new GuiButton(11, this.width / 2 - 71, this.height - 28, 70, 20, "Edit"));
		this.buttonList.add(deleteButton = new GuiButton(12, this.width / 2 + 1, this.height - 28, 70, 20, "Delete"));
		this.buttonList.add(new GuiButton(14, this.width / 2 + 73, this.height - 28, 70, 20, "Cancel"));
		updateButtons();
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.groupSlotContainer.handleMouseInput();
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
				if(this.selectedGroup > -1 && this.selectedGroup < this.groupList.size()) {
					this.groupList.remove(this.selectedGroup);
				}

				this.selectedGroup = -1;
			}
			else if (par1GuiButton.id == 10) {
				this.groupList.add(new GroupConfiguration());
				this.selectedGroup = this.groupList.size() - 1;
			}
			else if (par1GuiButton.id == 11) {
				if(this.selectedGroup > -1 && this.selectedGroup < this.groupList.size()) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiGroupSettings(this, this.groupList.get(this.selectedGroup)));
				}
			}
			else if (par1GuiButton.id == 13) {
				if(this.selectedGroup > -1 && this.selectedGroup < this.groupList.size()) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiMembers(this, new MemberList(this.groupList.get(this.selectedGroup).getUsernames())));
				}
			}
			else if (par1GuiButton.id == 14)
			{
				this.mc.displayGuiScreen(this.parentScreen);
			}
			else if (par1GuiButton.id == 15)
			{
				if(this.selectedGroup > -1 && this.selectedGroup < this.groupList.size()) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiLocationViewer(this, new Callable<HashMap<String, LocatorLocation>>() {
						public HashMap<String, LocatorLocation> call() {
							HashMap<String, LocatorLocation> locations = new HashMap<String, LocatorLocation>();
							for(String username : groupList.get(selectedGroup).getUsernames()) {
								if(LocatorListener.locations.containsKey(username)) {
									locations.put(username, LocatorListener.locations.get(username));
								}
							}
							return locations;
						}
					}));
				}
			}
			else
			{
				this.groupSlotContainer.actionPerformed(par1GuiButton);
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
		int j = this.selectedGroup;

		if (isShiftKeyDown() && par2 == 200)
		{
			if (j > 0 && j < this.groupList.countGroups())
			{
				this.groupList.swapGroups(j, j - 1);
				--this.selectedGroup;

				if (j < this.groupList.countGroups() - 1)
				{
					this.groupSlotContainer.scrollBy(-this.groupSlotContainer.getSlotHeight());
				}
			}
		}
		else if (isShiftKeyDown() && par2 == 208)
		{
			if (j >= 0 & j < this.groupList.countGroups() - 1)
			{
				this.groupList.swapGroups(j, j + 1);
				++this.selectedGroup;

				if (j > 0)
				{
					this.groupSlotContainer.scrollBy(this.groupSlotContainer.getSlotHeight());
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
		 this.groupSlotContainer.drawScreen(par1, par2, par3);
		 this.drawCenteredString(this.fontRendererObj, "Edit groups", this.width / 2, 20, 16777215);
		 super.drawScreen(par1, par2, par3);
	 }

	 public void updateButtons() {
		 boolean flag = this.selectedGroup >= 0 && this.selectedGroup < SimpleLocator.settings.getGroups().size();
		 this.editMembersButton.enabled = flag;
		 this.viewLocationsButton.enabled = flag;
		 this.editButton.enabled = flag;
		 this.deleteButton.enabled = flag;
	 }

	 static GroupList getGroupList(GuiGroups guiGroups)
	 {
		 return guiGroups.groupList;
	 }

	 static int getSelectedGroup(GuiGroups guiGroups)
	 {
		 return guiGroups.selectedGroup;
	 }

	 static int getAndSetSelectedGroup(GuiGroups guiGroups, int par1)
	 {
		 return guiGroups.selectedGroup = par1;
	 }

	 static GuiButton getButtonEditMembers(GuiGroups guiGroups)
	 {
		 return guiGroups.editMembersButton;
	 }
	 
	 static GuiButton getButtonViewLocations(GuiGroups guiGroups)
	 {
		 return guiGroups.viewLocationsButton;
	 }

	 static GuiButton getButtonEdit(GuiGroups guiGroups)
	 {
		 return guiGroups.editButton;
	 }

	 static GuiButton getButtonDelete(GuiGroups guiGroups)
	 {
		 return guiGroups.deleteButton;
	 }

	 static void editGroup(GuiGroups guiGroups, int par1)
	 {
		 if(par1 > -1 && par1 < guiGroups.groupList.size()) {
			 Minecraft.getMinecraft().displayGuiScreen(new GuiGroupSettings(guiGroups, guiGroups.groupList.get(par1)));
		 }
	 }

	 static int getTicksOpened(GuiGroups guiGroups)
	 {
		 return guiGroups.ticksOpened;
	 }
}
