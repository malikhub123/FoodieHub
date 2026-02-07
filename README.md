# 🍽️ FoodieHub – Full-Stack Food Ordering & Order Management Application

FoodieHub is a **modern, scalable, full-stack food ordering application** built completely from scratch using **Spring Boot and React**.  
It simulates a real-world **online food ordering platform**, enabling users to browse restaurant menus, manage carts, place orders, make secure online payments, and track order status updates.

The project focuses on **secure ordering workflows**, **role-based access control**, **payment processing**, **automated email notifications**, and **cloud deployment on AWS**, following industry-grade backend and full-stack development practices.

---

## 🚀 Live Demo

🔗 **Live Application:**  
http://foodie-hub.s3-website.ap-south-1.amazonaws.com/

---

## 🛠️ Tech Stack

### Component – Tool – Why It Matters

| Component | Tool | Why It Matters |
|---------|------|---------------|
| Frontend | React.js | Enables a responsive, component-based UI with efficient state management. |
| Backend Framework | Spring Boot | Provides a scalable and maintainable architecture for REST APIs. |
| Security | Spring Security + JWT | Implements secure authentication, authorization, and role-based access. |
| Database ORM | Spring Data JPA (Hibernate) | Simplifies persistence and manages complex relationships between users, orders, carts, and menus. |
| Payment Gateway | Stripe API | Handles secure payment processing and real-world transaction workflows. |
| Email Service | Spring Mail | Sends automated order confirmations and payment notifications. |
| Cloud Deployment | AWS (EC2, RDS, S3) | Enables live deployment, scalability, and production readiness. |
| API Design | RESTful APIs | Ensures smooth frontend–backend communication. |

---

## ✨ Key Features

- User registration and login using **JWT-based authentication**
- **Role-based access control** (Admin, Customer)
- Restaurant and menu management with CRUD operations
- Cart management and order placement workflow
- Secure online payments using **Stripe**
- **Order lifecycle tracking**:
  - Order Placed  
  - Payment Confirmed  
  - Completed
- Automated **email notifications** for order confirmation and payment updates
- Admin functionality for managing users, orders, and menu items
- Deployed on **AWS cloud infrastructure**

> ⚠️ **Note:**  
> Order status tracking is implemented for **order management purposes only**.  
> This project does **not** include delivery partner assignment or real-time delivery tracking.

---

## 📚 What This Project Demonstrates

- End-to-end **full-stack development**
- Secure **JWT-based authentication & authorization**
- Integration of **real-world payment gateways**
- Clean **RESTful API design**
- Cloud deployment using **AWS**
- Best practices for **security, scalability, and maintainability**

---

## 🧩 Project Challenges & Solutions

- **Complex Entity Relationships**  
  Designed and optimized relationships between users, carts, orders, menus, and payments using Spring Data JPA while avoiding N+1 query issues.

- **Stateless Authentication**  
  Implemented JWT-based authentication with secure token validation and role-based endpoint protection.

- **Payment Integration**  
  Integrated Stripe payment workflows with proper success and failure handling.

- **Email Security**  
  Configured secure email delivery using environment-based configuration without exposing credentials.

- **Cloud Deployment**  
  Deployed backend services and database on AWS, ensuring production-level stability and availability.

---

## 🏗️ High-Level Architecture

React Frontend
↓
Spring Boot Backend (REST APIs)
↓
Spring Security + JWT
↓
Stripe Payment Gateway
↓
MySQL (AWS RDS)
↓
AWS EC2 & S3 Deployment


---

## ⚙️ Environment Variables

Create an `.env` file (or configure in AWS / system environment):

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/foodiehub
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Stripe
STRIPE_SECRET_KEY=your_stripe_secret_key

# Email
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password
🧪 Run the Project Locally
Prerequisites
Java 17+

Node.js & npm

MySQL

Maven

Backend Setup (Spring Boot)
git clone https://github.com/your-username/foodiehub-backend.git
cd foodiehub-backend
mvn clean install
mvn spring-boot:run
Backend will run at:

http://localhost:8080
Frontend Setup (React)
git clone https://github.com/your-username/foodiehub-frontend.git
cd foodiehub-frontend
npm install
npm start
Frontend will run at:

http://localhost:3000
☁️ Deployment
Frontend: Deployed on AWS S3 (Static Website Hosting)

Backend: Deployed on AWS EC2

Database: Hosted on AWS RDS (MySQL)

Environment-specific configurations are managed using application properties and AWS environment variables.

🔮 Future Enhancements
Delivery partner module

Real-time order tracking

Google Maps integration

AI-based food recommendations

Admin analytics dashboard

👩‍💻 Author
Aditi Malik
B.Tech – Computer Science & Engineering
Full-Stack Developer (Spring Boot | React | AWS)
