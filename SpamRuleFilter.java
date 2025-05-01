import java.util.*;

/**
 * Find the top spam words 
 * adds a quick rule to mark an email as spam
 */
public class SpamRuleFilter {

    /**
     * Look at each email and count how often each word shows up
     * in spam vs. non-spam. 
     * @param emails       
     * @param stopwords  basic words to ignore
     * @param minCount     
     * @param topN   
     * @return list of top spam words
     */
    public static List<String> extractTopIndicators(
            List<Email> emails,
            Set<String> stopwords,
            int minCount,
            int topN) {
        Map<String, Integer> spamCount = new HashMap<>();
        Map<String, Integer> hamCount  = new HashMap<>();

        // Count words in spam and ham
        for (Email e : emails) {
            String[] words = e.text.toLowerCase().split("\\s+");
            for (String w : words) {
                w = w.replaceAll("[^a-z0-9]", "");
                if (w.isEmpty() || stopwords.contains(w)) continue;
                if (e.label == 1) spamCount.put(w, spamCount.getOrDefault(w, 0) + 1);
                else               hamCount .put(w, hamCount .getOrDefault(w, 0) + 1);
            }
        }

        // Compute spam/ham ratio for each word in spamCount
        List<String> candidates = new ArrayList<>();
        for (String w : spamCount.keySet()) {
            int s = spamCount.get(w);
            int h = hamCount.getOrDefault(w, 1);
            if (s < minCount) continue;
            candidates.add(w + ":" + ((double)s / h));
        }

        // Sort words by ratio descending
        candidates.sort((a, b) -> {
            double ra = Double.parseDouble(a.split(":")[1]);
            double rb = Double.parseDouble(b.split(":")[1]);
            return Double.compare(rb, ra);
        });

        // Extract just the words, take topN
        List<String> topWords = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, candidates.size()); i++) {
            topWords.add(candidates.get(i).split(":")[0]);
        }
        return topWords;
    }

    /**
     * Quick check if an email contains any of the top spam words.
     * @param text      the email body
     * @param topWords  the spam words to look for
     * @return true if any topWords are found in the email
     */
    public static boolean overrideToSpam(String text, List<String> topWords) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String w : words) {
            w = w.replaceAll("[^a-z0-9]", "");
            if (topWords.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
