package shadowjay1.forge.simplelocator;

public enum LocationType {
	EXACT("-", (byte) 0), DOWNLOADED("|", (byte) 1), DOWNLOADED_RADAR("v", (byte) 2), SNITCH("~", (byte) 3), PPBROADCAST("o", (byte) 4);
	
	private String indicator;
	private byte index;
	
	private LocationType(String indicator, byte index) {
		this.indicator = indicator;
		this.index = index;
	}
	
	public String getIndicator() {
		return indicator;
	}
	
	public byte getIndex() {
		return index;
	}
	
	public boolean isExact() {
		return this == EXACT || this == DOWNLOADED || this == DOWNLOADED_RADAR;
	}
	
	public boolean isRemote() {
		return this == DOWNLOADED || this == DOWNLOADED_RADAR;
	}
	
	public static LocationType getByIndex(byte index) {
		for(LocationType type : LocationType.values()) {
			if(type.index == index) {
				return type;
			}
		}
		
		return null;
	}
}
