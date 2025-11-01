# Dockerfile simplificado y funcional para Railway

# Etapa 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

WORKDIR /build

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Verificar que el JAR se creó
RUN ls -la /build/target/

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Instalar Tesseract OCR con soporte para español
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-spa \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Verificar instalación de Tesseract
RUN tesseract --version && \
    echo "Verificando archivos de idioma..." && \
    ls -la /usr/share/tesseract-ocr/4.00/tessdata/

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /build/target/LectorDocument.jar app.jar

# Crear directorio para archivos temporales
RUN mkdir -p /tmp/uploads

# Variables de entorno
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Exponer puerto
EXPOSE 8080

# Ejecutar la aplicación
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]