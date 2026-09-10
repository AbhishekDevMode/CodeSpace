# DevDocs (CodeSpace) 🚀

> **AI-Powered Code Documentation Generator & Interactive Architecture Knowledge Graph**

DevDocs is an intelligent developer platform that ingests codebases from GitHub repositories or uploaded ZIP archives, parses their Abstract Syntax Trees (AST) using ANTLR4 across multiple programming languages, maps code relationships into a Neo4j graph database, visualizes the architecture interactively with Cytoscape.js, and leverages Google Gemini AI to generate documentation and provide a contextual code copilot.

---

## 📑 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Option 1: Quickstart with Docker Compose (Recommended)](#option-1-quickstart-with-docker-compose-recommended)
  - [Option 2: Local Manual Setup](#option-2-local-manual-setup)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Contributing](#contributing)
- [License](#license)

---

## 🌟 Overview

Understanding large, unfamiliar codebases or generating comprehensive technical documentation is often tedious and time-consuming. **DevDocs** automates this end-to-end:

1. **Ingestion**: Clone public GitHub repositories via high-speed zipball downloads (with shallow Git clone fallback) or upload `.zip` archives directly.
2. **AST Code Parsing**: Parse Java, Python, and JavaScript source files using ANTLR4 grammars to extract classes, methods, imports, fields, and invocation calls.
3. **Graph Knowledge Base**: Store code files and structural connections (`IMPORTS`, `CALLS`) as nodes and edges in Neo4j.
4. **Interactive Graph UI**: Render interactive dependency maps using Cytoscape.js with zoom, pan, layout resets, and language-coded filtering.
5. **AI Documentation & Assistant**: Generate Javadoc and technical summaries with Google Gemini AI (`gemini-3.5-flash`), and converse with an embedded AI assistant about any file or the overall project.

---

## ✨ Key Features

- **Multi-Source Ingestion**: Ingest code from GitHub URLs (`https://github.com/owner/repo`) or upload local ZIP archives.
- **Polyglot AST Parsing**: ANTLR4 runtime visitors parse Java, Python, and JavaScript to inspect language constructs without executing code.
- **Neo4j Graph Mapping**: Model code entities as `FileNode` instances, tracking directed dependencies (`IMPORTS`) and function calls (`CALLS`).
- **Interactive Architecture Graph**: Explore file dependencies through a Cytoscape.js canvas featuring automatic force-directed graph layouts (`cose`), zoom controls, and file selection.
- **Automated AI Documentation**: One-click generation of technical documentation and docstrings for any code file in the repository.
- **Context-Aware AI Copilot**: Dedicated chat interface that feeds the selected code context into Gemini to explain code, suggest optimizations, and trace dependencies.
- **Secure JWT Authentication**: User signup, login, and protected REST endpoints backed by Spring Security and JWT tokens.
- **Dockerized Architecture**: Complete multi-container deployment ready with Docker Compose for MySQL, Neo4j, Spring Boot backend, and Nginx-served frontend.

---

## 🏗️ System Architecture

```text
               +-------------------------------------------+
               |        Browser / React 19 Frontend        |
               | (Cytoscape.js, Tailwind CSS, Lucide, Vite) |
               +-------------------------------------------+
                                     |
                         HTTP / REST API (JWT)
                                     v
               +-------------------------------------------+
               |         Spring Boot 3.3 Backend           |
               |  - AuthController & JWT Security          |
               |  - ProjectController & Ingestion          |
               |  - CodeParserService (ANTLR4 AST Engine)  |
               |  - GraphService & AI Router (Gemini)      |
               +-------------------------------------------+
                     /                 \                 \
                    /                   \                 \
                   v                     v                 v
        +-------------------+  +-------------------+  +------------------+
        |   MySQL 8.0 DB    |  |  Neo4j 5.12 Graph |  | Google Gemini AI |
        | (Users, Projects, |  | (FileNodes, Calls,|  | (Doc Generation, |
        |  Files, Metadata) |  |  Import Relations)|  |  Chat Assistant) |
        +-------------------+  +-------------------+  +------------------+
```

---

## 🛠️ Tech Stack

### Frontend
- **Framework**: [React 19](https://react.dev/) + [Vite](https://vitejs.dev/)
- **Styling**: [Tailwind CSS v4](https://tailwindcss.com/)
- **Graph Visualization**: [Cytoscape.js](https://js.cytoscape.org/) & `react-cytoscapejs`
- **Routing**: [React Router v6](https://reactrouter.com/)
- **HTTP Client**: [Axios](https://axios-http.com/) (with JWT request/response interceptors)
- **Icons**: [Lucide React](https://lucide.dev/)
- **Server / Proxy**: [Nginx](https://nginx.org/) (Production container)

### Backend
- **Framework**: [Spring Boot 3.3.4](https://spring.io/projects/spring-boot) (Java 21)
- **Security**: Spring Security 6 + [JJWT (0.11.5)](https://github.com/jwtk/jjwt)
- **Data Access**: Spring Data JPA (Hibernate) & Spring Data Neo4j
- **AST Parsing**: [ANTLR4 Runtime 4.13.1](https://www.antlr.org/) (Java, JavaScript, Python3 grammars)
- **AI Integration**: Spring AI (`spring-ai-core 1.0.0-M1`) with Google Gemini (`gemini-3.5-flash`) and Groq fallback

### Databases & Infrastructure
- **Relational DB**: [MySQL 8.0](https://www.mysql.com/)
- **Graph DB**: [Neo4j 5.12](https://neo4j.com/)
- **Containerization**: [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)

---

## 📂 Project Structure

```text
CodeSpace/
├── docker-compose.yml              # Multi-container orchestration (MySQL, Neo4j, Backend, Frontend)
├── backend/
│   ├── Dockerfile                  # Multi-stage build (Maven 3.9 + Eclipse Temurin 21 JRE)
│   ├── pom.xml                     # Maven dependencies (Spring Boot, ANTLR4, Spring AI, Neo4j)
│   └── src/
│       └── main/
│           ├── java/com/email/backend/
│           │   ├── BackendApplication.java
│           │   ├── config/         # SecurityConfig, GlobalExceptionHandler
│           │   ├── controller/     # AuthController, ProjectController, GraphController, AIController
│           │   ├── dto/            # AuthResponse, LoginRequest, ProjectRequest, etc.
│           │   ├── model/          # User, Project, CodeFile, GeneratedDoc, FileNode
│           │   ├── parser/         # ANTLR generated Lexers, Parsers, and Visitors (Java, JS, Python)
│           │   ├── repository/     # JPA and Neo4j Repositories
│           │   ├── security/       # JWT Token Provider, Auth Filter, CustomUserDetailsService
│           │   └── service/        # RepoProcessingService, CodeParserService, GraphService, AI Services
│           └── resources/
│               ├── application.properties
│               └── schema.sql      # Database initialization schema
└── frontend/
    ├── Dockerfile                  # Multi-stage build (Node 20 Alpine + Nginx Alpine)
    ├── nginx.conf                  # Reverse proxy rules and SPA routing
    ├── package.json                # Frontend dependencies and scripts
    ├── vite.config.js
    └── src/
        ├── api/
        │   └── axios.js            # Axios client with JWT interceptors
        ├── components/
        │   ├── Navbar.jsx          # Top navigation & user profile
        │   ├── Login.jsx           # User authentication
        │   ├── SignUp.jsx          # Registration
        │   ├── Dashboard.jsx       # Project listing, GitHub import & ZIP upload
        │   ├── ProjectDetail.jsx   # Project workspace, documentation viewer, AST tree
        │   ├── GraphView.jsx       # Cytoscape interactive graph component
        │   └── ChatInterface.jsx   # AI Copilot conversation panel
        ├── context/
        │   └── AuthContext.jsx     # Auth state management
        ├── App.jsx                 # App routes and protected route wrapper
        └── main.jsx
```

---

## 🚀 Getting Started

### Prerequisites

- **Docker & Docker Compose** (Recommended for the easiest setup)
- *Alternatively, for manual local execution*:
  - **Java JDK 21** or later
  - **Node.js 20+** and **npm**
  - **MySQL 8.0+**
  - **Neo4j 5.x+**
  - A [Google Gemini API Key](https://aistudio.google.com/)

---

### Option 1: Quickstart with Docker Compose (Recommended)

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd CodeSpace
   ```

2. **Configure environment variables**:
   Create a `.env` file in the root directory (or set environment variables in your shell):
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   JWT_SECRET=your_super_secret_jwt_key_at_least_256_bits_long
   ```

3. **Start all services**:
   ```bash
   docker-compose up --build
   ```

4. **Access the services**:
   - **Frontend UI**: [http://localhost](http://localhost) (or [http://localhost:80](http://localhost:80))
   - **Backend API**: [http://localhost:8080](http://localhost:8080)
   - **Neo4j Browser**: [http://localhost:7474](http://localhost:7474) (Auth: `neo4j` / `password`)
   - **MySQL Database**: `localhost:3306` (User: `root`, Password: `root`, Database: `codespace`)

---

### Option 2: Local Manual Setup

#### 1. Databases Setup
Make sure MySQL and Neo4j are running locally:
- **MySQL**: Create a database named `codespace`
- **Neo4j**: Run an instance accessible at `bolt://localhost:7687` with credentials (`neo4j` / `password`)

#### 2. Backend Setup
1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Set your environment variables:
   - **Linux/macOS**:
     ```bash
     export DB_URL="jdbc:mysql://localhost:3306/codespace?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
     export DB_USERNAME="root"
     export DB_PASSWORD="root_password"
     export NEO4J_URI="bolt://localhost:7687"
     export NEO_USERNAME="neo4j"
     export NEO_PASSWORD="password"
     export JWT_SECRET="your_secure_random_jwt_secret_key_with_at_least_256_bits"
     export GEMINI_API_KEY="your_gemini_api_key"
     ```
   - **Windows (PowerShell)**:
     ```powershell
     $env:DB_URL="jdbc:mysql://localhost:3306/codespace?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
     $env:DB_USERNAME="root"
     $env:DB_PASSWORD="root_password"
     $env:NEO4J_URI="bolt://localhost:7687"
     $env:NEO_USERNAME="neo4j"
     $env:NEO_PASSWORD="password"
     $env:JWT_SECRET="your_secure_random_jwt_secret_key_with_at_least_256_bits"
     $env:GEMINI_API_KEY="your_gemini_api_key"
     ```
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows use `mvnw.cmd spring-boot:run`)*

#### 3. Frontend Setup
1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## ⚙️ Environment Variables

The backend can be configured using standard environment variables or through `backend/src/main/resources/application.properties`:

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | Backend server port | `8080` |
| `DB_URL` | JDBC connection URL for MySQL | `jdbc:mysql://localhost:3306/codespace` |
| `DB_USERNAME` | MySQL database user | `root` |
| `DB_PASSWORD` | MySQL database password | *(empty)* |
| `NEO4J_URI` | Neo4j Bolt connection URI | `bolt://localhost:7687` |
| `NEO_USERNAME` | Neo4j authentication username | `neo4j` |
| `NEO_PASSWORD` | Neo4j authentication password | *(empty)* |
| `JWT_SECRET` | Secret key for signing JWT tokens | *(Required)* |
| `GEMINI_API_KEY` | Google Gemini API key for AI features | *(Required for AI)* |
| `GEMINI_MODEL` | Gemini LLM model identifier | `gemini-3.5-flash` |
| `VITE_API_BASE_URL` | Frontend API base URL (Vite build) | `http://localhost:8080` |

---

## 📡 API Reference

### 🔐 Authentication (`/api/auth`)
- `POST /api/auth/register` (or `/signup`): Register a new user account with `email` and `password`.
- `POST /api/auth/login`: Authenticate and receive a JWT Bearer token.

### 📁 Projects (`/api/projects`)
*(Requires `Authorization: Bearer <token>`)*
- `GET /api/projects`: Retrieve all projects belonging to the authenticated user.
- `GET /api/projects/{id}`: Retrieve details, status, and code files for a specific project.
- `POST /api/projects`: Ingest a GitHub repository (`{ "repoUrl": "...", "repoName": "..." }`).
- `POST /api/projects/upload`: Ingest a local codebase by uploading a ZIP archive (`multipart/form-data`).
- `DELETE /api/projects/{id}`: Delete a project and its associated files and AST metadata.

### 🕸️ Graph & Architecture (`/api/graph`)
*(Requires `Authorization: Bearer <token>`)*
- `GET /api/graph/project/{id}`: Fetch all `FileNode` instances and dependency links for Cytoscape visualization.
- `GET /api/graph/dependencies?path={filePath}`: Fetch dependencies directly imported/called by a file.
- `GET /api/graph/dependents?path={filePath}`: Fetch files that depend on the specified file.

### 🤖 AI Services (`/api/ai`)
*(Requires `Authorization: Bearer <token>`)*
- `GET /api/ai/health`: Check status of the connected AI provider (Gemini / Groq).
- `POST /api/ai/doc`: Generate technical documentation / Javadoc for given code context.
- `POST /api/ai/explain`: Generate an in-depth explanation of a code snippet.
- `POST /api/ai/improve`: Get actionable optimization and code quality suggestions.
- `POST /api/ai/chat`: Multi-turn conversational copilot answering questions about code in context.

---

## 💡 How to Use

1. **Sign Up / Log In**: Create an account or log into your dashboard.
2. **Add a Project**:
   - Enter a public GitHub repository URL (e.g., `https://github.com/owner/repository`).
   - Or upload a `.zip` file containing source code.
3. **Background Processing**: The system downloads the repository, extracts source files, runs ANTLR4 parsers to detect classes, functions, and imports, and inserts dependency graphs into Neo4j.
4. **Inspect Architecture**: Navigate into your project to explore the interactive Cytoscape graph. Click any node to view file contents and relations.
5. **Generate Documentation & Chat**:
   - Select a node and click **Generate Documentation** to produce AI documentation.
   - Switch to the **AI Assistant** tab to ask questions, request refactoring suggestions, or trace system flow.

---

## 🤝 Contributing

Contributions are welcome! Follow these steps to contribute:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m "Add amazing feature"`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
