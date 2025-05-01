import java.util.*;

/**
 * Build spam and ham average features from training features
 * and classify new emails by nearest-centroid rule.
 */
public class ModelClassifier {
    /**
     * average spam email.
     */
    private final double[] spamCentroid;

    /**
     * average ham email.
     */
    private final double[] hamCentroid;

     /**
     * Constructs the classifier by computing the average features respective
     *
     * @param spamFeatures 
     * @param hamFeatures  
     */
    public ModelClassifier(Collection<double[]> spamFeatures, Collection<double[]> hamFeatures) {
        this.spamCentroid = DistanceUtils.centroid(spamFeatures);
        this.hamCentroid = DistanceUtils.centroid(hamFeatures);
    }
    /**
     * Given a new email's features see which average it is closer to
     * @param emailFeatures
     * @return 1 if the email is closer to the spam; 0 otherwise
     */
    public int classify(double[] emailFeatures) {
        double distToSpam = DistanceUtils.euclidean(emailFeatures, spamCentroid);
        double distToHam = DistanceUtils.euclidean(emailFeatures, hamCentroid);

        if (distToSpam < distToHam) {
            return 1;
        } else {
            return 0; 
        }
    }
}