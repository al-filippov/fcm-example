package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FcmInteger {
    private final int numClusters;
    private final double[][] degreeOfMembership;
    private final double epsilon;
    private final double fuzziness;
    private final int maxIterationCount;
    private final List<Double> clusterCentre;
    private final List<Integer> dataPoints;

    public FcmInteger(final List<Integer> dataPoints,
                      int numClusters,
                      double fuzziness,
                      double epsilon,
                      int maxIterationCount) throws FcmException {
        if (dataPoints.size() == 0) {
            throw new FcmException("Number of data points should be > 0");
        }
        if (numClusters <= 0) {
            throw new FcmException("Number of clusters should be > 0");
        }
        if (fuzziness <= 1.0) {
            throw new FcmException("Fuzzyness coefficient should be > 1.0");
        }
        if (epsilon <= 0.0 || epsilon > 1.0) {
            throw new FcmException("Termination criterion should be > 0.0 and <= 1.0");
        }
        if (maxIterationCount <= 0) {
            throw new FcmException("Max iteration count should be > 0");
        }
        this.dataPoints = Collections.unmodifiableList(dataPoints);
        this.degreeOfMembership = new double[dataPoints.size()][numClusters];
        this.clusterCentre = new ArrayList<>();
        this.numClusters = numClusters;
        this.epsilon = epsilon;
        this.fuzziness = fuzziness;
        this.maxIterationCount = maxIterationCount;
        double degree;
        int maxDegree, randomValue;
        int i, j;
        for (i = 0; i < numClusters; i++) {
            clusterCentre.add(null);
        }
        final Random random = new Random(System.currentTimeMillis());
        for (i = 0; i < dataPoints.size(); i++) {
            degree = 0.0;
            maxDegree = 100;
            for (j = 1; j < numClusters; j++) {
                randomValue = random.nextInt(32767) % (maxDegree + 1);
                maxDegree -= randomValue;
                degreeOfMembership[i][j] = randomValue / 100.0;
                degree += degreeOfMembership[i][j];
            }
            degreeOfMembership[i][0] = 1.0 - Math.min(degree, 1.0);
        }
    }

    private double getNewValue(int i, int j) {
        int k;
        double power = 2 / (fuzziness - 1), sum = 0.0, top, bottom;
        for (k = 0; k < numClusters; k++) {
            top = getNormalization(i, j);
            bottom = getNormalization(i, k);
            if (top == 0.0 || bottom == 0.0) {
                continue;
            }
            sum += Math.pow(top / bottom, power);
        }
        if (sum == 0.0) {
            return 0.0;
        }
        return 1.0 / sum;
    }

    private double updateDegreeOfMembership() {
        int i, j;
        double maxDiff = 0d;
        for (j = 0; j < numClusters; j++) {
            for (i = 0; i < dataPoints.size(); i++) {
                double newUij, diff;
                newUij = getNewValue(i, j);
                diff = newUij - degreeOfMembership[i][j];
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                degreeOfMembership[i][j] = newUij;
            }
        }
        return maxDiff;
    }

    private double getNormalization(int i, int j) {
        return Math.sqrt(Math.pow(dataPoints.get(i) - clusterCentre.get(j), 2));
    }

    private void calculateCentreVectors() {
        int i, j;
        double numerator, denominator;
        double fuzzyDegreeOfMembership[][] = new double[dataPoints.size()][numClusters];
        for (i = 0; i < dataPoints.size(); i++) {
            for (j = 0; j < numClusters; j++) {
                fuzzyDegreeOfMembership[i][j] = Math.pow(degreeOfMembership[i][j], fuzziness);
            }
        }
        for (j = 0; j < clusterCentre.size(); j++) {
            numerator = 0.0;
            denominator = 0.0;
            for (i = 0; i < dataPoints.size(); i++) {
                numerator += fuzzyDegreeOfMembership[i][j] * dataPoints.get(i);
                denominator += fuzzyDegreeOfMembership[i][j];
            }
            clusterCentre.set(j,  numerator / denominator);
        }
    }

    public Map<Integer, List<Double>> fcm() {
        double maxDiff;
        int step = 0;
        do {
            step++;
            calculateCentreVectors();
            maxDiff = updateDegreeOfMembership();
            System.out.printf("Step: %s of %s; Function: %s %n", step, maxIterationCount, maxDiff);
        } while (maxDiff > epsilon && step < maxIterationCount);
        Map<Integer, List<Double>> result = new LinkedHashMap<>();
        int i, j;
        List<Double> degrees = new ArrayList<>();
        for (i = 0; i < dataPoints.size(); i++) {
            degrees.clear();
            for (j = 0; j < numClusters; j++) {
                degrees.add(degreeOfMembership[i][j]);
            }
            result.put(dataPoints.get(i), new ArrayList<>(degrees));
        }
        return result;
    }
}
