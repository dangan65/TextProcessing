import java.io.*;
import java.util.*;

/**
 * Summarize each feature dimension across emails (min, max, mean, median, std dev)
 * and save the CSV for external analysis.
 */

public class FeatureSummary {

    public static void saveSummaryStats(Map<Email, double[]> features, String filename) throws IOException {
        int dimension = features.values().iterator().next().length;
        List<double[]> data = new ArrayList<>(features.values());
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("feature,min,max,mean,median,std_dev\n");

        for (int i = 0; i < dimension; i++) {
            double[] values = new double[data.size()];
            for (int j = 0; j < data.size(); j++) {
                values[j] = data.get(j)[i];
            }
            Arrays.sort(values);
            double min = values[0];
            double max = values[values.length - 1];
            double mean = Arrays.stream(values).average().orElse(0);
            double median = values.length % 2 == 0 ? (values[values.length/2] + values[values.length/2 - 1]) / 2.0 : values[values.length/2];
            double stdDev = Math.sqrt(Arrays.stream(values).map(x -> Math.pow(x - mean, 2)).sum() / values.length);
            writer.write(i + "," + min + "," + max + "," + mean + "," + median + "," + stdDev + "\n");
        }

        writer.close();
    }
}
