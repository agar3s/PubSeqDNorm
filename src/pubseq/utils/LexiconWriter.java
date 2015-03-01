package pubseq.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class LexiconWriter {

	/**
	 * List of name fragments that trigger exclude
	 */
	public static final String[] EXCLUDE = { "unidentified", "unrecognized",
			"uncharacterized" };

	/**
	 * Parse split entry containing list of synonyms. This entry is defined as
	 * follow:
	 * <code>FIRST ENTRY (SECOND ENTRY) (THIRD ENTRY) ... (N-TH ENTRY)</code>
	 * 
	 * @param split
	 *            entry to split and parse
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> parseSynonyms(String split)
			throws Exception {

		split = split.trim();

		ArrayList<String> names = new ArrayList<>();

		// starting index of current string
		int start = 0;
		// end index. Not inclusive
		// int end = 0;
		// number of opening bracket
		int opBracket = 0;
		// number of closing bracket
		int clBracket = 0;
		// index of opening full bracket, i.e. sum of op == cl bracket
		ArrayList<Integer> opFullBracket = new ArrayList<>();
		// index of closing full bracket
		ArrayList<Integer> clFullBracket = new ArrayList<>();

		for (int index = 0; index < split.length(); index++) {

			if (split.charAt(index) == '(' || split.charAt(index) == ')') {
				if (split.charAt(index) == '(') {
					++opBracket;
					// first opening bracket
					if (opBracket == clBracket + 1) {
						opFullBracket.add(index);
						start = index + 1;
					}

					// upon first opening, entry prefix (if prefix is non-empty)
					if (names.size() == 0 && index > 0) {
						names.add(split.substring(0, index - 1));
					}
				} else if (split.charAt(index) == ')') {
					++clBracket;
					// last closing bracket
					if (opBracket == clBracket) {
						// end = index;

						String name = split.substring(start, index);
						// if name is EC identifier, put id to last name
						if (name.startsWith("EC")) {
							String lastName = names.remove(names.size() - 1)
									+ " (" + name + ")";
							names.add(lastName);
						} else {
							names.add(name);
						}

						clFullBracket.add(index);
					}
				}
			}
		}

		if (names.size() == 0) {
			names.add(split);
		}

		if (opFullBracket.size() != clFullBracket.size()) {
			throw new Exception("full opening and closing brackets differ!");
		}

		return names;
	}

	/**
	 * Converts String list to String representation with <code>sep</code> as
	 * separator
	 * 
	 * @param input
	 *            list of String
	 * @param sep
	 *            separator
	 * @return
	 */
	public static String writeStringWithSeparato(List<String> input, char sep) {

		String out = "";

		for (int index = 0; index < input.size(); index++) {
			if (index > 0) {
				out += sep;
			}
			out += input.get(index);
		}

		return out;
	}

	/**
	 * Checks whether given <code>name</code> contains any of the following:
	 * elements from {@value #EXCLUDE} and 'putative' if
	 * <code>includePutative</code> is <code>True</code>.
	 * 
	 * @param name
	 * @param includePutative
	 * @return
	 */
	public static boolean containsExcludedFragments(String name,
			boolean includePutative) {

		name = name.toLowerCase();

		boolean include = true;

		for (String fragment : EXCLUDE) {
			include = include && !name.contains(fragment);
		}

		if (name.contains("putative"))
			include = include && includePutative;

		return include;
	}

	/**
	 * 
	 * The function argument is:
	 * <code>UNIPROT_LIST OUTPUT_LEXICON_PATH OUTPUT_EXCLUDED_PATH include*</code>
	 * . Use include to include putative into result
	 * 
	 * @param args
	 *            String array containing input path, output path and optional
	 *            placeholder (if Putative is to be included).
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		File source = new File(args[0]);
		File target = new File(args[1]);
		File excluded = new File(args[2]);

		if (target.exists())
			target.delete();
		if (excluded.exists())
			excluded.delete();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(source), "UTF-8"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(target, true), "UTF-8"));
		BufferedWriter bwex = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(excluded, true), "UTF-8"));

		bw.write("# EntryName\tRecommendedName\tAlternativeNamesAndMainAccessionID"
				+ System.getProperty("line.separator"));

		String line = null;
		int counter = 0;

		boolean includePutative = args.length > 3;
		while ((line = br.readLine()) != null) {

			if (!line.startsWith("Entry")) {
				String[] splits = line.split("\t");

				// String accessionNum = splits[0];
				String entName = splits[1];
				String protNames = splits[3];
				String geneNames = splits[4];

				// checks whether prot/gene names contain excluded substrings
				boolean include = containsExcludedFragments(geneNames
						+ protNames, includePutative);

				ArrayList<String> proteins = parseSynonyms(protNames);
				String recName = proteins.remove(0);
				// proteins.add(accessionNum);

				ArrayList<String> genes = parseSynonyms(geneNames);
				String protsString = writeStringWithSeparato(proteins, '|');
				String genesString = writeStringWithSeparato(genes, '|');

				String altNames = genesString.length() > 0
						&& protsString.length() > 0 ? protsString + '|'
						+ genesString : protsString + genesString;

				if (include) {
					bw.write(recName + "\t" + entName + "\t\t\t\t\t\t"
							+ altNames + System.getProperty("line.separator"));
				} else {
					bwex.write(recName + "\t" + entName + "\t\t\t\t\t\t"
							+ altNames + System.getProperty("line.separator"));
				}

				if (++counter % 1000 == 0) {
					System.out.println("parsed " + counter + " entries");
				}
			}
		}

		br.close();
		bw.close();
		bwex.close();
	}
}