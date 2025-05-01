import java.util.*;

/**
 * Extracts feature vectors for emails:
 * TF–IDF weights for each token in the training vocabulary.
 * Four supplemental numeric features:
 *   - special‐character ratio
 *   - digit ratio
 *   - all‐caps ratio
 *   - link ratio
 */

 public class FeatureExtractor {
    private final Map<String, Integer> documentFrequencies;
    private final Map<String, Double> idf;
    private final List<String> vocabulary;
    private final int numDocuments;

    /**
     * Builds the extractor by scanning the training emails to
     * compute document frequencies
     *
     * @param allEmails the full list of training emails
     */

    public FeatureExtractor(List<Email> allEmails) {
        this.numDocuments = allEmails.size();
        this.documentFrequencies = new HashMap<>();
        computeDocumentFrequencies(allEmails);
        this.idf = new HashMap<>();
        computeIdf();
        this.vocabulary = new ArrayList<>(idf.keySet());
    }

    /**
     * First pass: count in how many documents each token appears.
     */
    private void computeDocumentFrequencies(List<Email> emails) {
        for (Email email : emails) {
            Set<String> seen = new HashSet<>(tokenize(email.text));
            for (String token : seen) {
                documentFrequencies.merge(token, 1, Integer::sum);
            }
        }
    }

    /**
     * Converts email frequencies to IDF weights:
     *   idf = log(N / (df + 1)) + 1
     *
     * @return a map from token to its IDF weight
     */
    private void computeIdf() {
        for (Map.Entry<String, Integer> entry : documentFrequencies.entrySet()) {
            String token = entry.getKey();
            int df = entry.getValue();
            double idfValue = Math.log((double) numDocuments / (df + 1)) + 1.0;
            idf.put(token, idfValue);
        }
    }

    /**
     * Extracts a combined TF–IDF + numeric features for an email
     */

    public double[] extractFeatures(Email email) {

        //  Compute term frequencies
        Map<String, Integer> tf = new HashMap<>();
        List<String> tokens = tokenize(email.text);
        for (String token : tokens) {
            tf.merge(token, 1, Integer::sum);
        }

        int vocabSize = vocabulary.size();
        // total features = vocabSize + 4 numeric features (special char, digits, caps, hyperlink)
        double[] features = new double[vocabSize + 4];


        // Fill TF–IDF for vocabulary tokens
        for (int i = 0; i < vocabSize; i++) {
            String token = vocabulary.get(i);
            int termFreq = tf.getOrDefault(token, 0);
            if (termFreq > 0) {
                double tfWeight = 1.0 + Math.log(termFreq);
                double idfWeight = idf.getOrDefault(token, Math.log((double) numDocuments + 1));
                features[i] = tfWeight * idfWeight;
            } else {
                features[i] = 0.0;
            }
        }

        // additional numeric features for computation
        String text = email.text;
        int length = text.length();
        int specialCount = 0, digitCount = 0;
        for (char c : text.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) specialCount++;
            if (Character.isDigit(c)) digitCount++;
        }
        double specialCharRatio = length > 0 ? (double) specialCount / length : 0.0;
        double digitRatio       = length > 0 ? (double) digitCount / length : 0.0;

        String[] words = text.split("\\s+");
        int allCapsCount = 0;
        for (String w : words) {
            if (w.length() > 1 && w.equals(w.toUpperCase())) allCapsCount++;
        }
        double allCapsRatio = words.length > 0 ? (double) allCapsCount / words.length : 0.0;

        // Count url
        int urlCount = 0;
        for (String w : words) {
            if (w.equals("URL")) urlCount++;
        }
        double linkRatio = words.length > 0 ? (double) urlCount / words.length : 0.0;

        // Append numeric features at the end
        features[vocabSize]     = specialCharRatio;
        features[vocabSize + 1] = digitRatio;
        features[vocabSize + 2] = allCapsRatio;
        features[vocabSize + 3] = linkRatio;

        return features;
    }

    /**
     * Splits text on non-word characters and lowercases tokens.
     *
     * @param text the raw text
     * @return a list of word tokens
     */
    private static List<String> tokenize(String text) {
        String[] parts = text.toLowerCase().split("\\W+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        return tokens;
    }
}