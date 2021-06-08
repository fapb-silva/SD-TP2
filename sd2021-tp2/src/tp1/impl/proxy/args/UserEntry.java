package tp1.impl.proxy.args;

import java.util.HashSet;
import java.util.Set;

public class UserEntry {
	private Set<String> entries;
	public UserEntry() {
		entries = new HashSet<String>();
	}
	public void addCreatedSheet(String sheetId) {
		if(!entries.contains(sheetId))
			entries.add(sheetId);
	}
	public void removeDeletedSheet(String sheetId) {
		if(entries.contains(sheetId))
			entries.remove(sheetId);
	}
	public Set<String> getSet() {
		return entries;
	}

}
