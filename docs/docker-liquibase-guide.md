# Guide: Local Development with Docker Compose & Database Migrations (Liquibase)

This document is a reference guide on how to migrate your local development setup from Spring's automatic DDL generation (`spring.jpa.hibernate.ddl-auto=update`) to a production-grade database schema migration workflow using **Docker Compose** and **Liquibase**.

---

## 🐳 Step 1: Running PostgreSQL with Docker Compose

Using a `docker-compose.yml` file allows you to spin up your database, cache, or message queues with a single command: `docker compose up -d`.

### 1. Create a `docker-compose.yml` file
Create this file in the root directory of your project:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: goodfiction-db
    environment:
      POSTGRES_DB: goodfiction
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: mysecretpassword
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

### 2. Commands to Manage Docker
*   **Start the DB**: `docker compose up -d` (runs in the background).
*   **Check logs**: `docker compose logs -f postgres`.
*   **Stop the DB**: `docker compose down` (retains data in the volume).
*   **Stop and wipe data**: `docker compose down -v` (removes the volume and resets the DB).

---

## 🗄️ Step 2: Database Version Control with Liquibase

Instead of letting Hibernate dynamically generate/alter your tables (which is risky for production and makes changes hard to track), we use **Liquibase** to version control database schemas.

### 1. Add Liquibase Dependency to `pom.xml`
Add the Liquibase core starter dependency under `<dependencies>`:

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### 2. Configure Spring Boot to use Liquibase
Update your `application.properties` (or `application-dev.properties`):

```properties
# Disable Hibernate DDL auto-generation (important!)
spring.jpa.hibernate.ddl-auto=validate

# Enable Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### 3. Set Up Liquibase Changelog Files
Liquibase tracks changes using XML, YAML, JSON, or SQL format. 
Create the directory structure: `src/main/resources/db/changelog/`

#### Create the Master Changelog: `db.changelog-master.xml`
This file acts as the entry point and includes other change log files:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <!-- Include changesets chronologically -->
    <include file="db/changelog/changes/001-create-initial-schema.sql" relativeToChangelogFile="false"/>
</databaseChangeLog>
```

#### Create the SQL Schema Changeset: `changes/001-create-initial-schema.sql`
Write standard PostgreSQL DDL in this file:

```sql
-- liquibase formatted sql

-- changeset neroforte:1
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(255),
    is_profile_public BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- changeset neroforte:2
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    google_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) UNIQUE,
    thumbnail_url VARCHAR(2000),
    published_date VARCHAR(255),
    description TEXT,
    page_count INTEGER
);
```

---

## 🔄 How it Works Together

1.  You run `docker compose up -d` to start the PostgreSQL instance.
2.  You run your Spring Boot application (`mvn spring-boot:run` or via your IDE).
3.  During startup, Spring Boot detects Liquibase.
4.  Liquibase reads your `db.changelog-master.xml` and checks the database table `databasechangelog` (which Liquibase creates automatically to track history).
5.  If a changeset has not been run, Liquibase executes the SQL, records the execution, and finishes starting your app.
6.  If you add new tables or modify columns, you just append a new changeset file, reference it in `db.changelog-master.xml`, and Spring Boot executes it on the next run!
