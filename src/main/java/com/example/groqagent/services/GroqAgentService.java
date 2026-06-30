package com.example.groqagent.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class GroqAgentService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final WeatherService weatherService;
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public GroqAgentService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public String ask(String userMessage) throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "You are a helpful weather assistant. Whenever the user asks about current weather, " +
                        "temperature, or conditions in Brussels, you must call the get_temperature tool rather than answering from memory."
        ));
        messages.add(Map.of("role", "user", "content", userMessage));
        //messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> tool = Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_temperature",
                        "description", "Get the current temperature in Brussels, Belgium",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(),
                                "required", List.of()
                        )
                )
        );


        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("tools", List.of(tool));
        //body.put("tool_choice", "auto");
        body.put("tool_choice", Map.of(
                "type", "function",
                "function", Map.of("name", "get_temperature")
        ));

        JsonNode response = callGroq(body);

        JsonNode message = response.path("choices").get(0).path("message");
        JsonNode toolCalls = message.path("tool_calls");

        if (toolCalls.isMissingNode() || !toolCalls.isArray() || toolCalls.isEmpty()) {
            return message.path("content").asText();
        }

        messages.add(mapper.convertValue(message, Map.class));

        for (JsonNode call : toolCalls) {
            String functionName = call.path("function").path("name").asText();
            String callId = call.path("id").asText();

            if ("get_temperature".equals(functionName)) {
                double temp = weatherService.getBrusselsTemperature();
                messages.add(Map.of(
                        "role", "tool",
                        "tool_call_id", callId,
                        "content", "Current temperature in Brussels: " + temp + "°C"
                ));
            }
        }

        Map<String, Object> followUp = new HashMap<>();
        followUp.put("model", model);
        followUp.put("messages", messages);

        JsonNode finalResponse = callGroq(followUp);
        return finalResponse.path("choices").get(0).path("message").path("content").asText();
    }

    private JsonNode callGroq(Map<String, Object> body) throws Exception {
        String raw = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return mapper.readTree(raw);
    }
}