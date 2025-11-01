# Usa una imagen base con Java y Maven
FROM eclipse-temurin:21-jdk

# Instala Tesseract y dependencias
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

# Copia los archivos del proyecto
WORKDIR /app
COPY . .

# Construye el proyecto con Maven
RUN ./mvnw package -DskipTests

# Expone el puerto que usará Spring Boot
EXPOSE 8080

# Ejecuta la app
CMD ["java", "-jar", "target/LectorDocument-0.0.1-SNAPSHOT.jar"]
