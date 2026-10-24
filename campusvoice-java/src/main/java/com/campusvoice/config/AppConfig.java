package com.campusvoice.config;

public class AppConfig {
    public static final String APP_NAME = "CampusVoice";
    public static final String TAGLINE = "Speak • Report • Get It Solved";
    public static final String SECONDARY_TAGLINE = "A Smart Complaint Management System with AI Priority Detection & Multilingual Support";
    public static final String VERSION = "1.0.1";

    // Institutional Details
    public static final String COLLEGE_NAME = "St. Joseph's College of Engineering and Technology, Palai (Autonomous)";
    public static final String COLLEGE_SHORT_NAME = "SJCET Palai";
    public static final String MANAGEMENT_NAME = "Palai Diocese";
    public static final String PRINCIPAL_NAME = "Dr. VP Deavsia";
    public static final String HOD_NAME = "Dr.Joby PP";

    // SLA Escalation Thresholds in Hours
    public static final int SLA_HIGH_HOURS = 24;
    public static final int SLA_MEDIUM_HOURS = 48;
    public static final int SLA_LOW_HOURS = 96;

    // AI Confidence Cutoff for Human Review
    public static final double AI_CONFIDENCE_THRESHOLD = 75.0;
}
