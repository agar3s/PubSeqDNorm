/**
 * 
 */
package pubseq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import pubseq.core.GeneLexicon;
import pubseq.core.GeneNameAnalyzer;
import pubseq.core.GeneSynonymTrainer;
import pubseq.core.GeneSynonymTrainer.TrainingPoint;
import banner.eval.BANNER;
import banner.eval.dataset.Dataset;
import banner.types.Mention;
import banner.types.Sentence;
import dnorm.types.FullRankSynonymMatrix;

/**
 * 
 * PubSeq implementation of {@link TrainSynonymMatrix}. Some fields and methods
 * in this class might need to be modified. However, it is suggested that later
 * implementation should only include modification of class
 * {@link TrainGeneSynonymMatrix}.
 */
public class TrainGeneSynonymMatrix {

	public static void main(String[] args) throws ConfigurationException {

		String medicFilename = args[0]; // data/CTD_diseases.tsv
		String abbreviationsFilename = args[1];
		String trainingConfigurationFile = args[2]; // ../config/banner_NCBIDiseasePubtator.xml
		int maxRank = Integer.parseInt(args[3]); // 1000
		double lambda = Double.parseDouble(args[4]); // 0.0001
		int maxIterations = Integer.parseInt(args[5]); // 50
		String holdoutConfigurationFile = args[6]; // ../config/banner_NCBIDiseasePubtator_DEV.xml
		String matrixFilename = args[7]; // output/simmatrix.bin

		// TODO training vs lexicon iterations & lambda

		// Train against BANNER output on training data
		// Train against dictionary

		GeneNameAnalyzer analyzer = GeneNameAnalyzer.getGeneNameAnalyzer(true,
				true, false, true);
		EvalGeneDNorm dnorm = new EvalGeneDNorm(analyzer, medicFilename,
				abbreviationsFilename);
		GeneLexicon lex = dnorm.getLexicon();

		FullRankSynonymMatrix matrix = new FullRankSynonymMatrix(
				lex.getDictionary());
		GeneSynonymTrainer syn = new GeneSynonymTrainer(lex, matrix, maxRank);

		List<TrainingPoint> training = loadDataset(dnorm,
				trainingConfigurationFile, syn);
		System.out.println("training.size(): " + training.size());
		List<TrainingPoint> holdout = loadDataset(dnorm,
				holdoutConfigurationFile, syn);
		System.out.println("holdout.size(): " + holdout.size());

		// Add training names to the lexicon vector space
		for (TrainingPoint point : training)
			lex.addName(point.getName());
		lex.prepare();
		matrix.initalize();

		syn.trainArgmax(training, new GeneSynonymTrainer.FixedLearningRate(
				lambda), 1.0, holdout, maxIterations);
		matrix.write(new File(matrixFilename));
	}

	private static List<TrainingPoint> loadDataset(EvalGeneDNorm dnorm,
			String configurationFile, GeneSynonymTrainer syn)
			throws ConfigurationException {
		Map<String, String> alternateIDMap = dnorm.loader.getAlternateIDMap();
		List<TrainingPoint> data = new ArrayList<TrainingPoint>();
		System.out.println("Getting config");
		HierarchicalConfiguration config = new XMLConfiguration(
				configurationFile);
		System.out.println("Getting dataset");
		Dataset dataset = BANNER.getDataset(config);
		Set<Sentence> sentences = dataset.getSentences();
		System.out.println("Getting sentences: " + sentences.size());
		for (Sentence s : sentences) {
			String documentId = s.getDocumentId();
			for (Mention m : s.getMentions()) {
				String[] conceptIds = m.getConceptId().split("\\|");
				for (String conceptId : conceptIds) {
					if (alternateIDMap.containsKey(conceptId)) {
						conceptId = alternateIDMap.get(conceptId);
					}
					if (conceptId.indexOf(":") == -1)
						conceptId = "MESH:" + conceptId;
					// Suppress mentions that could only be normalized to
					// "disease"
					if (conceptId != null && !conceptId.equals("D004194")) {
						// Handle abbreviations
						String mentionText = m.getText();
						mentionText = dnorm.abbrev.expandAbbreviations(
								documentId, mentionText);
						TrainingPoint p = syn.new TrainingPoint(mentionText,
								conceptId);
						data.add(p);
					}
				}
			}
		}
		return data;
	}

}
