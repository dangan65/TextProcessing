import java.io.*;
import java.util.*;

/**
 * Load the dataset CSV into Email objects.
 */
public class DatasetLoader {

    public static List<Email> load(String filename) throws IOException {
        List<Email> emails = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine(); // Skip header
        
        while ((line = br.readLine()) != null) {
            int lastComma = line.lastIndexOf(',');
            if (lastComma < 0) continue;
            String text = line.substring(0, lastComma).trim();
            String labelStr = line.substring(lastComma + 1).trim().replaceAll("[^0-9]", "");
            try {
                int label = Integer.parseInt(labelStr);
                emails.add(new Email(label, text));
            } catch (NumberFormatException ignored) {}
        }
        br.close();
        return emails;
    }
}
