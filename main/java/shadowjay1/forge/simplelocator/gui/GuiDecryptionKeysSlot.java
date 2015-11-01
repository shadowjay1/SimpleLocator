package shadowjay1.forge.simplelocator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadowjay1.forge.simplelocator.SimpleLocator;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
class GuiDecryptionKeysSlot extends GuiSlot
{
    final GuiDecryptionKeys parentGui;
    private Minecraft minecraft;
    private FontRenderer fontRenderer;

    public GuiDecryptionKeysSlot(GuiDecryptionKeys par1GuiMultiplayer)
    {
        super(Minecraft.getMinecraft(), par1GuiMultiplayer.width, par1GuiMultiplayer.height, 32, par1GuiMultiplayer.height - 64, 16);
        this.parentGui = par1GuiMultiplayer;
        this.minecraft = Minecraft.getMinecraft();
        this.fontRenderer = minecraft.fontRendererObj;
    }
    
    protected int getSize()
    {
        return SimpleLocator.settings.getDecryptionPassphrases().size();
    }
    
    protected void elementClicked(int par1, boolean par2, int par3, int par4)
    {
        if (par1 < this.getSize())
        {
            int j = GuiDecryptionKeys.getSelectedGroup(this.parentGui);
            GuiDecryptionKeys.getAndSetSelectedGroup(this.parentGui, par1);
            boolean flag1 = GuiDecryptionKeys.getSelectedGroup(this.parentGui) >= 0 && GuiDecryptionKeys.getSelectedGroup(this.parentGui) < this.getSize();
            boolean flag2 = GuiDecryptionKeys.getSelectedGroup(this.parentGui) < this.getSize();
            GuiDecryptionKeys.getButtonEdit(this.parentGui).enabled = flag2;
            GuiDecryptionKeys.getButtonDelete(this.parentGui).enabled = flag2;
        }
    }

    /**
     * returns true if the element passed in is currently selected
     */
    protected boolean isSelected(int par1)
    {
        return par1 == GuiDecryptionKeys.getSelectedGroup(this.parentGui);
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
        if (par1 < this.getSize())
        {
        	String username = SimpleLocator.settings.getDecryptionPassphrases().keySet().toArray(new String[0])[par1];
            
        	this.parentGui.drawString(this.fontRenderer, username, par2 + 5, par3 + 2, 0xffffff);
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
