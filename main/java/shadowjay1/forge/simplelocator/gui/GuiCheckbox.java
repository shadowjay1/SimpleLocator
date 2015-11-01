package shadowjay1.forge.simplelocator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiCheckbox extends GuiButton {
	static ResourceLocation locatorResources = new ResourceLocation("simplelocator", "gui.png");
	private boolean checked = false;
	
	static {
		//Minecraft.getMinecraft().getTextureManager().(locatorResources);
	}
	
	public GuiCheckbox(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}
	
	public void setChecked(boolean value) {
		this.checked = value;
	}
	
	public boolean isChecked() {
		return this.checked;
	}
	
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition + this.width - 20 && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.drawTexturedModalRect(this.xPosition + this.width - 20, this.yPosition, 0, 46 + k * 20, 10, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width - 10, this.yPosition, 190, 46 + k * 20, 10, this.height);
            mc.getTextureManager().bindTexture(locatorResources);
            this.drawTexturedModalRect(this.xPosition + this.width - 20, this.yPosition, this.checked ? 0 : 20, 0, 20, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.hovered)
            {
                l = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }
    }
}
