package shadowjay1.forge.simplelocator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import shadowjay1.forge.simplelocator.LocatorLocation;

@SideOnly(Side.CLIENT)
class GuiLocationSlot extends GuiSlot
{
    final GuiLocationViewer parentGui;
    private Minecraft minecraft;
    private FontRenderer fontRenderer;

    public GuiLocationSlot(GuiLocationViewer par1GuiMultiplayer)
    {
        super(Minecraft.getMinecraft(), par1GuiMultiplayer.width, par1GuiMultiplayer.height, 32, par1GuiMultiplayer.height - 64, 16);
        this.parentGui = par1GuiMultiplayer;
        this.minecraft = Minecraft.getMinecraft();
        this.fontRenderer = minecraft.fontRendererObj;
    }
    
    protected int getSize()
    {
        return GuiLocationViewer.getLocations(this.parentGui).size();
    }
    
    protected void elementClicked(int par1, boolean par2, int par3, int par4)
    {
        if (par1 < GuiLocationViewer.getLocations(this.parentGui).size())
        {
            int j = GuiLocationViewer.getSelectedLocation(this.parentGui);
            GuiLocationViewer.getAndSetSelectedLocation(this.parentGui, par1);
            boolean flag1 = GuiLocationViewer.getSelectedLocation(this.parentGui) >= 0 && GuiLocationViewer.getSelectedLocation(this.parentGui) < this.getSize();
            boolean flag2 = GuiLocationViewer.getSelectedLocation(this.parentGui) < GuiLocationViewer.getLocations(this.parentGui).size();

            if (par2 && flag1)
            {
                //GuiLocationViewer.editGroup(this.parentGui, par1);
            }
            else if (flag2 && GuiScreen.isShiftKeyDown() && j >= 0 && j < GuiLocationViewer.getLocations(this.parentGui).size())
            {
                //GuiLocationViewer.getLocations(this.parentGui).swapLocations(j, GuiLocationViewer.getSelectedLocation(this.parentGui));
            }
        }
    }

    /**
     * returns true if the element passed in is currently selected
     */
    protected boolean isSelected(int par1)
    {
        return par1 == GuiLocationViewer.getSelectedLocation(this.parentGui);
    }

    /**
     * return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight;
    }
    
    protected void drawBackground()
    {
        
    }

    protected void drawSlot(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        if (par1 < GuiLocationViewer.getLocations(this.parentGui).size())
        {
        	String username = GuiLocationViewer.getLocations(this.parentGui).keySet().toArray(new String[0])[par1];
            LocatorLocation location = GuiLocationViewer.getLocations(this.parentGui).get(username);
            String locationDesc;
            if(location != null) {
            	String ageStr = location.getAge() < 5000 ? "now" : Long.toString(location.getAge() / 1000) + "s ago";
        		locationDesc = ageStr + " (" + roundCoord(location.getX()) + ", " + roundCoord(location.getY()) + ", " + roundCoord(location.getZ()) + ", " + location.getWorld() + ")";
            }
            else {
            	locationDesc = "unknown";
            }
        	this.parentGui.drawString(this.fontRenderer, username + " - " + locationDesc + " [" + location.getSourceUser() + "]", par2 + 5, par3 + 2, 0xffffff);
        }
    }
    
    private String roundCoord(double coord) {
    	return Double.toString(Math.round(coord * 10) / 10);
    }
    
    @Override
    public void drawContainerBackground(Tessellator t) {
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
    	GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float height = 32.0F;
        WorldRenderer wr = t.getWorldRenderer();
        wr.startDrawingQuads();
        wr.setColorRGBA(0, 0, 0, 200);
        wr.addVertexWithUV((double)left,  (double)bottom, 0.0D, (double)(left  / height), (double)((bottom + (int)getAmountScrolled()) / height));
        wr.addVertexWithUV((double)right, (double)bottom, 0.0D, (double)(right / height), (double)((bottom + (int)getAmountScrolled()) / height));
        wr.addVertexWithUV((double)right, (double)top,    0.0D, (double)(right / height), (double)((top    + (int)getAmountScrolled()) / height));
        wr.addVertexWithUV((double)left,  (double)top,    0.0D, (double)(left  / height), (double)((top    + (int)getAmountScrolled()) / height));
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
