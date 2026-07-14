package com.aideai.network;

import com.aideai.AideAI;
import com.aideai.config.ModConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AIApiClient {
    private static final String BASE_URL = "https://api.deepseek.com/v1/chat/completions";
    private static OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
    private static MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static List<JsonObject> messageHistory = new ArrayList<>();

    public static String sendMessage(String userMessage) {
        String apiKey = ModConfig.CLIENT.apiKey.get();
        if (apiKey.isEmpty()) {
            return "[Error] Please set API Key first: /aideai setkey <your-api-key>";
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", ModConfig.CLIENT.model.get());

        JsonArray messages = new JsonArray();
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "You are a Minecraft assistant named AideAI. Be helpful and concise.");
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        requestBody.add("messages", messages);
        requestBody.addProperty("max_tokens", 500);
        requestBody.addProperty("temperature", 0.8);

        Request request = new Request.Builder()
            .url(ModConfig.CLIENT.apiUrl.get())
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(requestBody.toString(), JSON))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "[Error] API request failed: " + response.code();
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                return choices.get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();
            }
            return "[AideAI] AI returned empty response?";
        } catch (IOException e) {
            AideAI.LOGGER.error("API error", e);
            return "[Error] API request failed: " + e.getMessage();
        }
    }

    public static String extractCommand(String aiResponse) {
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("/")) {
                return line;
            }
        }
        return null;
    }
}
