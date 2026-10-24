package com.campusvoice;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.model.Complaint;
import com.campusvoice.service.ComplaintService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LocalDatabasePersistenceTest {
    @TempDir Path dataDir;

    @Test
    void submissionSurvivesRestartAndReachesHodInAnotherProcess() throws Exception {
        runProbe("submit");
        runProbe("read");
    }

    private void runProbe(String mode) throws Exception {
        Path output = dataDir.resolve(mode + ".log");
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Dcampusvoice.local=true", "-Dcampusvoice.dataDir=" + dataDir,
                "-Duser.home=" + dataDir,
                "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                Probe.class.getName(), mode).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(40, TimeUnit.SECONDS), "Database probe timed out");
            assertEquals(0, process.exitValue(), java.nio.file.Files.readString(output));
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    public static class Probe {
        public static void main(String[] args) throws Exception {
            ComplaintService service = new ComplaintService();
            if (args[0].equals("submit")) {
                Complaint complaint = new Complaint();
                complaint.setStudentId("55555555-5555-5555-5555-555555555555");
                complaint.setComplaintCode("#PERSISTENCE");
                complaint.setTitle("Persistent submission");
                complaint.setDescription("Visible to the HoD after restart");
                complaint.setCategoryId(8);
                if (!service.createComplaint(complaint)) throw new AssertionError("Submission failed");
                if (!service.updateStatus("#CP001", "RESOLVED", null)) throw new AssertionError("Update failed");
            } else {
                if (service.getComplaintsForDepartment(1).stream().noneMatch(c -> "#PERSISTENCE".equals(c.getComplaintCode())))
                    throw new AssertionError("HoD cannot see persisted submission");
                if (!"RESOLVED".equals(service.getComplaintByCode("#CP001").orElseThrow().getStatus()))
                    throw new AssertionError("Startup seed overwrote saved changes");
            }
            try (var connection = DatabaseConfig.getConnection(); var statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
        }
    }
}
