# 🚀 Getting Started — Developer Setup Guide

Follow this guide after cloning the repository to make sure your environment is ready and your code passes all checks.

This project uses:

- Java 21
- Maven
- Spring Boot
- PostgreSQL
- Google Java Format
- CI checks via GitHub Actions

---

## ✅ Step 1 — Install Required Tools

Make sure you have these installed:

- Java JDK 21
- Maven 3.9+
- Git
- PostgreSQL (if running database locally)
- IntelliJ IDEA or VS Code (recommended)

### Verify installation

```bash
java -version
mvn -version
git --version

mvn fmt:format  // for format code with google java format
mvn fmt:check   // to check format already or not

mvn spring-boot:run "-Dspring-boot.run.profiles=test" // to run with test




