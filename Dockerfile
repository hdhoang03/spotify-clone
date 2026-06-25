# ==========================================
# BƯỚC 1: BUILD MÃ NGUỒN (Dùng Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml và source code vào container
COPY pom.xml .
COPY src ./src

# Chạy lệnh Maven để đóng gói dự án thành file .jar
RUN mvn clean package -DskipTests

# ==========================================
# BƯỚC 2: CHẠY ỨNG DỤNG (Môi trường siêu nhẹ)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file .jar đã build từ Bước 1 sang
COPY --from=builder /app/target/*.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Lệnh khởi động Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]