package com.Husk.LectorDocument.Controller;

import com.Husk.LectorDocument.service.OcrService;
import com.Husk.LectorDocument.service.GeminiService;
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

    @PostMapping("/image")
    public String extractText(@RequestParam("file") MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("ocr_", file.getOriginalFilename());
        file.transferTo(tempFile);
        return ocrService.extractTextFromImage(tempFile.getAbsolutePath());
    }
    @GetMapping("/generateText")
    public String generateText(){
        return serviceGemini.GenerateText("holaa");
    }
    @PostMapping("/setFactura")
    public String setFactura(@RequestParam("file") MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("ocr_", file.getOriginalFilename());
        file.transferTo(tempFile);
        return ocrService.SetFactura(tempFile.getAbsolutePath());
    }

}
