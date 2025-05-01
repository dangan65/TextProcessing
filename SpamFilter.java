import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Load emails and split data build word features
 */
public class SpamFilter {

    static class Email {
        int label;   // 1 = spam
        String text; 

        Email(int label, String text) {
            this.label = label;
            this.text  = text;
        }
    }

    public static void main(String[] args) throws Exception {
        // Load the CSV file into a list of Emails
        List<Email> dataset = loadDataset("spam_or_not_spam.csv");

        // Shuffle and split into 80% train, 20% test (random each run)
        Collections.shuffle(dataset);
        int splitIndex = (int) (dataset.size() * 0.8);
        List<Email> trainSet = dataset.subList(0, splitIndex);
        List<Email> testSet  = dataset.subList(splitIndex, dataset.size());

        // Separate spam and not-spam emails in the training set
        List<Email> spam    = trainSet.stream()
                              .filter(e -> e.label == 1)
                              .collect(Collectors.toList());
        List<Email> notSpam = trainSet.stream()
                              .filter(e -> e.label == 0)
                              .collect(Collectors.toList());

        // Build a vocabulary from words in training emails
        Set<String> vocabulary = new HashSet<>();
        for (Email e : trainSet) {
            vocabulary.addAll(tokenize(e.text));
        }

        // Assign each word an index in the feature vector
        Map<String, Integer> wordIndex = indexVocabulary(vocabulary);

        // Build raw count features for spam and ham
        double[][] spamFeatures    = buildFeatureMatrix(spam,    wordIndex);
        double[][] notSpamFeatures = buildFeatureMatrix(notSpam, wordIndex);

        // Print out sizes
        System.out.println("Train size: " + trainSet.size());
        System.out.println("Test size: "  + testSet.size());
        System.out.println("Spam emails: " + spam.size());
        System.out.println("Not spam emails: " + notSpam.size());
        System.out.println("Vocabulary size (features): " + wordIndex.size());
    }

    /**
     * Reads the CSV and returns a list of Email objects.
     */
    static List<Email> loadDataset(String filename) throws IOException {
        List<Email> emails = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));

        br.readLine(); // skip header line
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
            String full = sb.toString();
            int lastComma = full.lastIndexOf(',');

            if (lastComma >= 0 && full.endsWith("\n")) {
                String body  = full.substring(0, lastComma)
                                  .trim().replaceAll("^\"|\"$", "");
                String lbl   = full.substring(lastComma + 1)
                                  .trim().replaceAll("[^0-9]", "");
                try {
                    emails.add(new Email(Integer.parseInt(lbl), body));
                } catch (NumberFormatException ignored) {}
                sb.setLength(0); 
            }
        }
        br.close();
        return emails;
    }

    /**
     * Splits text into lowercase words, removing non-letters.
     *
     * @param text raw email text
     * @return list of word tokens
     */
    static List<String> tokenize(String text) {
        return Arrays.asList(
            text.toLowerCase()
                .replaceAll("[^a-z ]", "")
                .split("\\s+")
        );
    }

    /**
     * Creates a map from word to features
     *
     * @param vocab set of all words
     * @return map 
     */
    static Map<String, Integer> indexVocabulary(Set<String> vocab) {
        Map<String, Integer> index = new HashMap<>();
        int i = 0;
        for (String word : vocab) {
            index.put(word, i++);
        }
        return index;
    }

    /**
     * Builds a raw count for a list of emails.
     * Each row is an email each column is a word count.
     *
     * @param emails    
     * @param wordIndex
     * @return 2D array [emailIndex][wordIndex]
     */
    static double[][] buildFeatureMatrix(
            List<Email> emails,
            Map<String, Integer> wordIndex) {
        int n = emails.size();
        int m = wordIndex.size();
        double[][] matrix = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (String token : tokenize(emails.get(i).text)) {
                Integer col = wordIndex.get(token);
                if (col != null) {
                    matrix[i][col] += 1;
                }
            }
        }
        return matrix;
    }
}
