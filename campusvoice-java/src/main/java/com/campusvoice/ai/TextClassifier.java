package com.campusvoice.ai;

import java.io.Serializable;
import java.util.*;

public class TextClassifier implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class PredictionResult {
        private final String priority;
        private final double confidence;
        private final Map<String, Double> classProbabilities;

        public PredictionResult(String priority, double confidence, Map<String, Double> classProbabilities) {
            this.priority = priority;
            this.confidence = confidence;
            this.classProbabilities = classProbabilities;
        }

        public String getPriority() { return priority; }
        public double getConfidence() { return confidence; }
        public Map<String, Double> getClassProbabilities() { return classProbabilities; }
    }

    private final List<String> classes = Arrays.asList("HIGH", "MEDIUM", "LOW");
    private final Map<String, double[]> classCentroids = new HashMap<>();
    private final Map<String, Integer> classCounts = new HashMap<>();
    private int vectorLength = 0;

    public void fit(double[][] X, String[] y) {
        if (X == null || X.length == 0 || y == null || y.length == 0) {
            return;
        }

        vectorLength = X[0].length;
        classCentroids.clear();
        classCounts.clear();

        for (String c : classes) {
            classCentroids.put(c, new double[vectorLength]);
            classCounts.put(c, 0);
        }

        // Accumulate vectors per class
        for (int i = 0; i < X.length; i++) {
            String label = y[i];
            if (classCentroids.containsKey(label)) {
                double[] centroid = classCentroids.get(label);
                for (int j = 0; j < vectorLength; j++) {
                    centroid[j] += X[i][j];
                }
                classCounts.put(label, classCounts.get(label) + 1);
            }
        }

        // Normalize each centroid to unit length
        for (String c : classes) {
            double[] centroid = classCentroids.get(c);
            int count = classCounts.get(c);
            if (count > 0) {
                double sumSq = 0.0;
                for (int j = 0; j < vectorLength; j++) {
                    centroid[j] /= count;
                    sumSq += centroid[j] * centroid[j];
                }
                if (sumSq > 0.0) {
                    double norm = Math.sqrt(sumSq);
                    for (int j = 0; j < vectorLength; j++) {
                        centroid[j] /= norm;
                    }
                }
            }
        }
    }

    public PredictionResult predict(double[] x) {
        if (classCentroids.isEmpty() || vectorLength == 0 || x.length != vectorLength) {
            Map<String, Double> fallback = new HashMap<>();
            fallback.put("HIGH", 0.1);
            fallback.put("MEDIUM", 0.8);
            fallback.put("LOW", 0.1);
            return new PredictionResult("MEDIUM", 80.0, fallback);
        }

        Map<String, Double> rawScores = new HashMap<>();
        double maxScore = -Double.MAX_VALUE;

        // Cosine similarity between input vector and class centroids
        for (String c : classes) {
            double[] centroid = classCentroids.get(c);
            double dotProduct = 0.0;
            for (int i = 0; i < vectorLength; i++) {
                dotProduct += x[i] * centroid[i];
            }
            // Scale temperature for well-calibrated probabilities
            double scaledScore = dotProduct * 5.0;
            rawScores.put(c, scaledScore);
            if (scaledScore > maxScore) {
                maxScore = scaledScore;
            }
        }

        // Softmax
        double sumExp = 0.0;
        Map<String, Double> expScores = new HashMap<>();
        for (String c : classes) {
            double exp = Math.exp(rawScores.get(c) - maxScore);
            expScores.put(c, exp);
            sumExp += exp;
        }

        String bestClass = "MEDIUM";
        double highestProb = 0.0;
        Map<String, Double> probs = new HashMap<>();

        for (String c : classes) {
            double p = sumExp > 0.0 ? (expScores.get(c) / sumExp) : 0.333;
            probs.put(c, p);
            if (p > highestProb) {
                highestProb = p;
                bestClass = c;
            }
        }

        double confidenceScore = Math.round(highestProb * 1000.0) / 10.0; // Round to 1 decimal
        return new PredictionResult(bestClass, confidenceScore, probs);
    }

    public List<String> getClasses() {
        return Collections.unmodifiableList(classes);
    }
}
