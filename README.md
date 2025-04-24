# Real-Time Ticketing System

A multi-threaded Java application that simulates a real-time ticket distribution and purchasing system. This project demonstrates thread synchronization, concurrent programming, and database integration for configuration persistence.

## Features

- **Multi-threaded Architecture**: Separate threads for ticket vendors and customers with synchronized access to shared resources
- **Configurable Parameters**: Customizable ticket quantities, release rates, and purchase rates
- **Database Integration**: MySQL-based configuration persistence
- **Robust Logging**: Comprehensive event logging to both console and file
- **Concurrency Control**: Thread-safe operations using locks, semaphores, and synchronized collections

## System Components

- **TicketPool**: Core component that manages the ticket inventory and coordinates operations
- **Vendor**: Represents ticket vendors who add tickets to the pool at configurable rates
- **Customer**: Represents customers who purchase tickets from the pool
- **Database**: Handles persistence for system configuration
- **Ticket**: Represents individual tickets with event information
- **TicketSystem**: Main application class with command-line interface

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- MySQL Server
- JDBC MySQL Connector

### Database Setup

1. Create a MySQL database named `ticket_syss`
2. Update the database connection settings in `Database.java` if necessary

### Running the Application

1. Compile all Java files:
   ```
   javac *.java
   ```
   
2. Run the application:
   ```
   java TicketSystem
   ```

3. Follow the on-screen prompts to configure and operate the system

## Usage

The system supports the following commands:
- `configure`: Set up or reconfigure the ticket system parameters
- `start`: Begin ticket distribution and purchasing operations
- `stop`: Halt all operations
- `exit`: Exit the application


