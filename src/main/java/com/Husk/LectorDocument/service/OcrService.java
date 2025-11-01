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

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Ruta típica en Linux
        tesseract.setLanguage("spa");

        try {
            return tesseract.doOCR(new File(imagePath)).trim();
        } catch (TesseractException e) {
            throw new RuntimeException("Error al procesar imagen con Tesseract", e);
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
