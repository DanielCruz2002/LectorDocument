package com.Husk.LectorDocument.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Service
public class OcrService {

    private static final Logger logger = Logger.getLogger(OcrService.class.getName());

    private final GeminiService geminiService;

    @Value("${tesseract.datapath:/usr/share/tesseract-ocr/4.00/tessdata}")
    private String tessDataPath;

    @Value("${tesseract.language:spa}")
    private String tessLanguage;

    public OcrService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Extrae texto de una imagen usando Tesseract OCR
     */
    public String extractTextFromImage(MultipartFile file) throws IOException {
        logger.info("Iniciando extracción de texto de imagen: " + file.getOriginalFilename());

        // Crear archivo temporal
        Path tempFile = Files.createTempFile("ocr-", "-" + file.getOriginalFilename());

        try {
            // Guardar el archivo subido temporalmente
            file.transferTo(tempFile.toFile());

            // Extraer texto
            String text = extractTextFromFile(tempFile.toFile());

            logger.info("Texto extraído exitosamente. Longitud: " + text.length());
            return text;

        } catch (TesseractException e) {
            logger.severe("Error en Tesseract: " + e.getMessage());
            throw new IOException("Error al procesar imagen con OCR: " + e.getMessage(), e);
        } finally {
            // Eliminar archivo temporal
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                logger.warning("No se pudo eliminar archivo temporal: " + e.getMessage());
            }
        }
    }

    /**
     * Extrae texto de un archivo File
     */
    public String extractTextFromFile(File file) throws TesseractException {
        if (!file.exists()) {
            throw new IllegalArgumentException("El archivo no existe: " + file.getAbsolutePath());
        }

        Tesseract tesseract = new Tesseract();

        // Configurar Tesseract
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage(tessLanguage);

        logger.info("Configuración Tesseract - Datapath: " + tessDataPath + ", Language: " + tessLanguage);

        // Procesar imagen
        String text = tesseract.doOCR(file);

        return text != null ? text.trim() : "";
    }

    /**
     * Lee un archivo de recursos (como prompts)
     */
    public String leerArchivoRecurso(String ruta) throws IOException {
        logger.info("Leyendo archivo de recursos: " + ruta);

        try {
            ClassPathResource resource = new ClassPathResource(ruta);
            return new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            logger.severe("Error al leer archivo de recursos: " + ruta);
            throw new IOException("No se pudo leer el recurso: " + ruta, e);
        }
    }

    /**
     * Procesa una factura: extrae texto y lo analiza con Gemini
     */
    public String procesarFactura(MultipartFile file) throws IOException {
        logger.info("Procesando factura: " + file.getOriginalFilename());

        // Leer el prompt desde resources
        String promptTemplate = leerArchivoRecurso("Prompts/Facturas.txt");

        // Extraer texto de la imagen
        String textoFactura = extractTextFromImage(file);
        textoFactura = textoFactura
                .replaceAll("[\\r\\n]+", " | ")   // Cambia \r o \n por " | "
                .replaceAll("\\s{2,}", " ")       // Colapsa espacios múltiples en uno solo
                .trim();                          // Quita espacios al inicio y final
        logger.info("*********************************************");
        logger.info("*********************************************");

        logger.info(textoFactura);

        logger.info("*********************************************");
        logger.info("*********************************************");
        if (textoFactura.isEmpty()) {
            throw new IOException("No se pudo extraer texto de la imagen");
        }

        // Reemplazar placeholder con el texto extraído
        String prompt = promptTemplate.replace("<TextOCR>", textoFactura);

        logger.info("Enviando texto a Gemini para análisis");

        // Analizar con Gemini
        return textoFactura;//geminiService.GenerateText(prompt);
    }

    /**
     * Verifica que Tesseract esté configurado correctamente
     */
    public boolean verificarConfiguracion() {
        try {
            File tessData = new File(tessDataPath);
            File langFile = new File(tessDataPath, tessLanguage + ".traineddata");

            boolean dataPathExiste = tessData.exists();
            boolean langFileExiste = langFile.exists();

            logger.info("Verificación Tesseract:");
            logger.info("- DataPath existe: " + dataPathExiste + " (" + tessDataPath + ")");
            logger.info("- Archivo de idioma existe: " + langFileExiste + " (" + langFile.getAbsolutePath() + ")");

            return dataPathExiste && langFileExiste;
        } catch (Exception e) {
            logger.severe("Error verificando configuración: " + e.getMessage());
            return false;
        }
    }
}