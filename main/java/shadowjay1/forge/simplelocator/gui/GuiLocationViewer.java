package shadowjay1.forge.simplelocator.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import shadowjay1.forge.simplelocator.LocatorLocation;
import shadowjay1.forge.simplelocator.SimpleLocator;

@SideOnly(Side.CLIENT)
public class GuiLocationViewer extends GuiScreen
{
	private GuiScreen parentScreen;

	private GuiLocationSlot locationSlotContainer;

	private int selectedLocation = -1;

	private int ticksOpened;

	private Callable<HashMap<String, LocatorLocation>> locationsCallable;
	private HashMap<String, LocatorLocation> locations;
	
	public GuiLocationViewer(GuiScreen par1GuiScreen, Callable<HashMap<String, LocatorLocation>> locationsCallable)
	{
		this.parentScreen = par1GuiScreen;
		this.locationsCallable = locationsCallable;
	}
	
	public GuiLocationViewer(GuiScreen par1GuiScreen, HashMap<String, LocatorLocation> locations)
	{
		this.parentScreen = par1GuiScreen;
		this.locations = locations;
	}

	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		this.locationSlotContainer = new GuiLocationSlot(this);

		this.initGuiControls();
	}

	public void initGuiControls()
	{
		//this.buttonList.add(new GuiButton(10, this.width / 2 - 107, this.height - 40, 70, 20, "Add"));
		//this.buttonList.add(deleteButton = new GuiButton(12, this.width / 2 - 35, this.height - 40, 70, 20, "Delete"));
		//this.buttonList.add(new GuiButton(14, this.width / 2 + 37, this.height - 40, 70, 20, "Cancel"));
		updateButtons();
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.locationSlotContainer.handleMouseInput();
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
		/*if (par1GuiButton.enabled)
		{
			if (par1GuiButton.id == 12)
			{
				if(this.selectedLocation > -1 && this.selectedLocation < this.locations.countGroups()) {
					this.locations.remove(this.selectedLocation);
				}

				this.selectedLocation = -1;
			}
			else if (par1GuiButton.id == 10) {
				this.mc.displayGuiScreen(new GuiAddMember(this, this.locations));
				this.selectedLocation = -1;
			}
			else if (par1GuiButton.id == 11) {
				if(this.selectedLocation > -1 && this.selectedLocation < this.locations.countGroups()) {
					//Minecraft.getMinecraft().displayGuiScreen(new GuiMemberSettings(this.groupList.get(this.selectedLocation)));
				}
			}
			else if (par1GuiButton.id == 14)
			{
				this.mc.displayGuiScreen(this.parentScreen);
			}
			else
			{
				this.locationSlotContainer.actionPerformed(par1GuiButton);
			}

			updateButtons();
			SimpleLocator.saveConfiguration();
		}*/
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 * @throws IOException 
	 */
	protected void keyTyped(char par1, int par2) throws IOException
	{
		/*int j = this.selectedLocation;

		if (isShiftKeyDown() && par2 == 200)
		{
			if (j > 0 && j < this.locations.countLocations())
			{
				this.locations.swapLocations(j, j - 1);
				--this.selectedLocation;

				if (j < this.locations.countLocations() - 1)
				{
					this.locationSlotContainer.scrollBy(-this.locationSlotContainer.getSlotHeight());
				}
			}
		}
		else if (isShiftKeyDown() && par2 == 208)
		{
			if (j >= 0 & j < this.locations.countLocations() - 1)
			{
				this.locations.swapLocations(j, j + 1);
				++this.selectedLocation;

				if (j > 0)
				{
					this.locationSlotContainer.scrollBy(this.locationSlotContainer.getSlotHeight());
				}
			}
		}
		else */if (par2 != 28 && par2 != 156)
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
		this.locationSlotContainer.drawScreen(par1, par2, par3);
		this.drawCenteredString(this.fontRendererObj, "View locations", this.width / 2, 20, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	public void updateButtons() {
		boolean flag = this.selectedLocation >= 0 && this.selectedLocation < SimpleLocator.settings.getGroups().size();
	}

	static HashMap<String, LocatorLocation> getLocations(GuiLocationViewer viewer)
	{
		if(viewer.locations != null) return viewer.locations;
		
		try {
			return viewer.locationsCallable.call();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static int getSelectedLocation(GuiLocationViewer viewer)
	{
		return viewer.selectedLocation;
	}

	static int getAndSetSelectedLocation(GuiLocationViewer viewer, int par1)
	{
		return viewer.selectedLocation = par1;
	}

	static int getTicksOpened(GuiLocationViewer viewer)
	{
		return viewer.ticksOpened;
	}
}
