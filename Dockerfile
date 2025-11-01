# Etapa 1: Build con Maven
FROM maven:3.9-eclipse-temurin-21 AS build

# Configurar variables de entorno para Maven
ENV MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

WORKDIR /build

# Copiar archivos de Maven para cachear dependencias
COPY pom.xml .
COPY src ./src

# Limpiar cualquier build anterior y construir el proyecto
RUN mvn clean package -DskipTests -e -X 2>&1 | tee /tmp/maven-build.log || \
    (cat /tmp/maven-build.log && exit 1)

# Verificar que el JAR se creó
RUN ls -lah /build/target/ && \
    test -f /build/target/*.jar || (echo "ERROR: JAR no encontrado" && exit 1)

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Instalar Tesseract OCR con idioma español
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-spa \
        libtesseract-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Verificar que Tesseract está instalado correctamente
RUN tesseract --version && \
    ls -la /usr/share/tesseract-ocr/4.00/tessdata/ && \
    test -f /usr/share/tesseract-ocr/4.00/tessdata/spa.traineddata || \
    (echo "ERROR: spa.traineddata no encontrado" && exit 1)

# Crear usuario no-root para seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /build/target/*.jar app.jar

# Crear directorio para archivos temporales
RUN mkdir -p /tmp/uploads && \
    chown -R appuser:appuser /app /tmp/uploads

# Variables de entorno
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]