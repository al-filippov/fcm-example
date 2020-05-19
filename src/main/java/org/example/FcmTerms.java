package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class FcmTerms {
    private int numClusters;
    private final double degreeOfMemberhip[][];
    private double epsilon;
    private double fuzziness;
    private int maxIterationCount;
    private final List<Map<String, Double>> clusterCentre;
    private List<Map<String, Double>> dataPoints;

    public FcmTerms(final List<List<Term>> dataPoints,
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
        this.dataPoints = dataPoints.stream()
                .map(doc -> doc.stream()
                        .collect(Collectors.toMap(Term::getName, Term::getFrequency)))
                .collect(Collectors.toList());
        this.degreeOfMemberhip = new double[dataPoints.size()][numClusters];
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
                degreeOfMemberhip[i][j] = randomValue / 100.0;
                degree += degreeOfMemberhip[i][j];
            }
            degreeOfMemberhip[i][0] = 1.0 - Math.min(degree, 1.0);
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
                diff = newUij - degreeOfMemberhip[i][j];
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                degreeOfMemberhip[i][j] = newUij;
            }
        }
        return maxDiff;
    }

    private double getNormalization(int i, int j) {
        final Map<String, Double> terms = dataPoints.get(i);
        double distance = clusterCentre.get(j).entrySet()
                .stream()
                .mapToDouble(term -> Math.pow(
                        Optional.ofNullable(terms.get(term.getKey())).orElse(0d) - term.getValue(), 2))
                .sum();
        return Math.sqrt(distance);
    }

    private void calculateCentreVectors() {
        int i, j;
        double fuzzyDegreeOfMembership[][] = new double[dataPoints.size()][numClusters];
        for (i = 0; i < dataPoints.size(); i++) {
            for (j = 0; j < numClusters; j++) {
                fuzzyDegreeOfMembership[i][j] = Math.pow(degreeOfMemberhip[i][j], fuzziness);
            }
        }
        for (j = 0; j < clusterCentre.size(); j++) {
            double denominator = 0.0;
            final Map<String, Double> numerators = new HashMap<>();
            final Map<String, Double> currentCentre = Optional
                    .ofNullable(clusterCentre.get(j))
                    .orElse(new HashMap<>());
            if (clusterCentre.get(j) == null) {
                clusterCentre.set(j, currentCentre);
            }
            for (i = 0; i < dataPoints.size(); i++) {
                final Map<String, Double> terms = dataPoints.get(i);
                denominator += fuzzyDegreeOfMembership[i][j];
                for (String term : terms.keySet()) {
                    double numerator = fuzzyDegreeOfMembership[i][j] *
                            Optional.ofNullable(terms.get(term)).orElse(0d);
                    numerators.put(term, numerators.computeIfAbsent(term, (k) -> 0.0) + numerator);
                }
            }
            for (String term : numerators.keySet()) {
                currentCentre.put(term, denominator == 0 ? 0.0 : numerators.get(term) / denominator);
            }
        }
    }

    public Map<List<Term>, List<Double>> fcm() {
        double maxDiff;
        int step = 0;
        do {
            step++;
            calculateCentreVectors();
            maxDiff = updateDegreeOfMembership();
            System.out.printf("Step: %s of %s; Function: %s %n", step, maxIterationCount, maxDiff);
        } while (maxDiff > epsilon && step < maxIterationCount);
        Map<List<Term>, List<Double>> result = new LinkedHashMap<>();
        int i, j;
        List<Double> degrees = new ArrayList<>();
        for (i = 0; i < dataPoints.size(); i++) {
            degrees.clear();
            for (j = 0; j < numClusters; j++) {
                degrees.add(degreeOfMemberhip[i][j]);
            }
            result.put(dataPoints.get(i).entrySet()
                            .stream()
                            .map(entry -> new Term(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList()),
                    new ArrayList<>(degrees));
        }
        return result;
    }
}
