# ENSAT-Management — agent guidelines

This file describes THIS repository and takes precedence over any AGENTS.md in parent folders
(the one in the user's home folder is about a different, Jakarta EE / Hibernate project).

## Stack

- Java 17, JavaFX 17 (FXML + CSS), plain JDBC (no ORM), PostgreSQL, Maven (use the `mvnw` wrapper).
- Exam mini-project; the brief is `docs/sujet-mini-projet-javafx-jdbc.pdf` (business rules are mandatory).

## Layout

- `src/main/java/com/example/ma_exam/` — `model`, `dao`, `controller`, `util`, `MainApp.java`.
- `src/main/resources/com/example/ma_exam/view/` — FXML views and `style.css`.
- `schema.sql` — database schema. `config.properties` (git-ignored, see `config.properties.example`) — credentials.

## Commands

- Build + tests: `./mvnw clean test`
- Run: `./mvnw clean javafx:run`

## Conventions

- DAOs get the shared connection from `DBConnection.getConnection()` and must NOT close it; use
  `DBConnection.inTransaction(...)` for multi-statement work. Always use `PreparedStatement`.
- Controllers run DB work through `Async.supply/run` (never on the JavaFX thread), validate input with
  `Validator`, and report errors with `Alerts`. Business-rule violations throw `BusinessRuleException`.
- UI text is in French.
