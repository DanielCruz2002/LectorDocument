# ==========================================
# 🔹 Etapa 1 — Construcción del proyecto
# ==========================================
FROM eclipse-temurin:21-jdk AS builder

# Copia todo el proyecto dentro del contenedor
WORKDIR /app
COPY . .

# Da permisos al wrapper de Maven (por si no los tiene)
RUN chmod +x ./mvnw || true

# Compila el proyecto y genera el .jar
RUN ./mvnw clean package -DskipTests


# ==========================================
# 🔹 Etapa 2 — Imagen final (más liviana)
# ==========================================
FROM eclipse-temurin:21-jdk

# Instala Tesseract OCR y sus dependencias
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    rm -rf /var/lib/apt/lists/*

# Crea el directorio de la aplicación
WORKDIR /app

# Copia el JAR construido desde la etapa anterior
COPY --from=builder /app/target/LectorDocument-0.0.1-SNAPSHOT.jar /app/app.jar

# Copia los modelos de idioma (por ejemplo, español)
COPY src/main/resources/Tesseract-OCR/tessdata /usr/share/tesseract-ocr/4.00/tessdata/

# Define la variable de entorno que Tesseract usará
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

# Expone el puerto del backend
EXPOSE 8080

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
