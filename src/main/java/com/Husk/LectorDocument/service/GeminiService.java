package com.Husk.LectorDocument.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiKey;
    private String Model = "gemini-2.5-flash";

    public GeminiService() {
    }

    public String GenerateText(String Prompt){
        Client client = new Client.Builder()
                .apiKey(geminiKey)
                .build();
        GenerateContentResponse response =
                client.models.generateContent(Model, Prompt,null);
        return response.text();

    }
}
