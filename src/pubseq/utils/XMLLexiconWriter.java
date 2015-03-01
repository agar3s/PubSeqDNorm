package pubseq.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;

import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;

public class XMLLexiconWriter {

	protected static XMLEventAllocator allocator;

	// private final String[] EXCLUDE = { "unidentified", "unrecognized",
	// "uncharacterized" };
	private String[] EXCLUDE;
	private String inPath = "default_in.xml";
	private String dictPath = "default_dict.tsv";
	private String abbPath = "default_abb.tsv";

	private BufferedWriter bwd = null;
	private BufferedWriter bwa = null;

	private int counter;

	public XMLLexiconWriter(String _inPath, String _dictPath, String _abbPath,
			boolean includePutative) {
		this.inPath = _inPath;
		this.dictPath = _dictPath;
		this.abbPath = _abbPath;

		if (includePutative) {
			this.EXCLUDE = new String[] { "unidentified", "unrecognized",
					"uncharacterized" };
		} else {
			this.EXCLUDE = new String[] { "unidentified", "unrecognized",
					"uncharacterized", "putative" };
		}

		this.counter = 0;
	}

	private void initiateWriters() throws UnsupportedEncodingException,
			FileNotFoundException {

		File dictFile = new File(this.dictPath);
		if (dictFile.exists())
			dictFile.delete();
		this.bwd = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(this.dictPath, true), "UTF-8"));

		File abbFile = new File(this.abbPath);
		if (abbFile.exists())
			abbFile.delete();
		this.bwa = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(this.abbPath, true), "UTF-8"));

	}

	private void parseIn() throws XMLStreamException, IOException {

		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		System.out.println("FACTORY: " + xmlif);
		xmlif.setEventAllocator(new XMLEventAllocatorImpl());
		allocator = xmlif.getEventAllocator();
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(this.inPath,
				new FileInputStream(this.inPath));

		int eventType = xmlr.getEventType();

		boolean newEntry = false;
		boolean gene = false;
		boolean recName = false;
		UniprotEntry entry = null;
		String curName = "";
		String shortName = "";

		while (xmlr.hasNext()) {
			eventType = xmlr.next();

			// System.out.println(getEventTypeString(eventType));

			if (eventType == XMLStreamConstants.START_ELEMENT) {

				switch (xmlr.getLocalName()) {
				case "entry":
					// parsing new entry
					newEntry = true;
					if (entry != null) {
						String rn = entry.getRecName();

						boolean ex = false;

						for (String exclude : this.EXCLUDE) {
							ex = ex | rn.toLowerCase().contains(exclude);
						}

						if (!ex)
							writeEntry(entry);
					}
					break;
				case "protein":
					// parsed start element of protein, prepare to parse recName
					recName = true;
					break;
				case "name":
					// encountering name
					if (newEntry) {
						// parsing protein ID
						String protID = xmlr.getElementText();
						entry = new UniprotEntry(protID);
						curName = protID;
						newEntry = false;
					} else if (gene) {
						// parsing gene name
						if (xmlr.getAttributeCount() > 0) {
							String someVal = xmlr.getAttributeValue(0);
							if (someVal.equals("primary")
									|| someVal.equals("synonym")) {
								String genName = xmlr.getElementText();
								entry.addAltName(genName);
							}
						}
					}
					break;
				case "fullName":
					// encounter fullName, update curName
					if (recName) {
						// fullName of recName
						curName = xmlr.getElementText();
						entry.setRecName(curName);
						recName = false;
					} else {
						// fullName of altName
						curName = xmlr.getElementText();
						entry.addAltName(curName);
					}
					break;
				case "shortName":
					// encounter shortNamee, associated shortName with curName
					shortName = xmlr.getElementText();
					entry.addAbbrev(curName, shortName);
					break;
				case "gene":
					gene = true;
				}
			}

			//
			// // Get all "Book" elements as XMLEvent object
			// if (eventType == XMLStreamConstants.START_ELEMENT
			// && xmlr.getLocalName().equals("Book")) {
			// // get immutable XMLEvent
			// XMLEvent something = getXMLEvent(xmlr);
			// StartElement event = getXMLEvent(xmlr).asStartElement();
			//
			// System.out.println("name");
			// System.out.println(xmlr.getName());
			//
			// // System.out.println("EVENT: " + event.toString());
			// }
			//
			// if (eventType == XMLStreamConstants.START_ELEMENT
			// && xmlr.getLocalName().equals("Title")) {
			// System.out.println("text element");
			// System.out.println(xmlr.getElementText());
			// }
			//
			// if (eventType == XMLStreamConstants.CHARACTERS) {
			//
			// XMLEvent something = getXMLEvent(xmlr);
			//
			// Characters ch = something.asCharacters();
			//
			// System.out.println("toString");
			// System.out.println(ch.toString());
			// System.out.println("something");
			// System.out.println(something);
			// }

		}
	}

	private void writeEntry(UniprotEntry entry) throws IOException {

		this.bwd.write(entry.getDictDef());
		this.bwa.write(entry.getAbbrevDef());

		if (++this.counter % 1000 == 0) {
			System.out.println("Parsed " + this.counter + " entries");
		}

	}

	@SuppressWarnings("unused")
	private static XMLEvent getXMLEvent(XMLStreamReader reader)
			throws XMLStreamException {
		return allocator.allocate(reader);
	}

	public final static String getEventTypeString(int eventType) {
		switch (eventType) {
		case XMLEvent.START_ELEMENT:
			return "START_ELEMENT";

		case XMLEvent.END_ELEMENT:
			return "END_ELEMENT";

		case XMLEvent.PROCESSING_INSTRUCTION:
			return "PROCESSING_INSTRUCTION";

		case XMLEvent.CHARACTERS:
			return "CHARACTERS";

		case XMLEvent.COMMENT:
			return "COMMENT";

		case XMLEvent.START_DOCUMENT:
			return "START_DOCUMENT";

		case XMLEvent.END_DOCUMENT:
			return "END_DOCUMENT";

		case XMLEvent.ENTITY_REFERENCE:
			return "ENTITY_REFERENCE";

		case XMLEvent.ATTRIBUTE:
			return "ATTRIBUTE";

		case XMLEvent.DTD:
			return "DTD";

		case XMLEvent.CDATA:
			return "CDATA";

		case XMLEvent.SPACE:
			return "SPACE";
		}
		return "UNKNOWN_EVENT_TYPE , " + eventType;
	}

	public static void main(String[] args) throws XMLStreamException,
			IOException {

		XMLLexiconWriter lw = new XMLLexiconWriter(args[0], args[1], args[2], args.length > 3);

		lw.initiateWriters();
		lw.parseIn();
	}

}
