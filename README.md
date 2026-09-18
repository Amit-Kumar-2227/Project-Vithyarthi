# Agentic Workflow Automation System (Java CLI)

A robust, modular, and enterprise-grade multi-agent task execution and state-management system built purely in Java. This project implements autonomous agent workflows (Planner, Executor, and Verifier) operating entirely through an interactive Command Line Interface (CLI) with zero graphical dependencies.

---

## 📋 Table of Contents
1. [Project Overview & Architecture](#-project-overview--architecture)
2. [Core Features](#-core-features)
3. [Design Patterns Implemented](#-design-patterns-implemented)
4. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
5. [Directory Structure](#-directory-structure)
6. [Step-by-Step Setup & Installation](#-step-by-step-setup--installation)
7. [Execution Guide](#-execution-guide)
8. [Sample Usage & Workflow Simulation](#-sample-usage--workflow-simulation)
9. [Error Handling & Logging](#-error-handling--logging)

---

## 🏛️ Project Overview & Architecture

The Agentic Workflow System mimics autonomous AI agent pipelines directly inside a terminal environment. When a user submits a complex task, the system passes it through three sequential stages:
1. **Planner Agent:** Analyzes the raw task input, decomposes it into smaller logical sub-tasks, and generates a structured execution plan.
2. **Executor Agent:** Iterates through the sub-tasks, simulates tool execution or data processing, and computes intermediate outputs.
3. **Verifier Agent:** Validates the final output against predefined success metrics, logs state transitions, and marks the workflow as `COMPLETED` or `FAILED`.

---

## 🚀 Core Features
* **Multi-Agent Pipeline:** Clear separation of concerns between planning, execution, and verification phases.
* **CLI-Driven Interactive Menu:** Real-time terminal prompts allowing users to input tasks and watch agent logs live.
* **Robust State Machine:** Tracks execution states meticulously (`PENDING`, `RUNNING`, `SUCCESS`, `FAILED`).
* **Local Logging Mechanism:** Automatically serializes execution history and state reports into a local directory.

---

## 🧩 Design Patterns Implemented
* **Strategy Pattern:** Used for dynamic agent behavior switching depending on task type.
* **Command Pattern:** Encapsulates sub-tasks as executable command objects for flexible queuing and rollback.
* **Factory Pattern:** Instantiates appropriate specialized agents based on runtime configuration.

---

## 🛠️ Prerequisites & System Requirements
Ensure your local environment meets the following criteria before running the project:
* **Java Development Kit (JDK):** Version 17 or higher (`java -version`).
* **Build Tools:** Standard Java compiler (`javac`) or Apache Maven (`mvn -v`).

---

## 📂 Directory Structure

The project follows a standard object-oriented package structure:
```text
├── src/
│   ├── com/
│   │   └── system/
│   │       ├── agents/         # Specialized Agent classes (Planner, Executor, Verifier)
│   │       ├── core/           # Workflow engine, state manager, and orchestration logic
│   │       ├── model/          # Task payloads, state records, and data structures
│   │       ├── utils/          # Logging and input validation utilities
│   │       └── main/           # Main execution class containing the CLI entry point
├── logs/                       # Generated execution logs and run reports
└── README.md                   # Comprehensive project documentation
