# TaxiBooking
Taxi booking system for picking the nearest available car to the customer location and return the total time taken to travel from the current car location to customer location then to customer destination.

This application is written in Scala to reduce complexity with many features of functional programming while being concise. Scala comes with static types to help avoid bugs, used by many successful companies, and used to write Apache Spark for large-scale data processing.

Akka toolkit is used to build a modular design using the actor model concept, avoids becoming a framework that restrict the application's architecture, and is able to achieve responsive concurrent execution and distribution of components.

TaxiManager is a parent actor, and manages multiple Taxi child actors. Via sending and receiving messages between TaxiManager and Taxi, new Taxi actors are created, states are maintained, and domain logic is achieved through their behaviors in the actor model. TaxiService is in charge of HTTP routing, handling, and interfacing with TaxiManager by sending and receiving messages to and from TaxiManager whenever there are appropriate HTTP requests and respond to them. TaxiServer extends TaxiService and sets up HTTP server at http://localhost:8080. Main is the entry point of this application and it calls TaxiServer to initialize the system.

## Getting Started
Follow these instructions to get this project up and running on your machine.

### Prerequisites
Install Oracle JDK 8 or OpenJDK 8. Install sbt 1.2.8 for documentation generation, unit testing, generate standalone executable JAR, and run this project with source code. Install Python 3 for integration testing.

### Configuration
Open src/main/resources/application.conf to modify the following setting:
* loglevel - Set to "DEBUG" to show all log messages, "OFF" to stop showing any log messages, and other options for different log levels: ERROR, WARNING, INFO.

### Documentation Generation
In Terminal or PowerShell, **cd** into the root folder of this project, and execute **sbt doc** to generate documentation in *TaxiBooking/target/scala-2.12*.

### Unit Testing
In Terminal or PowerShell, **cd** into the root folder of this project, and execute **sbt test** to run all unit tests.

### Generate Standalone Executable JAR
In Terminal or PowerShell, **cd** into the root folder of this project, and execute **sbt assembly** to generate JAR in *TaxiBooking/target/scala-2.12/api*.

### Running
In Terminal or PowerShell, **cd** into the root folder of this project, and execute **sbt run** to run with application.conf configuration.
Alternatively without running sbt, execute **java -jar TaxiBooking-0.1.0.jar** in the root folder of this project to run this application with standalone executable JAR.

### Integration Testing
Ensure TaxiBooking is running from the previous step, launch another Terminal or PowerShell, **cd** into the root folder of this project, and execute **python test_integration.py** to run all integration tests.