package com.example.groqagent.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class WeatherService {
    private final WebClient webClient = WebClient.create("https://api.open-meteo.com");

    // Brussels coordinates
    private static final double LAT = 50.8503;
    private static final double LON = 4.3517;

    public double getBrusselsTemperature() {
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", LAT)
                        .queryParam("longitude", LON)
                        .queryParam("current", "temperature_2m")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> current = (Map<String, Object>) response.get("current");
        return ((Number) current.get("temperature_2m")).doubleValue();
    }
}
