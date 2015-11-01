package shadowjay1.forge.simplelocator;

import java.util.List;

public class MemberList {
	private List<String> members;
	
	public MemberList(List<String> members) {
		this.members = members;
	}
	
	public void add(String str) {
		members.add(str);
	}
	
	public String get(int i) {
		return members.get(i);
	}
	
	public int countGroups() {
		return members.size();
	}
	
	public void swapGroups(int i1, int i2) {
		String s1 = members.get(i1);
		String s2 = members.get(i2);
		
		members.set(i1, s2);
		members.set(i2, s1);
	}
	
	public void remove(int i) {
		members.remove(i);
	}
}
