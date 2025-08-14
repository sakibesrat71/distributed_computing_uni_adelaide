# Makefile for Java RMI Calculator Project

# Java compiler and flags
JC = javac
JFLAGS = -d bin

# Java runtime
JAVA = java

# RMI registry command
RMIREGISTRY = rmiregistry

# Source directory
SRC_DIR = src

# Output directory for class files
BIN_DIR = bin

# All Java source files
SOURCES = $(wildcard $(SRC_DIR)/*.java)

# Class files (expected)
CLASSES = $(patsubst $(SRC_DIR)/%.java, $(BIN_DIR)/%.class, $(SOURCES))

# Default target - compile all
all: compile

# Compile all .java files into bin directory
compile:
	@echo "Compiling Java sources..."
	mkdir -p $(BIN_DIR)
	$(JC) $(JFLAGS) $(SOURCES)

# Start rmiregistry (background)
start-rmiregistry:
	@echo "Starting RMI registry on port 1099..."
	@cd $(BIN_DIR) && $(RMIREGISTRY) 1099 &

# Run the server
run-server:
	@echo "Starting CalculatorServer..."
	$(JAVA) -cp $(BIN_DIR) CalculatorServer

# Run the client
run-client:
	@echo "Starting CalculatorClient..."
	$(JAVA) -cp $(BIN_DIR) CalculatorClient

# Clean compiled files
clean:
	@echo "Cleaning up..."
	rm -rf $(BIN_DIR)

# Full run: compile, start registry, run server (use separate terminal for server ideally)
run: compile
	@echo "You need to run the following commands in separate terminals:"
	@echo "1) make start-rmiregistry"
	@echo "2) make run-server"
	@echo "3) make run-client"

.PHONY: all compile start-rmiregistry run-server run-client clean run
