-- File payloads accompany complaint_attachments metadata in the same shared database.
-- Applied automatically by v1.0.3; administrators can also run this migration ahead of time.
CREATE TABLE IF NOT EXISTS complaint_attachment_data (
    attachment_id VARCHAR(50) PRIMARY KEY,
    file_content BYTEA NOT NULL
);
