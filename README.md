# 📘 Trading App – Web Application + REST Microservice  
A complete Spring Boot project consisting of:

- **Web Application (MVC + Thymeleaf)**  
- **REST Microservice (invoice-service)**  

This repository follows the assignment requirements and includes:

✔ Authentication (Login / Registration)  
✔ CSRF Protection  
✔ Authorization with roles (USER / ADMIN)  
✔ Users, Products, Customers, Orders  
✔ Admin Panel for user management  
✔ Account profile with editing + password change  
✔ REST API for invoices (invoice-service)  
✔ Clean project structure with two independent modules  

## Project Structure
```
trading-app/
 ├─ web-app/
 ├─ invoice-service/
 ├─ documentation/
 ├─ correspondence/
 └─ README.md
```

## Features
- User Auth, Admin Panel, CRUD, REST API, Security

## How to Run
```
cd web-app
./mvnw spring-boot:run
```
```
cd invoice-service
./mvnw spring-boot:run
```
