# 🍽️ FoodieHub – Full-Stack Food Ordering & Order Management Application

[![Live Demo](https://img.shields.io/badge/demo-live-success?style=for-the-badge)](https://github.com/malikhub123/FoodieHub/blob/main/FoodieHub%20Demo_video.mp4)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/)

A **modern, scalable, full-stack food ordering application** built completely from scratch using **Spring Boot** and **React**. FoodieHub simulates a real-world online food ordering platform with secure payments, order tracking, and cloud deployment.

---

## 🌟 Overview

FoodieHub enables users to:
- 🔍 Browse restaurant menus
- 🛒 Manage shopping carts
- 💳 Place orders with secure payments
- 📧 Receive automated email notifications
- 📊 Track order status in real-time

Built with industry-grade practices focusing on **security**, **scalability**, and **maintainability**.

---

## 🎥 Project Demo

[![Watch the demo](image%201.png)](FoodieHub%20Demo_video.mp4)



## 🚀 Live Project

🔗 **Experience it live:** [http://foodie-hub.s3-website.ap-south-1.amazonaws.com/](http://foodie-hub.s3-website.ap-south-1.amazonaws.com/)

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Frontend** | React.js | Responsive, component-based UI with efficient state management |
| **Backend** | Spring Boot | Scalable REST API architecture |
| **Security** | Spring Security + JWT | Secure authentication & role-based access control |
| **ORM** | Spring Data JPA (Hibernate) | Simplified persistence & relationship management |
| **Payments** | Stripe API | Secure payment processing |
| **Email** | Spring Mail | Automated order & payment notifications |
| **Database** | MySQL (AWS RDS) | Reliable data storage |
| **Deployment** | AWS (EC2, RDS, S3) | Production-ready cloud infrastructure |

---

## ✨ Key Features

### 🔐 Authentication & Authorization
- JWT-based secure authentication
- Role-based access control (Admin/Customer)
- Protected API endpoints

### 🍕 Restaurant & Menu Management
- Full CRUD operations for menus
- Restaurant catalog browsing
- Dynamic menu updates

### 🛒 Order Management
- Smart cart management
- Seamless order placement workflow
- Order lifecycle tracking:
  - ✅ Order Placed
  - 💰 Payment Confirmed
  - 🎉 Completed

### 💳 Payment Integration
- Secure Stripe payment gateway
- Real-world transaction handling
- Payment success/failure workflows

### 📧 Automated Notifications
- Order confirmation emails
- Payment status updates
- Professional email templates

### 👨‍💼 Admin Dashboard
- User management
- Order oversight
- Menu item administration

> **📌 Note:** Order status tracking is implemented for management purposes. This project does not include delivery partner assignment or real-time GPS tracking.

---

## 🏗️ Architecture

```
┌─────────────────────┐
│   React Frontend    │
│   (AWS S3 Hosted)   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Spring Boot APIs   │
│     (AWS EC2)       │
└──────────┬──────────┘
           │
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐  ┌──────────────┐
│  MySQL  │  │    Stripe    │
│ AWS RDS │  │   Gateway    │
└─────────┘  └──────────────┘
```

---

## 🧩 Technical Challenges & Solutions

### Challenge 1: Complex Entity Relationships
**Solution:** Designed optimized JPA relationships between Users, Carts, Orders, Menus, and Payments while preventing N+1 query issues through strategic use of lazy/eager loading.

### Challenge 2: Stateless Authentication
**Solution:** Implemented JWT-based authentication with secure token validation, automatic refresh, and role-based endpoint protection.

### Challenge 3: Payment Integration
**Solution:** Integrated Stripe with robust error handling, webhook validation, and transaction state management.

### Challenge 4: Email Security
**Solution:** Environment-based configuration for email credentials with no hardcoded secrets.

### Challenge 5: Production Deployment
**Solution:** Deployed on AWS with EC2 for backend, RDS for database, and S3 for frontend, ensuring high availability and scalability.

---

## 📚 Learning Outcomes

This project demonstrates expertise in:

✅ End-to-end full-stack development  
✅ RESTful API design principles  
✅ Secure authentication & authorization  
✅ Third-party API integration  
✅ Cloud deployment & DevOps  
✅ Database design & optimization  
✅ Production-grade security practices  

---

## ⚙️ Environment Setup

### Prerequisites
- ☕ Java 17+
- 🟢 Node.js & npm
- 🐬 MySQL
- 📦 Maven

### Environment Variables

Create a `.env` file or configure in your deployment environment:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/foodiehub
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Stripe Configuration
STRIPE_SECRET_KEY=your_stripe_secret_key

# Email Configuration
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password
```

---

## 🚀 Local Development

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-username/foodiehub-backend.git
cd foodiehub-backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

**Backend runs at:** `http://localhost:8080`

### Frontend Setup

```bash
# Clone the repository
git clone https://github.com/your-username/foodiehub-frontend.git
cd foodiehub-frontend

# Install dependencies
npm install

# Start development server
npm start
```

**Frontend runs at:** `http://localhost:3000`

---

## ☁️ Deployment Architecture

| Component | AWS Service | Purpose |
|-----------|-------------|---------|
| Frontend | S3 + CloudFront | Static website hosting with CDN |
| Backend | EC2 | Application server |
| Database | RDS (MySQL) | Managed database service |
| Storage | S3 | Asset storage |

---

## 🔮 Future Enhancements

- [ ] 🚗 Delivery partner module
- [ ] 📍 Real-time order tracking with Google Maps
- [ ] 🤖 AI-based food recommendations
- [ ] 📊 Advanced admin analytics dashboard
- [ ] 🌐 Multi-language support
- [ ] ⭐ Rating and review system
- [ ] 🔔 Push notifications

---

## 📸 Screenshots

> Add screenshots of your application here to showcase the UI
![image](https://github.com/malikhub123/FoodieHub/blob/main/image%204.png)
![image](https://github.com/malikhub123/FoodieHub/blob/main/image%203.png)
![image](https://github.com/malikhub123/FoodieHub/blob/main/image%202.png)
![image](https://github.com/malikhub123/FoodieHub/blob/main/image%201.png)


---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👩‍💻 Author

**Aditi Malik**  
*B.Tech – Computer Science & Engineering*  
*Full-Stack Developer (Spring Boot | React | AWS)*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/aditi-malik-43880a222/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:malik2002.aditi@gmail.com)


<div align="center">

**If you found this project helpful, please consider giving it a ⭐!**

Made with ❤️ by Aditi Malik

</div>
