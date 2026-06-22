# Spotify Clone Backend 🎵

A comprehensive backend for a Spotify clone application built with modern Java and Spring Boot. It provides a robust RESTful API for music streaming, user management, playlist creation, and social features.

## 📖 Overview
This project serves as the backend for a full-stack Spotify clone, delivering high-performance APIs to handle everything from user authentication to complex audio streaming logic. It is architected using best practices in Spring Boot, featuring asynchronous processing, caching, and third-party integrations for payments and media storage.

## ✨ Features
- **Authentication & Authorization**: Secure JWT-based authentication, Role-based access control (Admin, User, Premium), Google OAuth2 integration, and Google reCAPTCHA v3 protection against bots.
- **Music & Content Management**: Comprehensive CRUD operations for Songs, Albums, Artists, Playlists, and Categories.
- **Streaming & Analytics**: Real-time stream tracking. Batched and synchronized play counts using Apache Kafka and scheduled cron jobs.
- **Social Features**: Like songs, create/share playlists, follow other users, follow artists, and block users.
- **Media Storage**: Direct integration with Cloudinary for efficient audio file streaming and image hosting.
- **Caching**: Redis integration for high-speed data access, caching popular queries, and managing invalidated JWT tokens.
- **Asynchronous Processing**: Apache Kafka implementation for decoupling background tasks like email notifications and system events.
- **Premium Subscriptions**: Integrated with PayOS payment gateway to process premium user upgrades.
- **Scheduled Jobs**: Automated workers for synchronizing play counts (`SyncPlayCountScheduler`) and managing premium subscription expirations (`PremiumScheduler`).
- **Email Notifications**: Spring Boot Mail with Thymeleaf templates for welcome emails, OTPs, and password resets.

## 🛠️ Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.6
- **Database**: MySQL 8.x, Spring Data JPA, Hibernate
- **Caching**: Redis (Lettuce client)
- **Message Broker**: Apache Kafka
- **Security**: Spring Security, OAuth2 Resource Server, JWT
- **Media Provider**: Cloudinary
- **DTO Mapping**: MapStruct
- **Payment Gateway**: PayOS

## 📁 Project Structure

```text
src/main/java/com/spotify/spotify
├── configuration   # Configuration for Security, Kafka, Redis, Cloudinary, etc.
├── controller      # RESTful API Endpoints
├── dto             # Request/Response Data Transfer Objects
├── entity          # Hibernate/JPA Database Entities
├── exception       # Global Exception Handling (@ControllerAdvice)
├── kafka           # Kafka Producers and Consumers
├── mapper          # MapStruct Mapper Interfaces
├── repository      # Spring Data JPA Repositories
└── service         # Core Business Logic and Services
```

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed and running:
- **Java 21** or higher
- **Maven**
- **MySQL 8**
- **Redis** server (running on `localhost:6379`)
- **Apache Kafka** (running on `localhost:9092`)

### Configuration
1. Clone the repository and navigate to the project directory.
2. Configure your MySQL database by creating a database named `spotify`.
3. Update `src/main/resources/application.yaml` with your own credentials or set the following environment variables:

```yaml
# Mail Server
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# Cloudinary
KEY_CLOUD=your_cloudinary_api_key
SECRET_CLOUD=your_cloudinary_api_secret

# PayOS (Payments)
PAYOS_CLIENT_ID=your_payos_client_id
PAYOS_API_KEY=your_payos_api_key
PAYOS_CHECKSUM_KEY=your_payos_checksum_key

# Google OAuth2 & reCAPTCHA
GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret
RECAPTCHA_SECRET_KEY=your_recaptcha_secret
```

### Running the Application

Using Maven wrapper:
```bash
# Clean and package the application
./mvnw clean package -DskipTests

# Run the Spring Boot application
./mvnw spring-boot:run
```
The application will start on `http://localhost:8080/spotify`.

## 📜 Domain Models
The core domain entities include:
- `User`, `Role`, `Permission` (Identity & Access Management)
- `Song`, `Album`, `Artist`, `Category`, `Lyrics` (Music Catalog)
- `Playlist`, `PlaylistSong` (User Collections)
- `LikeSong`, `ArtistFollow`, `UserFollow`, `UserBlock` (Social Interactions)
- `Order`, `SongStream`, `Notification` (Business Operations)
