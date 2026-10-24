package com.campusvoice.ai;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     Running CampusVoice AI Java Test Suite       ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        AIServiceTest.setUp();

        // Test 1: Health check
        try {
            AIServiceTest test = new AIServiceTest();
            test.testHealthCheck();
            System.out.println("✅ [PASS] testHealthCheck");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testHealthCheck: " + t.getMessage());
            failed++;
        }

        // Test 2: Electrical Hazard Override
        try {
            AIServiceTest test = new AIServiceTest();
            test.testElectricalHazardOverride();
            System.out.println("✅ [PASS] testElectricalHazardOverride (Safety Override Verified)");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testElectricalHazardOverride: " + t.getMessage());
            failed++;
        }

        // Test 3: Language Detection Malayalam
        try {
            AIServiceTest test = new AIServiceTest();
            test.testLanguageDetectionMalayalam();
            System.out.println("✅ [PASS] testLanguageDetectionMalayalam (Script Range Verified)");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testLanguageDetectionMalayalam: " + t.getMessage());
            failed++;
        }

        // Test 4: Translation Malayalam
        try {
            AIServiceTest test = new AIServiceTest();
            test.testTranslationMalayalam();
            System.out.println("✅ [PASS] testTranslationMalayalam (Glossary & Translation Verified)");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testTranslationMalayalam: " + t.getMessage());
            failed++;
        }

        // Test 5: Hindi Language Detection and Translation
        try {
            AIServiceTest test = new AIServiceTest();
            test.testHindiLanguageDetectionAndTranslation();
            System.out.println("✅ [PASS] testHindiLanguageDetectionAndTranslation");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testHindiLanguageDetectionAndTranslation: " + t.getMessage());
            failed++;
        }

        // Test 6: Tamil Language Detection
        try {
            AIServiceTest test = new AIServiceTest();
            test.testTamilLanguageDetection();
            System.out.println("✅ [PASS] testTamilLanguageDetection");
            passed++;
        } catch (Throwable t) {
            System.err.println("❌ [FAIL] testTamilLanguageDetection: " + t.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("Test Results: " + passed + " Passed, " + failed + " Failed.");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
