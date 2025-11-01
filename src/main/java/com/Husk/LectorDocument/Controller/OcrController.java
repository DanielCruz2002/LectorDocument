package com.Husk.LectorDocument.Controller;

import com.Husk.LectorDocument.service.OcrService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "*")
public class OcrController {

    private static final Logger logger = Logger.getLogger(OcrController.class.getName());

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    /**
     * Endpoint de salud
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "OCR Service");
        response.put("tesseract", ocrService.verificarConfiguracion());

        return ResponseEntity.ok(response);
    }

    /**
     * Extraer texto de una imagen
     */
    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractText(
            @RequestParam("file") MultipartFile file) {

        logger.info("Solicitud de extracción de texto: " + file.getOriginalFilename());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar archivo
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("error", "El archivo debe ser una imagen");
                return ResponseEntity.badRequest().body(response);
            }

            // Extraer texto
            String texto = ocrService.extractTextFromImage(file);

            response.put("success", true);
            response.put("filename", file.getOriginalFilename());
            response.put("text", texto);
            response.put("textLength", texto.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error procesando imagen: " + e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Procesar una factura con OCR + Gemini
     */
    @PostMapping("/factura")
    public ResponseEntity<String> procesarFactura(
            @RequestParam("file") MultipartFile file) {

        logger.info("Solicitud de procesamiento de factura: " + file.getOriginalFilename());

        try {

            // Procesar factura
            String resultado = ocrService.procesarFactura(file);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.severe("Error procesando factura: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }


    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> verificarConfiguracion() {
        Map<String, Object> response = new HashMap<>();

        boolean tesseractOk = ocrService.verificarConfiguracion();

        response.put("tesseract", tesseractOk);
        response.put("status", tesseractOk ? "OK" : "ERROR");

        if (!tesseractOk) {
            response.put("message", "Tesseract no está configurado correctamente");
        }

        return ResponseEntity.ok(response);
    }
}