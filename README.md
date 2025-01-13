This project is a sample **Test Automation Framework** built using **Gatling** and **Maven** to write automated load/performance tests in **Groovy**.

## Key features

- Simplified simulations which are free of complex Gatling DSL syntax
- Reusable methods to enable efficient API calls, ensuring scalability and ease of maintenance and reducing repetitive code
- Modular structure, where individual API classes are self-contained and designed with appropriate, reusable chain builders
- Simulations cover a wide range of frequently encountered use cases including verifications and assertions, demonstrated on Gatling's sample applications 

## Prerequisites

- **Java** 21 or higher
- **Maven** 3.9.6 or higher

## Getting Started

1. Clone or download the repository.
2. Open the project in IntelliJ IDEA (the bundled Maven version will be used by default)
3. Build the project using maven.
4. In **IntelliJ IDEA**, go to **Run** → **Edit Configurations**.
5. Choose any of the predefined **Maven run/debug configurations** and click **Run** ▶️ or **Debug** 🐞.
6. To run/debug a simulation locally, please use the application run/debug configuration - **RunDebugSimulation**

## Viewing the Gatling Report

At the end of the test execution, you can view timestamped gatling test reports, generated individually for each simulation run, in the `target/gatling` directory. The output should look something like this:

![Screenshot](/gatling-report-screenshot.png)