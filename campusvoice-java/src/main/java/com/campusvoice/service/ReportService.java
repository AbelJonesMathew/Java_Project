package com.campusvoice.service;

import com.campusvoice.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    public File generateCSVReport(List<Complaint> complaints, String reportName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(reportName + "_" + timeStamp + ".csv");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ID,Code,Category,Title,Priority,Status,Language,Student,AssignedTo,CreatedAt\n");
            for (Complaint c : complaints) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    c.getId(),
                    escapeCsv(c.getComplaintCode()),
                    escapeCsv(c.getCategoryName()),
                    escapeCsv(c.getTitle()),
                    c.getPriority(),
                    c.getStatus(),
                    c.getOriginalLanguage(),
                    escapeCsv(c.getStudentName()),
                    escapeCsv(c.getAssignedToName()),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
                ));
            }
            logger.info("CSV Report generated successfully: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            logger.error("Error generating CSV report", e);
            return null;
        }
    }

    public File generateTextSummaryReport(List<Complaint> complaints, String reportTitle) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(reportTitle.replaceAll(" ", "_") + "_" + timeStamp + ".txt");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("=================================================================\n");
            writer.write("                    CAMPUSVOICE EXECUTIVE REPORT                 \n");
            writer.write("                'Speak • Report • Get It Solved'                 \n");
            writer.write("=================================================================\n\n");
            writer.write("Report Title: " + reportTitle + "\n");
            writer.write("Generated Date: " + new Date().toString() + "\n");
            writer.write("Total Complaints Analyzed: " + complaints.size() + "\n\n");

            long highCount = complaints.stream().filter(c -> "HIGH".equals(c.getPriority())).count();
            long medCount = complaints.stream().filter(c -> "MEDIUM".equals(c.getPriority())).count();
            long lowCount = complaints.stream().filter(c -> "LOW".equals(c.getPriority())).count();
            long resolvedCount = complaints.stream().filter(c -> "RESOLVED".equals(c.getStatus())).count();

            writer.write("SUMMARY METRICS:\n");
            writer.write("-----------------------------------------------------------------\n");
            writer.write("High Priority: " + highCount + "\n");
            writer.write("Medium Priority: " + medCount + "\n");
            writer.write("Low Priority: " + lowCount + "\n");
            writer.write("Resolved Issues: " + resolvedCount + "\n");
            writer.write("Resolution Rate: " + (complaints.isEmpty() ? "0%" : String.format("%.1f%%", (double)resolvedCount / complaints.size() * 100)) + "\n\n");

            writer.write("COMPLAINT DETAILS:\n");
            writer.write("-----------------------------------------------------------------\n");
            for (Complaint c : complaints) {
                writer.write(String.format("[%s] %s | Priority: %s | Status: %s | Category: %s\nTitle: %s\nAI Reason: %s\n\n",
                    c.getComplaintCode(),
                    c.getOriginalLanguage(),
                    c.getPriority(),
                    c.getStatus(),
                    c.getCategoryName(),
                    c.getTitle(),
                    c.getAiReason()
                ));
            }
            logger.info("Summary Report generated successfully: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            logger.error("Error generating text summary report", e);
            return null;
        }
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
