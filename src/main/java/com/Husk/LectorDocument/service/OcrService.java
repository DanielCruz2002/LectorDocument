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
            // Extraer spa.traineddata del classpath a un archivo temporal
            Path tempDir = Files.createTempDirectory("tessdata");
            try (InputStream in = new ClassPathResource("Tesseract-OCR/tessdata/spa.traineddata").getInputStream()) {
                Files.copy(in, tempDir.resolve("spa.traineddata"), StandardCopyOption.REPLACE_EXISTING);
            }

            tesseract.setDatapath(tempDir.toAbsolutePath().toString());
            tesseract.setLanguage("spa");

            return tesseract.doOCR(new File(imagePath)).trim();

        } catch (Exception e) {
            throw new RuntimeException("Error configurando Tesseract: " + e.getMessage(), e);
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
