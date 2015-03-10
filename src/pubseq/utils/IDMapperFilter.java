package pubseq.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class filters UniProt mapping file and write into output ID type
 * matching the one described in main method's parameter. Uniprot's latest
 * mapping is available from <a href=
 * "ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/"
 * >UniProt Knowledgebase's FTP Server<a>)
 * 
 * @author raharjaliu
 *
 */
public class IDMapperFilter {

	public static final String DB_DELIM = ",";
	public static final String UNIPROT_DELIM = "\t";

	private Map<String, String[]> oldEntries;

	public IDMapperFilter() {

		oldEntries = new HashMap<String, String[]>();

	}

	protected void parseExistingEntries(String existingDBCSV) {

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(existingDBCSV), "UTF-8"));

			String line;
			String[] entry;

			while ((line = br.readLine()) != null) {

				line = line.replaceAll("\"", "");
				entry = line.split(DB_DELIM);

				this.oldEntries.put(entry[0], entry);
			}

			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void createNewEntriesCSV(String completeUniProtEntry,
			String newEntriesCSV, String idType) {

		try {
			File in = new File(completeUniProtEntry);

			File out = new File(newEntriesCSV);
			if (out.exists())
				out.delete();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(in), "UTF-8"));

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(out, true), "UTF-8"));

			String line;
			String[] splits;
			int count = 0;

			while ((line = br.readLine()) != null) {
				splits = line.split(UNIPROT_DELIM);

				if (splits[1].equals(idType) && !this.oldEntries.containsKey(splits[0])) {
					bw.write("\"" + splits[0] + "\",\"" + splits[1] + "\",\""
							+ splits[2] + "\"\n");
					if (++count % 1000 == 0) {
						System.out.println("Done writing " + count
								+ " UniProt entries");
					}
				}
			}

			System.out
					.println("Finished writing " + count + " UniProt entries");

			br.close();
			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {

		IDMapperFilter filter = new IDMapperFilter();
		filter.parseExistingEntries(args[0]);
		filter.createNewEntriesCSV(args[1], args[2], args[3]);

	}



}
