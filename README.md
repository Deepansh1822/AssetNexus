# 🚀 AssetNexus - Enterprise Asset Management System (3-Tier Edition)

<div align="center">
  <img src="frontend/images/AssetNexus.png" alt="AssetNexus Logo" width="180" height="180">
  <p align="center">
    <strong>Modular. Decoupled. Enterprise-Ready.</strong>
  </p>

  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Client](https://img.shields.io/badge/Frontend-Decoupled-blue.svg)](/frontend)
  [![API](https://img.shields.io/badge/Rest-API-orange.svg)](/backend)
  [![Database](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
</div>

---

## 🏛️ Architecture: 3-Tier Decoupled
This project has been modernized from a combined Thymeleaf monolith to a professional **3-Tier Architecture**:

1.  **Presentation Tier (Frontend)**: A standalone web application using Modern JavaScript, Vanilla CSS, and a custom Layout Engine. Communicates via REST.
2.  **Logic Tier (Backend)**: A Spring Boot REST API providing data services, security (CORS/AJAX Auth), and business logic.
3.  **Data Tier (Database)**: MySQL for persistent storage of assets, employees, and maintenance logs.

---

## 🚀 Key Modernization Features
- **Decoupled Frontend**: Moved out of backend resources to the `/frontend` directory. No longer dependent on server-side rendering.
- **RESTful Integration**: Replaced Thymeleaf template logic with `fetch` API calls and JSON data exchange.
- **Global Layout Engine**: Implemented `layout.js` to dynamically manage components (Header, Sidebar, Footer) across all static pages.
- **Enhanced Security**: Updated Spring Security to handle AJAX login/logout and CORS for cross-origin frontend hosting.

---

## 🛠️ Getting Started (Decoupled Mode)

### 1. Start the Backend API
The backend now serves as a dedicated REST API.
```bash
cd backend
./mvnw spring-boot:run
```
*Port: `8085` (API Base: `http://localhost:8085/api`)*

### 2. Serve the Frontend
You can serve the frontend using any static web server. 

**Option A: VS Code Live Server**
Right-click `frontend/login.html` and select **"Open with Live Server"**. (Typically runs on port `5500`).

**Option B: Simple PHP/Node Server**
```bash
cd frontend
# If you have python
python -m http.server 5500
# If you have node
npx http-server -p 5500
```
*Note: The backend CORS settings are pre-configured to trust `http://localhost:5500`.*

---

## 📂 Project Structure
```text
AssetManagement/
├── backend/            # Spring Boot REST API
│   ├── src/main/java/  # REST Controllers, Models, Services
│   └── pom.xml
├── frontend/           # Standalone Web App
│   ├── fragments/      # Reusable HTML Components
│   ├── src/layout.js   # Client-side Layout & Auth Engine
│   ├── images/         # Static assets
│   ├── index.html      # Dashboard Page
│   └── login.html      # Identity Portal
└── old-combined-resources/ # Backup of original Thymeleaf code
```

---

<div align="center">
  Crafted with precision for 3-tier scalability by <a href="https://github.com/Deepansh1822">Deepansh</a>
</div>
