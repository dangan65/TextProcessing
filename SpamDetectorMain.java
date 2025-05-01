import java.io.IOException;
import java.util.*;

public class SpamDetectorMain {
    public static void main(String[] args) throws IOException {
        // Load and shuffle dataset
        List<Email> allEmails = DatasetLoader.load("spam_or_not_spam.csv");
        for (int trial = 1; trial <= 5; trial++) {
            Collections.shuffle(allEmails); 
            int split = (int) (0.8 * allEmails.size());
            List<Email> train = allEmails.subList(0, split);
            List<Email> test  = allEmails.subList(split, allEmails.size());

            // Initialize FeatureExtractor with training data
            FeatureExtractor extractor = new FeatureExtractor(train);

            // Extract features for train and test sets
            Map<Email, double[]> trainFeatures = new LinkedHashMap<>();
            for (Email e : train) {
                trainFeatures.put(e, extractor.extractFeatures(e));
            }
            Map<Email, double[]> testFeatures = new LinkedHashMap<>();
            for (Email e : test) {
                testFeatures.put(e, extractor.extractFeatures(e));
            }

            // Save features and summary
            FeatureSummary.saveSummaryStats(trainFeatures, "feature_summary.csv");

            // Build spam and ham models by averaging features
            List<double[]> spamVectors = new ArrayList<>();
            List<double[]> hamVectors  = new ArrayList<>();
            for (Map.Entry<Email, double[]> entry : trainFeatures.entrySet()) {
                if (entry.getKey().label == 1) spamVectors.add(entry.getValue());
                else                           hamVectors.add(entry.getValue());
            }
            ModelClassifier classifier = new ModelClassifier(spamVectors, hamVectors);

            // Common words 
            Set<String> stopwords = new HashSet<>(Arrays.asList(
                "the","to","and","a","of","in","for","on","with",
                "is","at","by","an","be","this","that","from","or",
                "as","it","are","your","you","i","we","us","was",
                "have","has"
            ));
            List<String> indicators = SpamRuleFilter.extractTopIndicators(train, stopwords, 5, 10);

            // Evaluate accuracy
            int correct = 0;
            for (Map.Entry<Email, double[]> entry : testFeatures.entrySet()) {
                Email email = entry.getKey();
                double[] feats = entry.getValue();

                int pred = classifier.classify(feats);
                if (SpamRuleFilter.overrideToSpam(email.text, indicators)) pred = 1;
                if (pred == email.label) correct++;
            }

            System.out.printf("Accuracy: %.2f%%%n", 100.0 * correct / testFeatures.size());

            // Print top spam words
            System.out.println("Top spam-indicators:");
            indicators.forEach(System.out::println);
            
         System.out.println("\n");
        }
    }
}
