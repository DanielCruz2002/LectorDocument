# Usa Java 21 como base
FROM eclipse-temurin:21-jdk

# Instala Tesseract y dependencias
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

# Crea directorio de la app
WORKDIR /app

# Copia todo el proyecto
COPY . .

# Copia los archivos de idioma de Tesseract al sistema
COPY src/main/resources/Tesseract-OCR/tessdata /usr/share/tesseract-ocr/4.00/tessdata/
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

# Da permisos a Maven Wrapper si existe
RUN chmod +x ./mvnw || true

# Construye el proyecto
RUN ./mvnw package -DskipTests

# Expone el puerto
EXPOSE 8080

# Ejecuta la app
CMD ["java", "-jar", "target/LectorDocument-0.0.1-SNAPSHOT.jar"]