package stsfinegrain.utilities;

import java.util.ArrayList;

/**
 * @author Davor Jovanović
 */
public class MatrixOperations {

    public static double [][] scalarMultiplication(double[][] matrix, double factor, int m, int n){
    	double [][] rezMatrix = new double[m][n];
        for(int i=0; i<m; i++ )
            for(int j=0; j<n; j++) 
            	rezMatrix[i][j] = matrix[i][j]*factor;
        return rezMatrix;
    }

    public static double [][] addition(double[][] matrix1, double[][]matrix2, int m, int n){
        double[][] rezMatrix = new double[m][n];
        for(int i=0; i<m; i++)
            for(int j=0; j<n; j++) rezMatrix[i][j] = matrix1[i][j]+matrix2[i][j];
        return rezMatrix;
    }

    public static double[][] findMaxElemAndRemove (double[][] matrix, int m, int n, ArrayList<Double> ro) {
        if (m==0 || n==0) return null;

        double[][] rezMatrix=new double[m-1][n-1];
        int skipI=0;
        int skipJ=0;
        double max=0.0;

        for(int i=0; i<m; i++)
            for(int j=0; j<n; j++)
                if(matrix[i][j] > max) {
                    skipI = i;
                    skipJ = j;
                    max = matrix[i][j];
                }

        // construct rezMatrix
        if(max == 0.0) return null;
        int ii = 0;
        int jj = 0;

        for(int i=0; i<m; i++) {
            for(int j=0; j<n; j++) {
                if (skipI != i && skipJ != j) rezMatrix[ii][jj++] = matrix[i][j];
            }
            if (skipI != i) ii++;
            // and reset jj to zero
            jj = 0;
        }

        // add to ro list
        ro.add(max);
        return rezMatrix;
    }
}
