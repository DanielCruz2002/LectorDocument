# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Instalar Tesseract OCR y el idioma español
RUN apt-get update && \
    apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-spa \
    libtesseract-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Verificar instalación de Tesseract
RUN tesseract --version && \
    ls -la /usr/share/tesseract-ocr/4.00/tessdata/

# Crear directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Crear directorio temporal para imágenes
RUN mkdir -p /tmp/uploads && chmod 777 /tmp/uploads

# Exponer el puerto
EXPOSE 8080

# Variables de entorno opcionales
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]