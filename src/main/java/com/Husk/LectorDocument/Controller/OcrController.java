package com.Husk.LectorDocument.Controller;

import com.Husk.LectorDocument.service.OcrService;
import com.Husk.LectorDocument.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "http://localhost:4200/")
public class OcrController {

    private final OcrService ocrService;
    private final GeminiService serviceGemini;

    public OcrController(OcrService ocrService, GeminiService serviceGemini) {
        this.serviceGemini = serviceGemini;
        this.ocrService = ocrService;
    }

    @GetMapping("/test-tesseract")
    public ResponseEntity<String> testTesseract() {
        try {
            File tessdata = new File("/usr/share/tesseract-ocr/4.00/tessdata/spa.traineddata");
            if (tessdata.exists()) {
                return ResponseEntity.ok("✅ Tesseract configurado correctamente. Archivo spa.traineddata encontrado.");
            } else {
                return ResponseEntity.ok("❌ No se encontró spa.traineddata en: " + tessdata.getAbsolutePath());
            }
        } catch (Exception e) {
            return ResponseEntity.ok("❌ Error: " + e.getMessage());
        }
    }

}
