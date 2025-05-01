import java.util.*;

/**
 * methods for computing distances and centroids (distance from ham and spam)
 * over feature collections.
 */

public class DistanceUtils {

    public static double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
     /**
     * Computes the centroid
     * This is used as the “model” for a class.
     */

    public static double[] centroid(Collection<double[]> vectors) {
        int dim = vectors.iterator().next().length;
        double[] centroid = new double[dim];
        for (double[] v : vectors) {
            for (int i = 0; i < dim; i++) centroid[i] += v[i];
        }
        for (int i = 0; i < dim; i++) centroid[i] /= vectors.size();
        return centroid;
    }
}
