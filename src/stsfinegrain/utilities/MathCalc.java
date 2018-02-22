package stsfinegrain.utilities;

/**
* @author Vuk Batanović
*/
public class MathCalc {

    public static double cosineSimilarity (double[] a, double[] b) {
        double dotProduct = 0.0;
        double aMagnitude = 0.0;
        double bMagnitude = 0.0;
        for (int i = 0; i < b.length ; i++) {
            aMagnitude += a[i] * a[i];
            bMagnitude += b[i] * b[i];
            dotProduct += a[i] * b[i];
        }
        aMagnitude = Math.sqrt(aMagnitude);
        bMagnitude = Math.sqrt(bMagnitude);
        if (aMagnitude == 0 || bMagnitude == 0)
        	return 0;
        else
        	return dotProduct / (aMagnitude * bMagnitude);
    }
    
    public static double cosineSimilarity (int[] a, int[] b) {
        long dotProduct = 0;
        long aMagnitude = 0;
        long bMagnitude = 0;
        for (int i = 0; i < b.length ; i++) {
            aMagnitude += a[i] * a[i];
            bMagnitude += b[i] * b[i];
            dotProduct += a[i] * b[i];
        }
        double aMagnitudeSqRt = Math.sqrt(aMagnitude);
        double bMagnitudeSqRt = Math.sqrt(bMagnitude);
        if (aMagnitudeSqRt == 0 || bMagnitudeSqRt == 0)
        	return 0;
        else
        	return dotProduct / (aMagnitudeSqRt * bMagnitudeSqRt);
    }
    
    public static double pearsonCorrelation (double[] arr1, double[] arr2) {
        double xSum = 0;
        double ySum = 0;
        for (int i = 0; i < arr1.length; ++i) {
            xSum += arr1[i];
            ySum += arr2[i];
        }
        double xMean = xSum / arr1.length;
        double yMean = ySum / arr1.length;
        double numerator = 0, xSqSum = 0, ySqSum = 0;
        for (int i = 0; i < arr1.length; ++i) {
            double x = arr1[i] - xMean;
            double y = arr2[i] - yMean;
            numerator += x * y;
            xSqSum += (x * x);
            ySqSum += (y * y);
        }
        if (xSqSum == 0 || ySqSum == 0)
            return 0;
        return numerator / Math.sqrt(xSqSum * ySqSum);
    }
}
