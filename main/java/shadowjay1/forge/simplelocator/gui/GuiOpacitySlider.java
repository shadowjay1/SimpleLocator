package shadowjay1.forge.simplelocator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import shadowjay1.forge.simplelocator.SimpleLocator;

@SideOnly(Side.CLIENT)
public class GuiOpacitySlider extends GuiButton
{
    /** The value of this slider control. */
    public float sliderValue = 1.0F;

    /** Is this slider control being dragged. */
    public boolean dragging;
    
    private float maxOpacity;
    private boolean infinityAllowed;
    
    private String name;

    public GuiOpacitySlider(int par1, int par2, int par3, float maxOpacity, boolean infinityAllowed, String par5Str, float par6)
    {
        super(par1, par2, par3, 150, 20, par5Str);
        this.maxOpacity = maxOpacity;
        this.infinityAllowed = infinityAllowed;
        this.sliderValue = par6;
        this.name = par5Str;
        this.width = 200;
        
        adjustFromOpacity(SimpleLocator.settings.getOpacity());
        updateDisplayString();
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean par1)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (float)(par2 - (this.xPosition + 4)) / (float)(this.width - 8);

                if (this.sliderValue < 0.0F)
                {
                    this.sliderValue = 0.0F;
                }

                if (this.sliderValue > 1.0F)
                {
                    this.sliderValue = 1.0F;
                }

                updateDisplayString();
                SimpleLocator.settings.setOpacity(getOpacity());
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        if (super.mousePressed(par1Minecraft, par2, par3))
        {
            this.sliderValue = (float)(par2 - (this.xPosition + 4)) / (float)(this.width - 8);

            if (this.sliderValue < 0.0F)
            {
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F)
            {
                this.sliderValue = 1.0F;
            }

            updateDisplayString();
            SimpleLocator.settings.setOpacity(getOpacity());
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int par1, int par2)
    {
        this.dragging = false;
        SimpleLocator.saveConfiguration();
    }
    
    private void updateDisplayString() {
    	this.displayString = this.name + ": " + ((this.sliderValue >= 1.0F && infinityAllowed) ? "infinity" : (Math.round(this.maxOpacity * this.sliderValue * 100) + "%"));
    }
    
    private void adjustFromOpacity(float opacity) {
    	this.sliderValue = opacity / this.maxOpacity;

    	if(this.sliderValue > 1.0F)
    		this.sliderValue = 1.0F;
    	else if(this.sliderValue < 0.0F)
    		this.sliderValue = 0.0F;
    }
    
    public float getOpacity() {
    	if(this.infinityAllowed && this.sliderValue >= 1.0F) {
    		return Float.MAX_VALUE;
    	}
    	
    	return Math.round(this.maxOpacity * this.sliderValue * 100F) / 100F;
    }
}
