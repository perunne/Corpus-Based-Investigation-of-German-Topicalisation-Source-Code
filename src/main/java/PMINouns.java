import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PMINouns {

    private static final String CORPUS_FILE = "deu_mixed-typical_2011_300K-sentences.txt";
    private static final String OUTPUT_FILE = "pmi_nouns.tsv";
    private static final int MIN_PAIR_COUNT = 2;

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        props.setProperty("tokenize.language", "de");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/german-ud.tagger");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Map<String, Integer> wordCounts = new HashMap<>();
        Map<String, Integer> pairCounts = new HashMap<>();
        int totalSentences = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(CORPUS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                totalSentences++;
                Annotation document = new Annotation(line.split("\t")[1]);
                pipeline.annotate(document);

                List<String> nouns = new ArrayList<>();
                List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        if (pos.startsWith("N")) {
                            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
                            nouns.add(lemma);
                            wordCounts.put(lemma, wordCounts.getOrDefault(lemma, 0) + 1);
                        }
                    }
                }

                for (int i = 0; i < nouns.size(); i++) {
                    for (int j = i + 1; j < nouns.size(); j++) {
                        String pair = nouns.get(i) + "\t" + nouns.get(j);
                        pairCounts.put(pair, pairCounts.getOrDefault(pair, 0) + 1);
                    }
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writer.write("word1\tword2\tpmi\n");
            for (String pair : pairCounts.keySet()) {
                int pairCount = pairCounts.get(pair);
                if (pairCount >= MIN_PAIR_COUNT) {
                    String[] tokens = pair.split("\t");
                    String word1 = tokens[0];
                    String word2 = tokens[1];

                    int word1Count = wordCounts.get(word1);
                    int word2Count = wordCounts.get(word2);

                    double pmi = Math.log((double) pairCount * totalSentences / (word1Count * word2Count));

                    writer.write(word1 + "\t" + word2 + "\t" + pmi + "\n");
                }
            }
        }
    }
}

