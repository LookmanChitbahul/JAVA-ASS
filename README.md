# SmartRetailAnalyticsSystem

Project skeleton created.

**DB Setup & Test**

- **Config file:** Edit `src/main/resources/config.properties` and set your database values:

  - `db.url=jdbc:mysql://localhost:3306/your_db_name?useSSL=false&serverTimezone=UTC`
  - `db.user=your_db_user`
  - `db.password=your_db_password`

- **Test connection:** Run the `main` in `database.DBConnection` to verify connectivity. It prints `Connected: true` on success.

- **Using Maven/IDE:** Ensure the MySQL connector dependency is present in `pom.xml` (already added). In your IDE, refresh Maven to download dependencies, then run `database.DBConnection`.

- **Command-line (if Maven installed):**

```powershell
mvn compile


BUT WE ARE NOT USING MVN
# Run with exec plugin or run compiled class from IDE
```

- **Troubleshooting:**
  - Access denied -> check MySQL user/host privileges.
  - Communications link failure -> ensure MySQL server is running and port/host are correct.
