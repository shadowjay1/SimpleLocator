package shadowjay1.forge.simplelocator;

import java.util.ArrayList;

public class GroupList extends ArrayList<GroupConfiguration> {
	public int countGroups() {
		return this.size();
	}
	
	public void swapGroups(int i1, int i2) {
		GroupConfiguration g1 = this.get(i1);
		GroupConfiguration g2 = this.get(i2);
		
		this.set(i1, g2);
		this.set(i2, g1);
	}
	
	public GroupConfiguration getByUsername(String username) {
		for(GroupConfiguration group : this) {
			if(group.getUsernames().contains(username)) {
				return group;
			}
		}
		
		return null;
	}
}
