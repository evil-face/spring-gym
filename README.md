# Gym Management App

This project is a gym management system that consists of two separate microservices for efficient handling of various tasks related to gym operations. 

## Features

- **Microservices Architecture:** The application is built using a microservices architecture to comminicate with separate [workload microservice](https://github.com/evil-face/trainer-workload-service).

- **REST API:** The main service is equipped with a REST API to facilitate communication and data exchange for gym entities such as 'Trainee', 'Trainer' and 'Training'.

- **Message Broker:** Efficient communication channel is established through the use of ActiveMQ message broker, ensuring real-time updates and notifications.

- **Data Storage Solutions:** The app integrates both SQL (PostgreSQL) and NoSQL (MongoDB) data storage solutions.

- **Circuit Breaker Pattern:** The application's resilience is improved by implementing the Circuit Breaker pattern.

- **Health Monitoring:** Utilizing Actuator, Prometheus, and Grafana, the app's health can be monitored.

- **Security Measures:** The application is secured using JWT access tokens. Basic login brute force protection is also implemented to enhance security.

- **Code Quality and Analysis:** SonarQube was used for static code analysis, ensuring high code quality. The project maintains a code coverage of 75%.
