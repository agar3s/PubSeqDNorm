/**
 * 
 */
package pubseq.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Silphe
 *
 */
public class UniprotEntry {

	private String proteinID;
	private String recName;
	private Set<String> altNames;
	private Map<String, Set<String>> abbrevs;

	public UniprotEntry(String _proteinID) {

		this.proteinID = _proteinID;
		this.altNames = new HashSet<>();
		this.abbrevs = new HashMap<>();
	}

	public void setRecName(String _recName) {
		this.recName = _recName;
	}

	public String getRecName() {
		return this.recName;
	}

	/**
	 * Returns true if {@link #altNames} already contained altName
	 * 
	 * @param altName
	 * @return
	 */
	public boolean addAltName(String altName) {
		return this.altNames.add(altName);
	}

	/**
	 * Returns true if abbrev is already associated with name
	 * 
	 * @param name
	 * @param abbrev
	 * @return
	 */
	public boolean addAbbrev(String name, String abbrev) {

		Set<String> set;

		if (this.abbrevs.get(name) != null) {
			set = this.abbrevs.get(name);
			return set.add(abbrev);
		} else {
			set = new HashSet<>();
			this.abbrevs.put(name, set);
			return set.add(abbrev);
		}
	}

	public String getDictDef() {

		String out = this.recName + "\t" + this.proteinID + "\t\t\t\t\t\t";

		boolean start = true;

		for (String name : this.altNames) {

			if (start) {
				out += name;
				start = false;
			} else {
				out += ("|" + name);
			}
		}

		out += System.getProperty("line.separator");
		return out;
	}

	public String getAbbrevDef() {

		String out = "";

		for (String key : this.abbrevs.keySet()) {

			Set<String> abbrevList = this.abbrevs.get(key);

			for (String abbrev : abbrevList) {
				out += abbrev + "\t" + key
						+ System.getProperty("line.separator");
			}
		}

		return out;
	}

}
