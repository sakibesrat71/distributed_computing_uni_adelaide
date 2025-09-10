# Distributed Weather Aggregation System

## Overview

This project implements a distributed weather data aggregation system consisting of:

- **AggregationServer**: A central server that collects weather data from multiple content servers, aggregates it, and serves it to clients.
- **ContentServer**: A client that reads local weather data files and sends updates to the AggregationServer via HTTP-like PUT requests.
- **GETClient**: A client that queries the AggregationServer to retrieve and display aggregated weather data.
- **LamportClock**: A Lamport logical clock implementation used to synchronize events across distributed components.

The system supports concurrent requests, data expiry after a configurable timeout (default 30 seconds), and persistent storage with crash recovery using atomic file writes.

---

## Architecture

### Components

- **AggregationServer**
    - Listens for TCP socket connections.
    - Handles HTTP-like GET and PUT requests.
    - Stores weather data keyed by content server `id`.
    - Maintains last contact timestamps for expiry.
    - Periodically cleans expired entries.
    - Uses a Lamport clock for event ordering.
    - Persists aggregated data safely to `weather.json`.

- **ContentServer**
    - Reads weather data from local text files.
    - Converts colon-separated key:value pairs to JSON.
    - Sends HTTP-like PUT requests to AggregationServer with JSON payload and Lamport clock.
    - Can be run concurrently to simulate multiple data sources.

- **GETClient**
    - Sends HTTP-like GET requests to AggregationServer.
    - Receives aggregated weather data as JSON array.
    - Parses and prints each weather entry line-by-line.
    - Synchronizes Lamport clock with server responses.

- **LamportClock**
    - Provides logical time ordering for distributed events.
    - Methods to tick and update clocks safely.

---

## How to Run

### Prerequisites

- Java JDK 18 or later installed.
- Project source and weather input files prepared.

---

### Running AggregationServer

Run AggregationServer first to start listening for client requests.


- Default port is `4567` if omitted.
- Example:


---

### Running ContentServer

Send weather data to the AggregationServer using ContentServer.

java ContentServer serverHost:port <path-to-weather-file>


- Example:
````
java ContentServer localhost:4567 weather_input1.txt
````

ContentServer reads the input file, converts data to JSON, and sends a PUT request.

---

### Running GETClient

Retrieve aggregated weather data from AggregationServer.

java GETClient serverHost:port


- Example:
````
java GETClient localhost:4567
````

GETClient sends a GET request and prints each weather entry received.

---

### Simulating Multiple ContentServers

Use the included `ConcurrentContentServerLauncher` to launch multiple ContentServer instances concurrently.

- Edit `ConcurrentContentServerLauncher.java` to specify the server address and weather files.
- Run in IntelliJ or command line without any arguments.

---

## Testing

### Unit Tests

- `LamportClockTest.java` tests Lamport clock increment and update.
- `ContentServerTest.java` tests JSON conversion correctness.
- `AggregationServerTest.java` tests JSON parsing, expiry logic, and map management.
- `GETClientTest.java` tests JSON parsing utilities.

### Integration Tests

- `IntegrationTest.java` launches the full system inside the test JVM.
- Tests concurrent PUT and GET requests.
- Validates expiry by running actual waits and verifying removal of stale data.
- Useful for end-to-end behavior verification.

---

## Project Structure

```plaintext
.
├── src/
│   ├── AggregationServer.java
│   ├── ContentServer.java
│   ├── GETClient.java
│   ├── LamportClock.java
│   ├── ConcurrentContentServerLauncher.java
│   ├── weather_input1.txt
│   ├── weather_input2.txt
│   └── ...
│
├── tests/
│   ├── LamportClockTest.java
│   ├── ContentServerTest.java
│   ├── AggregationServerTest.java
│   ├── GETClientTest.java
│   ├── IntegrationTest.java
│   ├── weather.json
│   └── weather_temp.json
│
└── README.md
```

---

## Notes

- Weather input files are simple text files with colon-separated key:value lines.
- Expiry timeout is configurable in `AggregationServer.java` (`EXPIRY_TIME_MS`).
- The server maintains fault-tolerant persistence mechanism to recover data after crashes.
- Lamport clocks are included in all messages to provide logical ordering.

---

## Troubleshooting

- Ensure weather input files are placed where the JVM process can read them (relative to working directory).
- Use `ConcurrentContentServerLauncher` to simulate realistic concurrent load.
- For testing, watch server console logs for expiry events and errors.
- Verify ports used are free and matching between servers and clients.

---


