package shadowjay1.forge.simplelocator;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class GroupingThread extends Thread {
	public static List<LocationGroup> groups = new ArrayList<LocationGroup>();

	public void run() {
		while (true) {
			try {
				if (Minecraft.getMinecraft().thePlayer != null) {
					synchronized (groups) {
						groups = GroupingUtils.getGroups(Minecraft.getMinecraft().thePlayer, LocatorListener.locations);
					}
				}
				
				Thread.sleep(500);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
