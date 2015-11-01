package shadowjay1.forge.simplelocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocatorPacket {
	private String sessionId;
	private Action action;
	private String ip;
	private int port;
	private LocatorLocation ownLocation = null;
	private HashMap<String, LocatorLocation> activeLocations;
	private HashMap<String, LocatorLocation> exactLocations;
	private ArrayList<String> trustedUsers;
	private long localTime;
	
	public LocatorPacket(String sessionId, String ip, int port, HashMap<String, LocatorLocation> activeLocations, HashMap<String, LocatorLocation> exactLocations, ArrayList<String> trustedUsers) {
		this.sessionId = sessionId;
		this.action = Action.REQUEST;
		this.ip = ip;
		this.port = port;
		this.activeLocations = activeLocations;
		this.exactLocations = exactLocations;
		this.trustedUsers = trustedUsers;
	}
	
	public Action getAction() {
		return action;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setOwnLocation(LocatorLocation location) {
		this.ownLocation = location;
	}
	
	public Map<String, LocatorLocation> getActiveLocations() {
		if(activeLocations == null) {
			return new HashMap<String, LocatorLocation>();
		}
		
		return activeLocations;
	}
	
	public Map<String, LocatorLocation> getPlayerLocations() {
		if(exactLocations == null) {
			return new HashMap<String, LocatorLocation>();
		}
		
		return exactLocations;
	}
	
	public List<String> getTrustedUsers() {
		if(trustedUsers == null) {
			return new ArrayList<String>();
		}
		
		return trustedUsers;
	}
	
	public long getLocalTime() {
		return localTime;
	}
	
	@Override
	public String toString() {
		return "LocatorPacket{id:" + sessionId +"; action:" + action.toString() + "}";
	}
	
	public enum Action {
		REQUEST, RESPONSE, DENY
	}
}
