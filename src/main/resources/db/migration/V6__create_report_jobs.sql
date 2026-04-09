CREATE TABLE report_jobs (
    id UUID PRIMARY KEY,
    report_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    file_path VARCHAR(255),
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX report_jobs_created_at_idx ON report_jobs(created_at DESC);
