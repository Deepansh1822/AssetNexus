# Separation of Backend and Frontend

## Objective
To separate the backend Spring Boot code from the frontend HTML/CSS/JS code into distinct directories within the project root. This structure allows for cleaner separation of concerns and facilitates independent development workflows.

## Proposed Strategy
1.  **Stop Running Application**: Ensure the Spring Boot application (port 8085) is fully stopped to release file locks.
2.  **Create Directory Structure**:
    - `backend/`: Will contain the Spring Boot application source code, Maven configuration, and build artifacts.
    - `frontend/`: Will contain the Thymeleaf templates (`templates/`) and static assets (`static/` via CSS/JS).
3.  **Move Files**:
    - Move `src`, `pom.xml`, `.mvn`, `mvnw`, `mvnw.cmd`, `target`, and IDE configuration files (`.project`, `.classpath`, etc.) to the `backend/` directory.
    - Move `templates` and `static` directories from `backend/src/main/resources/` to the `frontend/` directory.
4.  **Configure Application**:
    - Update `backend/src/main/resources/application.properties` to point to the new location of frontend resources using `file:../frontend/...`.
5.  **Clean & Re-Import**:
    - The user will need to re-import the project in their IDE (Eclipse/IntelliJ) as a Maven project from the `backend/` directory.
    - To run the application, use `mvn spring-boot:run` from the `backend/` directory.

## Expected Directory Structure After Changes
```
AssetManagement/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ (Backend Logic)
│   │   │   └── resources/
│   │   │       └── application.properties
│   ├── pom.xml
│   ├── mvnw
│   └── target/
└── frontend/
    ├── templates/ (HTML)
    └── static/ (CSS, JS)
```

## Benefits
- **Clear Separation**: Backend logic is distinct from UI presentation.
- **Independent Frontend Work**: Frontend developers can work on `frontend/` without needing to navigate backend Java code.
- **Modern Structure**: Aligns closer to modern full-stack project structures.
