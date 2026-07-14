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

public class AIapiClient {
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
            return "\u007c[错语] 请先设置 API Key: /aideai setkey <yourmySomekey>";
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", ModConfig.CLIENT.model.get());

        JsonArray messages = new JsonArray();
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "你是一个Minecraft全局作創易限，名为AideAI。你可情安下放词。可输出意行意前，也以参数易限控刷。");
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
                return "\u007c[错语] API读求失败： " + response.code();
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                return choices.get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();
            }
            return "\u007e[AideAI] AI没&spos;返回内宸？";
        } catch (IOException e) {
            AideAI.LOGGER.error("API错误地址错误", e);
            return "\u007c[错语] API错误失败： " + e.getMessage();
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
