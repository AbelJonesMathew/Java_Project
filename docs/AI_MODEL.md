# CampusVoice AI Model Architecture & Safety Rules

## 1. Machine Learning Priority Classifier

CampusVoice uses a multi-stage priority classification pipeline:

1. **Preprocessing & Translation**: Text submitted in non-English languages (e.g., Malayalam, Hindi, Tamil, Telugu, Kannada) is auto-detected and translated to English.
2. **Feature Extraction**: TF-IDF (Term Frequency-Inverse Document Frequency) vectorization with n-gram range `(1, 2)`.
3. **Classification Algorithm**: Logistic Regression / Linear Support Vector Machine trained on campus complaint scenarios.
4. **Output**: Probability scores mapped to `LOW`, `MEDIUM`, or `HIGH` priority tags.

---

## 2. Rule-Based Safety Overrides

Safety hazards must **never** be misclassified due to statistical anomalies. CampusVoice enforces rule-based safety overrides before machine learning classification:

- **Critical Keywords**: `fire`, `electrical shock`, `gas leak`, `weapon`, `violence`, `ragging`, `harassment`, `medical emergency`, `collapse`, `danger`, `exposed wire`, `poison`, `flood`.
- **Action**: Any text containing these terms automatically elevates the rating to **HIGH** priority with explicit risk factor tagging.

---

## 3. Human-in-the-Loop & Model Retraining

1. HODs and Administrators can manual-override priority ratings.
2. Overridden complaints are flagged in `ai_analysis.human_overridden = TRUE`.
3. `export_training_data.py` extracts verified records into `approved_training_data.csv`.
4. Executing `python training/train_model.py` re-fits the model pipeline and updates `models/priority_classifier.pkl`.
