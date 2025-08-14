# Java RMI Calculator 

This project implements a distributed calculator using **Java RMI** where **each client has its own stack** on the server accessed by their own id.
Follow this readme to compile, start the RMI registry and server, run clients, and simulate multiple clients...

---

## 1. Compile All Java Files

**Linux / macOS**

``cd src``

``javac *.java``


**Windows (Command Prompt)**

``cd src``

``javac *.java``

---

## 2. Start the RMI Registry and Server

### A. Start RMI Registry

**Linux / macOS**

``cd src``

``rmiregistry 1099 &``

**Windows (Command Prompt)**

``cd src``

``start rmiregistry 1099``


>✅ Make sure you run `rmiregistry` **from inside the `src` folder** so it can find your compiled `.class` files.  
>Leave the RMI registry running while you run the server and clients.

---

### B. Start the Server

**Linux / macOS**

``cd src``

`java CalculatorServer`


**Windows (Command Prompt)**

`cd src`

`java CalculatorServer`


When the server starts successfully you’ll see:

`Calculator Server ready.`


---

## 3. Run the Client

**Linux / macOS**

`cd src`

`java CalculatorClient`


**Windows (Command Prompt)**

`cd src`

`java CalculatorClient`

You can register more clients in compile time in the CalculatorClient.java file. There are comments to guide you there.


---

## 5. Run Automated Tests (JUnit)

> **Important:** The RMI registry and server must be running before running tests.

If you’re using IntelliJ:
1. Mark the `test` folder as **Test Sources Root**.
2. Ensure JUnit 5 is on the classpath.
3. Run `CalculatorTest` via right‑click → **Run**.


**Linux / macOS** (Maven):

`mvn test`


**Windows** (Maven, Command Prompt or PowerShell):

`mvn test`

If not using Maven, just run from IntelliJ’s built-in test runner.

---

## 6. Example Project Structure

```
assi 1/
├─ src/
│  ├─ Calculator.java
│  ├─ CalculatorImplementation.java
│  ├─ CalculatorServer.java
│  ├─ CalculatorClient.java
├─ test/
│  └─ CalculatorTest.java
├─ README.md
```
