package com.campusvoice.service;

import com.campusvoice.model.AIAnalysis;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PriorityServiceTest {

    @Test
    public void testElectricalHazardPriorityDetection() {
        PriorityService service = new PriorityService();
        AIAnalysis analysis = service.analyzePriority("Exposed electrical wire in computer lab is sparking and could cause electric shock", "Electricity", "English");

        assertNotNull(analysis);
        assertEquals("HIGH", analysis.getFinalPriority());
        assertTrue(analysis.getConfidence() >= 90.0);
        assertNotNull(analysis.getReason());
    }

    @Test
    public void testLowPriorityLibraryRequest() {
        PriorityService service = new PriorityService();
        AIAnalysis analysis = service.analyzePriority("Need more chairs in central library reading section", "Library", "English");

        assertNotNull(analysis);
        assertNotEquals("HIGH", analysis.getFinalPriority());
    }
}
