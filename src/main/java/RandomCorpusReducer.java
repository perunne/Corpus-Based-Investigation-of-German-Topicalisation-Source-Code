import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCorpusReducer {

    public static void main(String[] args) {
        String inputFilePath = "deu_mixed-typical_2011_300K-sentences.txt"; // Input corpus file path
        String outputFilePath = "reduced_corpus.csv"; // Output reduced corpus file path
        int targetSize = 500; // Desired number of sentences in the reduced corpus

        try {
            List<String> lines = Files.readAllLines(Paths.get(inputFilePath));
            List<String> reducedLines = randomReduce(lines, targetSize);
            Files.write(Paths.get(outputFilePath), reducedLines);
            System.out.println("Reduced corpus saved to: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> randomReduce(List<String> lines, int targetSize) {
        if (lines.size() <= targetSize) {
            return lines;
        }

        List<String> reducedLines = new ArrayList<>(targetSize);
        Random random = new Random();
        while (reducedLines.size() < targetSize) {
            int index = random.nextInt(lines.size());
            reducedLines.add(lines.remove(index));
        }

        return reducedLines;
    }
}
