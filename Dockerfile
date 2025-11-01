FROM eclipse-temurin:21-jdk

# Instala Tesseract y los paquetes necesarios
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev && \
    apt-get install -y tesseract-ocr-eng tesseract-ocr-spa && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

# Da permisos de ejecución a Maven Wrapper
RUN chmod +x ./mvnw

# Construye el proyecto
RUN ./mvnw package -DskipTests

# Asegura que Tess4J encuentre Tesseract
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu/
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

EXPOSE 8080

CMD ["java", "-jar", "target/LectorDocument-0.0.1-SNAPSHOT.jar"]
