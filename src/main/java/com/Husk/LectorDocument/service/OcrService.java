package com.Husk.LectorDocument.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private final GeminiService geminiService;

    // Permitir configurar la ruta desde application.properties
    @Value("${tesseract.datapath:/usr/share/tesseract-ocr/4.00/tessdata}")
    private String tessDataPath;

    public OcrService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String extractTextFromImage(String imagePath) {
        Tesseract tesseract = new Tesseract();

        try {
            // Intentar con la ruta configurada
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("spa");

            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                throw new RuntimeException("La imagen no existe en: " + imagePath);
            }

            String text = tesseract.doOCR(imageFile).trim();
            System.out.println("OCR exitoso. Texto extraído: " + text.substring(0, Math.min(100, text.length())));
            return text;

        } catch (TesseractException e) {
            System.err.println("Error Tesseract: " + e.getMessage());
            System.err.println("Ruta tessdata: " + tessDataPath);
            System.err.println("Ruta imagen: " + imagePath);
            throw new RuntimeException("Error al procesar imagen con Tesseract: " + e.getMessage(), e);
        }
    }

    public String leerArchivoRecurso(String ruta) throws IOException {
        try {
            return new String(new ClassPathResource(ruta).getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new IOException("No se pudo leer el recurso: " + ruta, e);
        }
    }

    public String SetFactura(String imagePath) throws IOException {
        // Leer el prompt desde resources
        String contenido = this.leerArchivoRecurso("Prompts/Facturas.txt");

        // Extraer texto de la imagen con OCR
        String contentFactura = extractTextFromImage(imagePath);

        // Reemplazar placeholder con el contenido extraído
        String prompt = contenido.replace("<TextOCR>", contentFactura);

        // Generar respuesta con Gemini
        return geminiService.GenerateText(prompt);
    }
}