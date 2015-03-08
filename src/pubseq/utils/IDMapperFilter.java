package pubseq.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

	public static void main(String[] args) throws IOException {

		File in = new File(args[0]);

		File out = new File(args[1]);
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
			splits = line.split(args[3]);

			if (splits[1].equals(args[2])) {
				bw.write("\"" + splits[0] + "\",\"" + splits[1] + "\",\""
						+ splits[2] + "\"\n");
				if (++count % 1000 == 0) {
					System.out.println("Done writing " + count
							+ " UniProt entries");
				}
			}
		}

		System.out.println("Finished writing " + count + " UniProt entries");

		br.close();
		bw.close();

	}

}
