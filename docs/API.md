# CampusVoice AI Microservice API Specification

Base URL: `http://localhost:8000`

---

## Endpoints

### 1. Health Check
`GET /ai/health`

**Response:**
```json
{
  "status": "healthy",
  "service": "CampusVoice AI Service",
  "version": "1.0.0",
  "model_loaded": true
}
```

---

### 2. Priority Analysis
`POST /ai/analyze-priority`

**Request Body:**
```json
{
  "text": "The electrical wire in the lab is exposed and students could get an electric shock.",
  "category": "Electricity",
  "language": "English"
}
```

**Response:**
```json
{
  "priority": "HIGH",
  "confidence": 96.0,
  "reason": "Potential safety hazard detected. The complaint mentions exposed electrical wiring which can cause electric shock and affects multiple students.",
  "detected_language": "English",
  "translated_text": "The electrical wire in the lab is exposed and students could get an electric shock.",
  "risk_factors": [
    "Electrical hazard",
    "Student safety risk",
    "Multiple people affected"
  ],
  "human_review_required": false
}
```

---

### 3. Language Detection
`POST /ai/detect-language`

**Request Body:**
```json
{
  "text": "ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല."
}
```

**Response:**
```json
{
  "language_code": "ml",
  "language_name": "Malayalam",
  "confidence": 0.99
}
```

---

### 4. Translation
`POST /ai/translate`

**Request Body:**
```json
{
  "text": "ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.",
  "source_language": "Malayalam",
  "target_language": "English"
}
```

**Response:**
```json
{
  "original_text": "ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.",
  "translated_text": "The projector in the classroom is not working.",
  "source_language": "Malayalam",
  "target_language": "English"
}
```
