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

import shadowjay1.forge.simplelocator.GroupConfiguration;

@SideOnly(Side.CLIENT)
class GuiGroupSlot extends GuiSlot
{
    final GuiGroups parentGui;
    private Minecraft minecraft;
    private FontRenderer fontRenderer;

    public GuiGroupSlot(GuiGroups par1GuiMultiplayer)
    {
        super(Minecraft.getMinecraft(), par1GuiMultiplayer.width, par1GuiMultiplayer.height, 32, par1GuiMultiplayer.height - 64, 36);
        this.parentGui = par1GuiMultiplayer;
        this.minecraft = Minecraft.getMinecraft();
        this.fontRenderer = minecraft.fontRendererObj;
    }
    
    protected int getSize()
    {
        return GuiGroups.getGroupList(this.parentGui).countGroups();
    }
    
    @Override
    protected void elementClicked(int par1, boolean par2, int par3, int par4)
    {
        if (par1 < GuiGroups.getGroupList(this.parentGui).countGroups())
        {
            int j = GuiGroups.getSelectedGroup(this.parentGui);
            GuiGroups.getAndSetSelectedGroup(this.parentGui, par1);
            boolean flag1 = GuiGroups.getSelectedGroup(this.parentGui) >= 0 && GuiGroups.getSelectedGroup(this.parentGui) < this.getSize();
            boolean flag2 = GuiGroups.getSelectedGroup(this.parentGui) < GuiGroups.getGroupList(this.parentGui).countGroups();
            GuiGroups.getButtonEditMembers(this.parentGui).enabled = flag1;
            GuiGroups.getButtonViewLocations(this.parentGui).enabled = flag1;
            GuiGroups.getButtonEdit(this.parentGui).enabled = flag2;
            GuiGroups.getButtonDelete(this.parentGui).enabled = flag2;

            if (par2 && flag1)
            {
                GuiGroups.editGroup(this.parentGui, par1);
            }
            else if (flag2 && GuiScreen.isShiftKeyDown() && j >= 0 && j < GuiGroups.getGroupList(this.parentGui).countGroups())
            {
                GuiGroups.getGroupList(this.parentGui).swapGroups(j, GuiGroups.getSelectedGroup(this.parentGui));
            }
        }
    }

    /**
     * returns true if the element passed in is currently selected
     */
    protected boolean isSelected(int par1)
    {
        return par1 == GuiGroups.getSelectedGroup(this.parentGui);
    }

    /**
     * return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * 36;
    }
    
    protected void drawBackground()
    {
        
    }

    protected void drawSlot(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        if (par1 < GuiGroups.getGroupList(this.parentGui).countGroups())
        {
        	GroupConfiguration group = GuiGroups.getGroupList(this.parentGui).get(par1);
            
            String sizeInfo = group.getUsernames().size() + (group.getUsernames().size() != 1 ? " members" : " member");
            this.parentGui.drawString(this.fontRenderer, group.getName(), par2 + 2, par3 + 1, 16777215);
            this.parentGui.drawString(this.fontRenderer, group.getConfigSummary(), par2 + 2, par3 + 12, 8421504);
            this.parentGui.drawString(this.fontRenderer, sizeInfo, par2 + 215 - this.fontRenderer.getStringWidth(sizeInfo), par3 + 12, 8421504);
        }
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
