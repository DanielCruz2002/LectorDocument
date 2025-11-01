package com.Husk.LectorDocument.service;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private GeminiService GeminiService;
    public OcrService(GeminiService GeminiService){
        this.GeminiService = GeminiService;
    }

    public String extractTextFromImage(String imagePath) {
        ITesseract tesseract = new Tesseract();

        // Configura la ruta a los datos de idioma de Tesseract (tessdata)
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        // Idioma (por ejemplo, "eng" o "spa")
        tesseract.setLanguage("spa");

        try {
            String text = tesseract.doOCR(new File(imagePath));
            return text;
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error al procesar imagen: " + e.getMessage();
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
