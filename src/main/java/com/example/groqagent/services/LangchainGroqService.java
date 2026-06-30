package com.example.groqagent.services;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;


@Service
public class LangchainGroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private final WeatherService weatherService;
    private Assistant assistant;

    public LangchainGroqService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostConstruct
    public void init() {
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName(model)
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WeatherTools(weatherService))
                .build();
    }

    public String ask(String question) {
        return assistant.chat(question);
    }

    interface Assistant {
        String chat(String userMessage);
    }

    static class WeatherTools {
        private final WeatherService weatherService;

        WeatherTools(WeatherService weatherService) {
            this.weatherService = weatherService;
        }

        @Tool("Get the current temperature in Brussels, Belgium")
        double getBrusselsTemperature() {
            return weatherService.getBrusselsTemperature();
        }
    }
}
