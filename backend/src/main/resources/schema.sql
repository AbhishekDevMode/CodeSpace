CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    repo_url VARCHAR(500),
    repo_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'processing',
    error_message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS code_files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT,
    file_path VARCHAR(500),
    content LONGTEXT,
    language VARCHAR(50),
    ast_data JSON,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS generated_docs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_id INT,
    documentation LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES code_files(id) ON DELETE CASCADE
);
