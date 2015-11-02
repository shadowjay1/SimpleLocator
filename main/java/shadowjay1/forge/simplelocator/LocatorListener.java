package shadowjay1.forge.simplelocator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import shadowjay1.forge.simplelocator.gui.GuiLocatorSettings;

public class LocatorListener extends Gui {
	private static Pattern snitch = Pattern.compile("^ \\* ([a-zA-Z0-9_]+) (?:entered|logged out in|logged in to) snitch at \\S* \\[(\\S+) ([-]?[0-9]+) ([-]?[0-9]+) ([-]?[0-9]+)\\]");
	private static Pattern broadcast = Pattern.compile("^(?:Your|(?:[a-zA-Z0-9_]+)'s) prison pearl is held by ([a-zA-Z0-9_]+) at (\\S+) ([-]?[0-9]+) ([-]?[0-9]+) ([-]?[0-9]+)");

	private static Minecraft mc;

	public static HashMap<String, LocatorLocation> locations = new HashMap<String, LocatorLocation>();
	public static HashMap<String, HashMap<String, LocatorLocation>> offlineLocations = new HashMap<String, HashMap<String, LocatorLocation>>();

	public LocatorListener() {
		this.mc = Minecraft.getMinecraft();
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(event.phase == Phase.START) {
			if(mc.theWorld == null) {
				synchronized(LocatorListener.locations) {
					if(!LocatorListener.locations.isEmpty()) {
						LocatorListener.locations.clear();
					}
				}

				SimpleLocator.previousPlayerList = new ArrayList<String>();

				return;
			}

			if(SimpleLocator.binding.isPressed()) {
				if(mc.inGameHasFocus)
					mc.displayGuiScreen(new GuiLocatorSettings());
			}

			for(Object o : mc.theWorld.playerEntities) {
				if(o instanceof EntityOtherPlayerMP) {
					EntityOtherPlayerMP player = (EntityOtherPlayerMP) o;

					synchronized(LocatorListener.locations) {
						LocatorListener.locations.put(SimpleLocator.filterChatColors(player.getName()), new LocatorLocation(player));
					}
				}
			}

			ArrayList<String> playerList = new ArrayList<String>();
			Collection players = mc.thePlayer.sendQueue.func_175106_d();

			for(Object o : players) {
				if(o instanceof NetworkPlayerInfo) {
					NetworkPlayerInfo info = (NetworkPlayerInfo) o;

					playerList.add(SimpleLocator.filterChatColors(info.getGameProfile().getName()));
				}
			}

			ArrayList<String> temp = (ArrayList<String>) playerList.clone();
			playerList.removeAll(SimpleLocator.previousPlayerList);
			SimpleLocator.previousPlayerList.removeAll(temp);

			for(String player : SimpleLocator.previousPlayerList) {
				SimpleLocator.onPlayerLeave(player);
			}

			for(String player : playerList) {
				SimpleLocator.onPlayerJoin(player);
			}

			SimpleLocator.previousPlayerList = temp;
			
			synchronized(offlineLocations) {
				if(!offlineLocations.containsKey(mc.getNetHandler().getNetworkManager().getRemoteAddress().toString())) {
					offlineLocations.put(mc.getNetHandler().getNetworkManager().getRemoteAddress().toString(), new HashMap<String, LocatorLocation>());
				}
				
				offlineLocations.get(mc.getNetHandler().getNetworkManager().getRemoteAddress().toString()).put(mc.thePlayer.getName(), new LocatorLocation(mc.thePlayer));
			}
		}
	}

	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		Matcher snitchMatcher = snitch.matcher(event.message.getFormattedText());
		System.out.println(event.message.getFormattedText());
		if(snitchMatcher.find()) {
			try {
				String username = snitchMatcher.group(1);
				String world = snitchMatcher.group(2);
				int x = Integer.parseInt(snitchMatcher.group(3));
				int y = Integer.parseInt(snitchMatcher.group(4));
				int z = Integer.parseInt(snitchMatcher.group(5));

				synchronized(locations) {
					LocatorLocation newLocation = new LocatorLocation(x, y, z, LocationType.SNITCH, world);
					LocatorLocation location = locations.get(username);

					boolean use = true;

					if(location != null) {
						if(newLocation.compareTo(location) <= 0) {
							use = false;
						}
					}

					if(use)
						locations.put(username, newLocation);
				}

				return;
			}
			catch(Exception e) {}
		}

		Matcher broadcastMatcher = broadcast.matcher(event.message.getFormattedText());

		if(broadcastMatcher.find()) {
			try {
				String username = broadcastMatcher.group(1);
				String world = snitchMatcher.group(2);
				int x = Integer.parseInt(broadcastMatcher.group(3));
				int y = Integer.parseInt(broadcastMatcher.group(4));
				int z = Integer.parseInt(broadcastMatcher.group(5));

				synchronized(locations) {
					LocatorLocation newLocation = new LocatorLocation(x, y, z, LocationType.PPBROADCAST, world);
					LocatorLocation location = locations.get(username);

					boolean use = true;

					if(location != null) {
						if(newLocation.compareTo(location) <= 0) {
							use = false;
						}
					}

					if(use)
						locations.put(username, newLocation);
				}

				return;
			}
			catch(Exception e) {}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		LocatorSettings settings = SimpleLocator.settings;

		if(settings.isRenderEnabled()) {
			float opacity = settings.getOpacity();

			float guiScale = settings.getScale();

			int maxDistance = settings.getMaxViewDistance();

			HashMap<String, LocatorLocation> locationsClone;

			synchronized(locations) {
				locationsClone = (HashMap<String, LocatorLocation>) locations.clone();
			}

			List<LocationGroup> groupsClone = new ArrayList<LocationGroup>();

			synchronized(GroupingThread.groups) {
				groupsClone.addAll(GroupingThread.groups);
			}

			for(LocationGroup group : groupsClone) {
				List<String> message = new ArrayList<String>();

				Iterator<String> usernameIterator = group.iterator();

				while(usernameIterator.hasNext()) {
					String username = usernameIterator.next();

					LocatorLocation location = locationsClone.remove(username);

					if(location != null) {
						GroupConfiguration groupConfiguration = settings.getGroups().getByUsername(username);
						int maxDistUser = groupConfiguration != null ? groupConfiguration.getMaxViewDistanceOverride() : maxDistance;
						int maxAge = groupConfiguration != null ? groupConfiguration.getExpirationTimeOverride() : SimpleLocator.settings.getExpirationTime();

						if(location.getAge() > maxAge) {
							synchronized(locations) {
								locations.remove(username);
							}

							continue;
						}
						
						if(!LocatorLocation.dimensionToWorld(mc.thePlayer.dimension).equals(location.getWorld())) {
							continue;
						}

						if(isVisible(username)) {
							continue;
						}

						if(!settings.isShowExactOfflineEnabled() && location.getType().isExact() && !isOnline(username)) {
							continue;
						}

						double xLength = location.getX() - mc.thePlayer.posX;
						double yLength = location.getY() - mc.thePlayer.posY;
						double zLength = location.getZ() - mc.thePlayer.posZ;

						double distance = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);

						if(distance > maxDistUser) {
							continue;
						}

						boolean showTime = !location.getType().isRemote();

						message.add(username + " (" + Math.round(distance) + "m)" + (showTime ? " (" + formatTime(location.getAge()) + ")" : "") + " " + location.getType().getIndicator());
					}
				}

				double xLength = group.getX() - mc.thePlayer.posX;
				double yLength = group.getY() - mc.thePlayer.posY;
				double zLength = group.getZ() - mc.thePlayer.posZ;

				double distance = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);

				double scale = 50D;

				xLength /= distance;
				yLength /= distance;
				zLength /= distance;

				xLength *= scale;
				yLength *= scale;
				zLength *= scale;

				renderText(message.toArray(new String[0]), (float) (mc.thePlayer.posX + xLength), (float) (mc.thePlayer.posY + yLength), (float) (mc.thePlayer.posZ + zLength), 0xffffffff, true, 0, guiScale, opacity);
			}

			Iterator<String> names = locationsClone.keySet().iterator();

			while(names.hasNext()) {
				String name = names.next();

				if(!isVisible(name)) {
					LocatorLocation loc = locationsClone.get(name);

					if(!renderLocation(name, loc))
						continue;

					double xLength = loc.getX() - mc.thePlayer.posX;
					double yLength = loc.getY() - mc.thePlayer.posY;
					double zLength = loc.getZ() - mc.thePlayer.posZ;

					double distance = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);

					boolean showTime = !loc.getType().isRemote();

					double scale = 50D;

					xLength /= distance;
					yLength /= distance;
					zLength /= distance;

					xLength *= scale;
					yLength *= scale;
					zLength *= scale;

					renderText(new String[] {name, "(" + Math.round(distance) + "m)" + (showTime ? " (" + formatTime(loc.getAge()) + ")" : ""), loc.getType().getIndicator()}, (float) (mc.thePlayer.posX + xLength), (float) (mc.thePlayer.posY + yLength), (float) (mc.thePlayer.posZ + zLength), 0xffffffff, true, 0, guiScale, opacity);
				}
			}
		}
	}

	/*@ForgeSubscribe
	public void onRenderPlayerArmor(RenderPlayerEvent.SetArmorModel armorEvent) {
		if (armorEvent.entity instanceof EntityOtherPlayerMP) {
			EntityOtherPlayerMP player = (EntityOtherPlayerMP) armorEvent.entity;

			GroupConfiguration group = SimpleLocator.settings.getGroups().getByUsername(player.username);
			Color color;

			if (group != null && (color = group.getColor()) != null) {
				if (armorEvent.stack != null) {
					Item armorItem = armorEvent.stack.getItem();
					if (armorItem instanceof ItemArmor) {
						ItemArmor armor = (ItemArmor)armorItem; // god this looks dumb
						ModelBiped modelBiped;
						int slot = armorEvent.slot;

						Minecraft.getMinecraft().renderEngine.bindTexture(RenderBiped.getArmorResource(armorEvent.entityPlayer, armorEvent.stack, armorEvent.slot, null));

						if (slot == 2) {
							modelBiped = new ModelBiped(0.5f);
						} else {
							modelBiped = new ModelBiped(1.0f);
						}

						modelBiped.bipedHead.showModel = slot == 0;
						modelBiped.bipedHeadwear.showModel = slot == 0;
						modelBiped.bipedBody.showModel = slot == 1 || slot == 2;
						modelBiped.bipedRightArm.showModel = slot == 1;
						modelBiped.bipedLeftArm.showModel = slot == 1;
						modelBiped.bipedRightLeg.showModel = slot == 2 || slot == 3;
						modelBiped.bipedLeftLeg.showModel = slot == 2 || slot == 3;
						armorEvent.renderer.setRenderPassModel(modelBiped);

						// Otherwise it renders as a child and I can't access armorEvent.renderer.mainModel from here
						modelBiped.isChild = false; 

						// r/g/b floats for the armor color
						GL11.glColor3f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);

						if (armorEvent.stack.isItemEnchanted()) {
							armorEvent.result = 15;
						}
						armorEvent.result = 1;
					}
				}
			}
		}
	}*/

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Post event) {
		if (event.entity instanceof EntityOtherPlayerMP) {
			EntityOtherPlayerMP e = (EntityOtherPlayerMP) event.entity;

			LocatorSettings settings = SimpleLocator.settings;

			GroupConfiguration group = settings.getGroups().getByUsername(SimpleLocator.filterChatColors(e.getName()));

			if(group != null) {
				Color c = group.getColor();

				if(c == null) {
					c = settings.getDefaultColor();
				}

				if(c != null) {
					Entity player = Minecraft.getMinecraft().thePlayer;
					double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.partialRenderTick;
					double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.partialRenderTick;
					double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.partialRenderTick;

					render(-(renderPosX - e.posX), -(renderPosY - e.posY), -(renderPosZ - e.posZ), e, c);
				}
			}
		}
	}

	private void render(double x, double y, double z, EntityLivingBase e, Color c)
	{
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(5F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL13.GL_MULTISAMPLE);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.09F);

		GL11.glPushMatrix();
		//System.out.println(x + ", " + y + ", " + z);
		//GL11.glTranslated(x, y, z);
		//GL11.glRotatef(e.rotationYaw, 0.0F, (float)y, 0.0F);
		//GL11.glTranslated(-x, -y, -z);
		GL11.glTranslated(0.0D, e.isSneaking() ? -0.1D : 0.1D, 0.0D);
		AxisAlignedBB bb = AxisAlignedBB.fromBounds(x - (double)e.width, y, z - (double)e.width, x + (double)e.width, y + (double)e.height, z + (double)e.width);
		bb = bb.contract(0.25D, 0.0D, 0.25D);

		GL11.glColor3f(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F);
		drawCircle(bb);
		GL11.glColor3f(1F, 1F, 1F);
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL13.GL_MULTISAMPLE);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	private static final int segments = 20;

	public static void drawCircle(AxisAlignedBB bb)
	{
		Tessellator t = Tessellator.getInstance();

		double centerX = (bb.minX + bb.maxX) / 2;
		double centerY = (bb.minY + bb.maxY) / 2;
		double centerZ = (bb.minZ + bb.maxZ) / 2;

		WorldRenderer wr = t.getWorldRenderer();
		wr.startDrawing(3);

		for(float i = 0; i < segments + 1; i++) {
			wr.addVertex(Math.cos((i / segments) * 2 * Math.PI) * .6 + centerX, centerY, Math.sin((i / segments) * 2 * Math.PI) * .6 + centerZ);
		}
		t.draw();
	}

	public static void drawCrossedOutlinedBoundingBox(AxisAlignedBB bb)
	{
		Tessellator t = Tessellator.getInstance();
		WorldRenderer wr = t.getWorldRenderer();
		wr.startDrawing(3);
		wr.addVertex(bb.minX, bb.minY, bb.minZ);
		wr.addVertex(bb.maxX, bb.minY, bb.minZ);
		wr.addVertex(bb.maxX, bb.minY, bb.maxZ);
		wr.addVertex(bb.minX, bb.minY, bb.maxZ);
		wr.addVertex(bb.minX, bb.minY, bb.minZ);
		t.draw();
		wr.startDrawing(3);
		wr.addVertex(bb.minX, bb.maxY, bb.minZ);
		wr.addVertex(bb.maxX, bb.maxY, bb.minZ);
		wr.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		wr.addVertex(bb.minX, bb.maxY, bb.maxZ);
		wr.addVertex(bb.minX, bb.maxY, bb.minZ);
		t.draw();
		wr.startDrawing(1);
		wr.addVertex(bb.minX, bb.minY, bb.minZ);
		wr.addVertex(bb.minX, bb.maxY, bb.minZ);
		wr.addVertex(bb.maxX, bb.minY, bb.minZ);
		wr.addVertex(bb.maxX, bb.maxY, bb.minZ);
		wr.addVertex(bb.maxX, bb.minY, bb.maxZ);
		wr.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		wr.addVertex(bb.minX, bb.minY, bb.maxZ);
		wr.addVertex(bb.minX, bb.maxY, bb.maxZ);
		wr.addVertex(bb.minX, bb.minY, bb.minZ);
		wr.addVertex(bb.minX, bb.maxY, bb.maxZ);
		wr.addVertex(bb.maxX, bb.minY, bb.minZ);
		wr.addVertex(bb.maxX, bb.maxY, bb.maxZ);
		t.draw();
	}

	public static boolean isVisible(String username) {
		if(username.equals(mc.thePlayer.getName()))
			return true;

		for(Object o : mc.theWorld.playerEntities) {
			if(o instanceof EntityOtherPlayerMP) {
				if(SimpleLocator.filterChatColors(((EntityOtherPlayerMP) o).getName()).equals(username)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isOnline(String username) {
		username = SimpleLocator.filterChatColors(username);

		Collection players = mc.thePlayer.sendQueue.func_175106_d();

		for(Object o : players) {
			if(o instanceof NetworkPlayerInfo) {
				NetworkPlayerInfo info = (NetworkPlayerInfo) o;

				if(SimpleLocator.filterChatColors(info.getGameProfile().getName()).equals(username)) {
					return true;
				}
			}
		}

		return false;
	}

	public static void renderText(String[] text, float x, float y, float z, int color, boolean renderBlackBox, float partialTickTime, float guiScale, float opacity)
	{
		color = ((int) (opacity * 255.0F) << 24 ) | (color & 0x00ffffff);

		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		FontRenderer fontRenderer = mc.fontRendererObj;

		float playerX = (float) (mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTickTime);
		float playerY = (float) (mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTickTime);
		float playerZ = (float) (mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTickTime);

		float dx = x - playerX;
		float dy = y - playerY;
		float dz = z - playerZ;
		float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		float multiplier = distance / 120F;
		float scale = 0.45f * multiplier;
		scale *= guiScale;

		//System.out.println(opacity);

		GL11.glPushMatrix();
		GL11.glTranslatef(dx, dy, dz);
		GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scale, -scale, scale);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


		int textWidth = 0;

		for(String line : text) {
			int current = fontRenderer.getStringWidth(line);

			if(current > textWidth) {
				textWidth = current;
			}
		}

		int lineHeight = 10;

		if(renderBlackBox)
		{	
			Tessellator tessellator = Tessellator.getInstance();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			WorldRenderer wr = tessellator.getWorldRenderer();
			wr.startDrawingQuads();
			int stringMiddle = textWidth / 2;
			wr.setColorRGBA_F(0.0F, 0.0F, 0.0F, opacity);
			wr.addVertex(-stringMiddle - 1, -1 + 0, 0.0D);
			wr.addVertex(-stringMiddle - 1, 8 + lineHeight * text.length - lineHeight, 0.0D);
			wr.addVertex(stringMiddle + 1, 8 + lineHeight * text.length - lineHeight, 0.0D);
			wr.addVertex(stringMiddle + 1, -1 + 0, 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		int i = 0;

		for(String message : text)
		{
			int messageLength = fontRenderer.getStringWidth(message);
			fontRenderer.drawString(message, -messageLength / 2, i * lineHeight, color);
			i++;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}

	private static String formatTime(long time) {
		if(time < 99000) {
			return (time / 1000) + " sec";
		}
		else {
			return (time / 1000 / 60) + " min";
		}
	}

	public static boolean renderLocation(String username, LocatorLocation location) {
		LocatorSettings settings = SimpleLocator.settings;

		GroupConfiguration groupConfiguration = settings.getGroups().getByUsername(username);
		int maxDistUser = groupConfiguration != null ? groupConfiguration.getMaxViewDistanceOverride() : settings.getMaxViewDistance();
		int maxAge = groupConfiguration != null ? groupConfiguration.getExpirationTimeOverride() : settings.getExpirationTime();

		if(location.getAge() > maxAge) {
			synchronized(locations) {
				locations.remove(username);
			}

			return false;
		}
		
		if(!LocatorLocation.dimensionToWorld(mc.thePlayer.dimension).equals(location.getWorld())) {
			return false;
		}

		if(isVisible(username)) {
			return false;
		}

		if(!settings.isShowExactOfflineEnabled() && location.getType().isExact() && !isOnline(username)) {
			return false;
		}

		double xLength = location.getX() - mc.thePlayer.posX;
		double yLength = location.getY() - mc.thePlayer.posY;
		double zLength = location.getZ() - mc.thePlayer.posZ;

		double distance = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);

		if(distance > maxDistUser) {
			return false;
		}

		return true;
	}
}
