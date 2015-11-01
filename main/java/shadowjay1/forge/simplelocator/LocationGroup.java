package shadowjay1.forge.simplelocator;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class LocationGroup extends ArrayList<String> implements ILocation {
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private float yawFromPlayer = 0;
	private float pitchFromPlayer = 0;
	
	public LocationGroup() {
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
	
	public void calculateCenter() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		
		int amount = this.size();
		
		if(amount == 0) {
			return;
		}
		
		Iterator<String> iterator = this.iterator();
		
		while(iterator.hasNext()) {
			LocatorLocation location = LocatorListener.locations.get(iterator.next());
			
			if(location == null) {
				iterator.remove();
				
				continue;
			}
			
			x += location.getX();
			y += location.getY();
			z += location.getZ();
		}
		
		x /= amount;
		y /= amount;
		z /= amount;
	}
	
	public void calculateYawFromPlayer(EntityPlayer p) {
		yawFromPlayer = (float) Math.atan2(z - p.posZ, x - p.posX);
	}
	
	public void calculatePitchFromPlayer(EntityPlayer p) {
		double xDist = x - p.posX;
		double zDist = z - p.posZ;
		double xzDist = Math.sqrt(xDist * xDist + zDist * zDist);
		pitchFromPlayer = (float) Math.atan2(y - p.posY, xzDist);
	}
	
	public float getYawFromPlayer() {
		return yawFromPlayer;
	}
	
	public float getPitchFromPlayer() {
		return pitchFromPlayer;
	}
}
