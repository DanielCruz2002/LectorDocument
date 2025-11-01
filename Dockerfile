# ===========================
# 🧱 Etapa 1: Build con Maven
# ===========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x ./mvnw || true
RUN ./mvnw clean package -DskipTests


# ===========================
# 🚀 Etapa 2: Imagen final
# ===========================
FROM eclipse-temurin:21-jdk

# Instala Tesseract OCR
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia el JAR compilado
COPY --from=builder /app/target/LectorDocument-0.0.1-SNAPSHOT.jar /app/app.jar

# 🔹 Copia tus modelos (los que ya tienes)
COPY src/main/resources/Tesseract-OCR/tessdata /usr/share/tesseract-ocr/4.00/tessdata/
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
