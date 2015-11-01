package shadowjay1.forge.simplelocator;

import java.io.Serializable;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class LocatorLocation implements ILocation, Serializable {
	private static final HashMap<Integer, String> dimensionToWorld = new HashMap<Integer, String>();
	
	private final double x;
	private final double y;
	private final double z;
	private final LocationType type;
	private final long creationTime;
	private final String worldName;
	private final String sourceUser;
	
	private final byte[] encryptedData;
	
	static {
		dimensionToWorld.put(-1, "world_nether");
		dimensionToWorld.put(0, "world");
		dimensionToWorld.put(1, "world_the_end");
	}
	
	public static String dimensionToWorld(int dimension) {
		return dimensionToWorld.get(dimension);
	}
	
	public LocatorLocation(long creationTime, String sourceUser, byte[] encryptedData) {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.type = null;
		this.creationTime = creationTime;
		this.worldName = null;
		this.sourceUser = sourceUser;
		
		this.encryptedData = encryptedData;
	}
	
	public LocatorLocation(double x, double y, double z, LocationType type, String worldName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.creationTime = System.currentTimeMillis();
		this.worldName = worldName;
		this.sourceUser = Minecraft.getMinecraft().getSession().getUsername();
		
		this.encryptedData = null;
	}
	
	public LocatorLocation(EntityPlayer p) {
		this.x = p.posX;
		this.y = p.posY;
		this.z = p.posZ;
		this.type = LocationType.EXACT;
		this.creationTime = System.currentTimeMillis();
		this.worldName = dimensionToWorld(p.dimension);
		this.sourceUser = Minecraft.getMinecraft().getSession().getUsername();
		
		this.encryptedData = null;
	}
	
	public LocatorLocation(double x, double y, double z, LocationType type, long creationTime, String worldName, String sourceUser) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.creationTime = creationTime;
		this.worldName = worldName;
		this.sourceUser = sourceUser;
		
		this.encryptedData = null;
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
	
	public LocationType getType() {
		return type;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	public long getAge() {
		return System.currentTimeMillis() - creationTime;
	}
	
	public String getWorld() {
		return worldName;
	}
	
	public String getSourceUser() {
		return sourceUser;
	}
	
	public byte[] getEncryptedData() {
		return encryptedData;
	}
	
	public int compareTo(LocatorLocation loc2) {
		long time1 = this.getEffectiveTime();
		long time2 = loc2.getEffectiveTime();
		
		if(time1 > time2) {
			return 1;
		}
		else if(time1 < time2) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	private long getEffectiveTime() {
		switch(type) {
		case EXACT:
			return creationTime;
		case SNITCH:
			return creationTime - 5000;
		case PPBROADCAST:
			return creationTime - 4000;
		case DOWNLOADED:
			return creationTime - 3000;
		case DOWNLOADED_RADAR:
			return creationTime - 3000;
		default:
			return creationTime;
		}
	}
	
	public LocatorLocation setType(LocationType type) {
		return new LocatorLocation(this.x, this.y, this.z, type, this.creationTime, this.worldName, this.sourceUser);
	}
	
	public LocatorLocation setCreationTime(long time) {
		return new LocatorLocation(this.x, this.y, this.z, this.type, time, this.worldName, this.sourceUser);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof LocatorLocation) {
			LocatorLocation l2 = (LocatorLocation) o;
			
			return this.x == l2.x && this.y == l2.y && this.z == l2.z && this.type == l2.type;
		}
		
		return false;
	}
}
