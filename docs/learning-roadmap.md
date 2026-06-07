# Developer Growth Roadmap & Antigravity Context Guide

Welcome to your learning repository! This document serves two purposes:
1. It defines your learning goals and practice exercises (Testing, Queues, Performance, Security).
2. It acts as a persistent **context document** that Antigravity (your AI assistant) reads to understand your rules, preferences, and goals.

---

## 🎯 Part 1: Learning Goals & Practice Modules

Here is a structured path to level up your Java/Spring Boot skills using the **GoodFiction** project.

### Module 1: Testing (Highly Recommended First Step)
Since your job doesn't cover tests, this is a critical area for professional growth.
*   **Goal 1: Unit Testing (Mockito)**
    *   *Concept*: Mock dependencies, test business logic, capture arguments, verify behavior, and handle exceptions.
    *   *Practice*: Currently, `UserServiceTest` exists. We will write comprehensive unit tests for [BookService.java](file:///c:/Users/NeroForte/IdeaProjects/GoodFiction/src/main/java/com/neroforte/goodfiction/service/BookService.java) and [UserBookListService.java](file:///c:/Users/NeroForte/IdeaProjects/GoodFiction/src/main/java/com/neroforte/goodfiction/service/UserBookListService.java).
*   **Goal 2: Integration Testing with Testcontainers**
    *   *Concept*: Test components with a real database instead of H2 or mocks.
    *   *Practice*: Configure **Testcontainers** with PostgreSQL. Write integration tests for your repositories and services to verify database constraints, custom queries, and transaction rollbacks.
*   **Goal 3: API/Controller Testing (`@WebMvcTest`)**
    *   *Concept*: Test endpoints, security filters, serialization/deserialization, and validation without spinning up the whole server.
    *   *Practice*: Write slice tests for `BookController` and `UserController`.

---

### Module 2: Asynchronous Queues & Event-Driven Architecture
Queues are essential for building scalable, responsive applications.
*   **Goal 1: Spring Events & `@Async`**
    *   *Concept*: Publish internal application events to decoupling actions.
    *   *Practice*: When a user updates their reading shelf, publish a `ShelfUpdatedEvent`. Consume it asynchronously in [ActivityFeedService.java](file:///c:/Users/NeroForte/IdeaProjects/GoodFiction/src/main/java/com/neroforte/goodfiction/service/ActivityFeedService.java) to save the feed activity.
*   **Goal 2: Dedicated Message Broker (RabbitMQ or ActiveMQ)**
    *   *Concept*: Introduce a message broker for durable, retryable messaging.
    *   *Practice*: Set up RabbitMQ (using Docker) and use Spring AMQP. Instead of synchronous calls or in-memory events, publish messages to an exchange, routing them to queue consumers.
*   **Goal 3: Dead Letter Queues (DLQ) & Error Handling**
    *   *Concept*: Handle failures gracefully, routing failed messages to a DLQ for inspection.

---

### Module 3: Database Optimization (JPA/Hibernate)
*   **Goal 1: Profiling & Fixing N+1 Query Problems**
    *   *Concept*: Understand why JPA does multiple select queries and how to solve it with Entity Graphs, Join Fetches, or DTO projections.
    *   *Practice*: Trace query execution for `UserBookListService` and optimize the retrieval of lists with many books.
*   **Goal 2: Database Migrations (Flyway/Liquibase)**
    *   *Concept*: Version control your database schema.
    *   *Practice*: Integrate Flyway and write SQL migration scripts for your database.

---

## 🧠 Part 2: How to Provide Context to Antigravity

To get the most out of Antigravity, you can guide me using Markdown files. When you ask me to do a task, I scan your workspace. Having structured files tells me exactly how you want me to write code without you needing to explain it in every chat message.

### 1. Style Guide / Instructions File
You can create a file called `.antigravity-rules.md` or `docs/coding-guidelines.md` with rules like:
```markdown
# Coding Preferences
- Use constructor injection instead of `@Autowired`.
- Write unit tests using the Given/When/Then structure.
- Always use MapStruct for mapping instead of manual conversion.
- Prefer record classes for DTOs.
```
Whenever I edit code, I will respect these rules.

### 2. Task Tracking & Requirements
If you start a new practice module (e.g., adding integration tests), write the requirements in a Markdown file (e.g., `docs/tasks/integration-tests.md`) and say:
> "Antigravity, please help me implement the tasks defined in [integration-tests.md](file:///c:/Users/NeroForte/IdeaProjects/GoodFiction/docs/tasks/integration-tests.md)."

This ensures I have complete context, edge cases, and acceptance criteria.

---

## 🛠️ Part 3: Practicing in a Separate Branch

To practice a concept safely without breaking your main app, we should use a separate branch.

### Suggested First Practice: **Asynchronous Activity Feed (Spring Events)**
This is a perfect introduction to queues:
1. A user updates a book shelf (`UserBookListService`).
2. Instead of synchronously calling `ActivityFeedService.logActivity` (blocking the request), we publish a `BookActivityEvent`.
3. A listener receives the event asynchronously and logs it in the database.

### Next Steps to Begin:
1. **Choose your focus**: Let me know if you'd like to start with **Testing (Module 1)** or **Queues/Async Processing (Module 2)**.
2. **Create a branch**: We will checkout a new branch (e.g., `practice/async-events` or `practice/integration-testing`).
3. **Draft the practice plan**: We will write a specific task list file for that branch and begin working!
