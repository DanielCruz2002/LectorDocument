package com.Husk.LectorDocument.service;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class OcrService {

    private GeminiService GeminiService;
    public OcrService(GeminiService GeminiService){
        this.GeminiService = GeminiService;
    }

    public String extractTextFromImage(String imagePath) {
        ITesseract tesseract = new Tesseract();

        try {
            // 🔹 Intentamos primero usar la variable de entorno (si está en Docker/Railway)
            String tessdataPath = System.getenv("TESSDATA_PREFIX");
            if (tessdataPath != null && !tessdataPath.isEmpty()) {
                tesseract.setDatapath(tessdataPath);
            } else {
                // 🔹 Si no existe, copiamos los datos de tessdata desde resources al sistema temporal
                Path tempDir = Files.createTempDirectory("tessdata");
                copyResourceToFile("Tesseract-OCR/tessdata/spa.traineddata",
                        tempDir.resolve("spa.traineddata").toFile());
                tesseract.setDatapath(tempDir.toAbsolutePath().toString());
            }

            tesseract.setLanguage("spa");

            String text = tesseract.doOCR(new File(imagePath));
            return text.trim();

        } catch (IOException e) {
            throw new RuntimeException("Error al preparar los datos de Tesseract: " + e.getMessage(), e);
        } catch (TesseractException e) {
            throw new RuntimeException("Error al procesar imagen con Tesseract: " + e.getMessage(), e);
        }
    }
    private void copyResourceToFile(String resourcePath, File targetFile) throws IOException {
        try (InputStream in = new ClassPathResource(resourcePath).getInputStream()) {
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    public String leerArchivoRecurso(String ruta) throws IOException {
        return new String(new ClassPathResource(ruta).getInputStream().readAllBytes());
    }

    public String SetFactura(String imagePath) throws IOException {
        String contenido = this.leerArchivoRecurso("Prompts/Facturas.txt");
        String ContentFactura = extractTextFromImage(imagePath);
        String prompt = contenido.replace("<TextOCR>", ContentFactura);


        return GeminiService.GenerateText(prompt);
    }

}
