# Junie Guidelines for Spring Boot Development

This file contains guidelines for Junie to follow when working on this Spring Boot project.

## Core Technologies & Versions

- **Java:** Use the latest Java 24.
- **Spring Boot:** Always use the latest stable release of Spring Boot 3.5.x (or the latest major
  stable version).
- **Build Tool:** Use Gradle as the build tool. Ensure the `build.gradle` usees the latest stable
  Spring Boot package.
- **Unit test:** Use junit.
- **Log:** Use SLF4J as the logging API.
- **Swagger:** API Docs. Springdoc-openapi-starter-webmvc-ui 2.8.x (or the latest major stable
  version).
- **PostgresSQL:** Use the 16.x stable version.

## Project Structure

- **MSA Architecture**

- **Domain Driven Design**

- **Packaging:** Strongly prefer **package-by-feature** structure over package-by-layer.

    * **Why package-by-feature?** It improves modularity, makes navigating the code related to a
      single feature easier, and makes it easier to reuse code across features.

    * **Example:**

      **PREFER THIS (Package-by-Feature):** `{Domain}` is a feature.

      ```
      com.funa
      ├─ {Domain}                         # Feature: Domain
      │  ├─ {Domain}Controller.java       # Controller for Domain
      │  ├─ {Domain}Service.java          # Service logic for Domain
      │  ├─ {Domain}Repository.java       # Data access for Domain
      │  ├─ {Domain}.java                 # Domain/Entity for Domain
      │  └─ dto                           # Data Transfer Objects specific to Domain
      │     ├─ {Domain}Request.java
      │     └─ {Domain}Response.java
      └─ common                           # Optional: Truly shared utilities/conifg
         ├─ exception
         │  ├─ ValidException.java
         │  ├─ DuplicationException.java
         │  └─ NotFoundException.java
         └─ config 
      ```

      **AVOID THIS (Package-by-Layer):**
      ```
      com.funa
      ├─ controller
      │  └─ {Domain}Controller.java
      ├─ service
      │  └─ {Domain}Service.java
      ├─ repository
      │  └─ {Domain}Repository.java
      └─ model (or domain/entity)
         └─ {Domain}.java
      ```

## Java Language Features

- **Data Carriers:** Use **Lombok** for immutable data transfer objects (DTOs). value objects (VOs).

## Environment file

- The default profile is 'local' if not specified: `application.properties`
- Use 'dev' profile for development: `application-dev.properties`

## Controller

- **DO NOT use Entity:** Use the `./dto/*Request.java`, `./dto/*Response.java`.
- **Swagger:** Generate Docs in All API. Should be able to output to **Swagger.json**.

## Data Access

- **Default:** If unsure, lean towards Spring Data JPA for typical applications development.
- Default Entity properties:
    - createdAt
    - createdId
    - updatedAt
    - updatedId

## Database

- **Table naming rules:** Use **Snake case** and prefix it with 'tb_'.
- **Unit test environment test:** Use H2 Database.
- **Develop, Local environment test:** Use PostgresSQL Database.

## Logs

- **Log content:** timestamp, log level, transaction ID(Trace ID), user ID