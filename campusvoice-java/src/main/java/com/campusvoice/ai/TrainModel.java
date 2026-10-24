package com.campusvoice.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TrainModel {
    private static final Logger logger = LoggerFactory.getLogger(TrainModel.class);

    public static class Sample {
        public String text;
        public String category;
        public String priority;
        public String fullFeature;

        public Sample(String text, String category, String priority) {
            this.text = text;
            this.category = category;
            this.priority = priority;
            this.fullFeature = (category != null ? category : "") + " " + (text != null ? text : "");
        }
    }

    public static class TrainedAIModel {
        private final TfidfVectorizer vectorizer;
        private final TextClassifier classifier;
        private final double trainingAccuracy;
        private final int sampleCount;

        public TrainedAIModel(TfidfVectorizer vectorizer, TextClassifier classifier, double trainingAccuracy, int sampleCount) {
            this.vectorizer = vectorizer;
            this.classifier = classifier;
            this.trainingAccuracy = trainingAccuracy;
            this.sampleCount = sampleCount;
        }

        public TfidfVectorizer getVectorizer() { return vectorizer; }
        public TextClassifier getClassifier() { return classifier; }
        public double getTrainingAccuracy() { return trainingAccuracy; }
        public int getSampleCount() { return sampleCount; }
    }

    public static List<Sample> loadDataset() {
        List<Sample> samples = new ArrayList<>();

        // 1. Try reading from classpath resource
        try (InputStream is = TrainModel.class.getResourceAsStream("/ai/dataset.csv")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    samples = parseCsv(reader);
                    if (!samples.isEmpty()) {
                        logger.info("Loaded {} samples from classpath resource /ai/dataset.csv", samples.size());
                        return samples;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read /ai/dataset.csv from classpath: {}", e.getMessage());
        }

        // 2. Fallback to filesystem
        File[] candidateFiles = new File[]{
            new File("src/main/resources/ai/dataset.csv"),
            new File("../campusvoice-ai/training/dataset.csv"),
            new File("campusvoice-ai/training/dataset.csv"),
            new File("training/dataset.csv")
        };

        for (File f : candidateFiles) {
            if (f.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                    samples = parseCsv(reader);
                    if (!samples.isEmpty()) {
                        logger.info("Loaded {} samples from file {}", samples.size(), f.getAbsolutePath());
                        return samples;
                    }
                } catch (Exception e) {
                    logger.warn("Error reading {}: {}", f.getPath(), e.getMessage());
                }
            }
        }

        return samples;
    }

    private static List<Sample> parseCsv(BufferedReader reader) throws Exception {
        List<Sample> list = new ArrayList<>();
        String line = reader.readLine(); // Header line

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Handle quoted CSV values
            List<String> cols = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder sb = new StringBuilder();

            for (char c : line.toCharArray()) {
                if (c == '\"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    cols.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            cols.add(sb.toString().trim());

            if (cols.size() >= 3) {
                String text = cols.get(0);
                String category = cols.get(1);
                String priority = cols.get(2).toUpperCase();
                list.add(new Sample(text, category, priority));
            }
        }
        return list;
    }

    public static TrainedAIModel trainPriorityModel() {
        List<Sample> samples = loadDataset();
        if (samples.isEmpty()) {
            logger.warn("Dataset is empty! Creating fallback model.");
            return new TrainedAIModel(new TfidfVectorizer(), new TextClassifier(), 0.0, 0);
        }

        List<String> docTexts = new ArrayList<>();
        String[] labels = new String[samples.size()];

        for (int i = 0; i < samples.size(); i++) {
            Sample s = samples.get(i);
            docTexts.add(s.fullFeature);
            labels[i] = s.priority;
        }

        TfidfVectorizer vectorizer = new TfidfVectorizer();
        vectorizer.fit(docTexts);

        double[][] X = new double[samples.size()][];
        for (int i = 0; i < samples.size(); i++) {
            X[i] = vectorizer.transform(samples.get(i).fullFeature);
        }

        TextClassifier classifier = new TextClassifier();
        classifier.fit(X, labels);

        // Evaluate train accuracy
        int correct = 0;
        for (int i = 0; i < samples.size(); i++) {
            TextClassifier.PredictionResult pred = classifier.predict(X[i]);
            if (pred.getPriority().equalsIgnoreCase(labels[i])) {
                correct++;
            }
        }

        double accuracy = Math.round((correct / (double) samples.size()) * 1000.0) / 10.0;
        logger.info("Java AI Model Training Complete. Samples: {}, Vocabulary: {}, Accuracy: {}%",
                samples.size(), vectorizer.getVocabularySize(), accuracy);

        return new TrainedAIModel(vectorizer, classifier, accuracy, samples.size());
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CampusVoice AI Model Training (Java Native)    ");
        System.out.println("==================================================");

        TrainedAIModel model = trainPriorityModel();

        System.out.println("Samples Loaded     : " + model.getSampleCount());
        System.out.println("Vocabulary Features: " + model.getVectorizer().getVocabularySize());
        System.out.println("Training Accuracy  : " + model.getTrainingAccuracy() + "%");
        System.out.println("Classes Supported  : " + model.getClassifier().getClasses());
        System.out.println("==================================================");
        System.out.println("Model successfully trained and ready for inference!");
    }
}
