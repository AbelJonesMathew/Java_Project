package com.campusvoice.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExportTrainingData {
    private static final Logger logger = LoggerFactory.getLogger(ExportTrainingData.class);

    public static class VerifiedRecord {
        public String text;
        public String category;
        public String priority;
        public String language;

        public VerifiedRecord(String text, String category, String priority, String language) {
            this.text = text;
            this.category = category;
            this.priority = priority;
            this.language = language;
        }
    }

    public static File exportHumanReviewedData() {
        Path exportDir = Paths.get("training");
        try {
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
        } catch (Exception ignored) {}

        File exportFile = exportDir.resolve("approved_training_data.csv").toFile();
        logger.info("Exporting human-reviewed training data to {}", exportFile.getAbsolutePath());

        List<VerifiedRecord> sampleRecords = new ArrayList<>();
        sampleRecords.add(new VerifiedRecord("Exposed wire in lab 3", "Electricity", "HIGH", "English"));
        sampleRecords.add(new VerifiedRecord("Classroom projector flickering", "Academic", "MEDIUM", "English"));
        sampleRecords.add(new VerifiedRecord("Water dispenser needs filter replacement", "Water", "LOW", "English"));
        sampleRecords.add(new VerifiedRecord("Hostel WiFi router dead", "Internet/WiFi", "MEDIUM", "English"));
        sampleRecords.add(new VerifiedRecord("Gas cylinder valve loose in canteen kitchen", "Food/Canteen", "HIGH", "English"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile, StandardCharsets.UTF_8))) {
            writer.write("text,category,priority,language\n");
            for (VerifiedRecord r : sampleRecords) {
                writer.write(String.format("\"%s\",%s,%s,%s\n",
                        r.text.replace("\"", "\"\""), r.category, r.priority, r.language));
            }
            logger.info("Successfully exported {} verified training samples.", sampleRecords.size());
            return exportFile;
        } catch (Exception e) {
            logger.error("Failed to export training data: {}", e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Exporting human-reviewed priority data to CSV...");
        File f = exportHumanReviewedData();
        if (f != null) {
            System.out.println("Successfully exported verified training data to: " + f.getAbsolutePath());
        } else {
            System.err.println("Export failed.");
        }
    }
}
