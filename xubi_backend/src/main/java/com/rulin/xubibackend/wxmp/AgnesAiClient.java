package com.rulin.xubibackend.wxmp;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * Agnes AI 客户端
 * 使用 OpenAI 兼容 HTTP API
 */
@Slf4j
@Service
public class AgnesAiClient {

    @Resource
    private AgnesAiConfig config;

    private final Gson gson = new Gson();

    public String chat(String systemPrompt, String userPrompt) {
        ChatRequest request = new ChatRequest();
        request.model = config.getModelName();
        request.messages = new Message[] {
                new Message("system", systemPrompt != null ? systemPrompt : "You are a helpful assistant."),
                new Message("user", userPrompt)
        };
        request.temperature = 0.6f;
        request.maxTokens = 4096;

        String requestBody = gson.toJson(request);
        log.info("Agnes AI 请求: {}", requestBody);

        try {
            java.net.URL url = new java.net.URL(config.getBaseUrl() + "/chat/completions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int statusCode = conn.getResponseCode();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                    statusCode == 200 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            conn.disconnect();
            String responseBody = sb.toString();

            if (statusCode != 200) {
                throw new RuntimeException("AI 调用返回 " + statusCode + ": " + responseBody);
            }
            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("AI 返回为空");
            }

            log.info("Agnes AI 响应: {}", responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);
            return parseContent(responseBody);

        } catch (Exception e) {
            log.error("Agnes AI 调用失败", e);
            throw new RuntimeException("AI 调用失败: " + e.getMessage(), e);
        }
    }

    private String parseContent(String responseBody) {
        try {
            com.google.gson.JsonElement root = gson.fromJson(responseBody, com.google.gson.JsonElement.class);
            com.google.gson.JsonElement choices = root.getAsJsonObject().get("choices");
            if (choices == null || !choices.isJsonArray() || choices.getAsJsonArray().size() == 0) {
                log.error("响应无 choices: {}", responseBody);
                return "";
            }
            com.google.gson.JsonArray arr = choices.getAsJsonArray();
            com.google.gson.JsonElement message = arr.get(0).getAsJsonObject().get("message");
            if (message == null) return "";
            return message.getAsJsonObject().get("content").getAsString();
        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", responseBody, e);
            throw new RuntimeException("解析 AI 响应失败: " + e.getMessage());
        }
    }

    static class ChatRequest {
        String model;
        Message[] messages;
        Float temperature;
        Integer maxTokens;
    }

    static class Message {
        String role;
        String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
