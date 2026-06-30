# Groq Weather Agent

A small Spring Boot project demonstrating an AI agent that answers questions about the current temperature in Brussels, Belgium. It calls the [Groq](https://groq.com) LLM API (OpenAI-compatible) and uses **tool calling** so the model can fetch live weather data instead of guessing.

Two implementations are included so you can compare approaches:

1. **Hand-rolled agent** (`/agent/ask`) — raw HTTP calls to Groq's API via `WebClient`, with manual JSON request/response handling and a manual tool-call loop.
2. **LangChain4j agent** (`/agent/ask-langchain`) — the same behavior built using [LangChain4j](https://docs.langchain4j.dev/)'s `AiServices` and `@Tool` annotation, which automates schema generation and the tool-call loop.

Both agents use the same weather data source: [Open-Meteo](https://open-meteo.com/) (free, no API key required).

## How it works

1. You send a question to one of the endpoints (e.g. *"What's the temperature in Brussels right now?"*).
2. The question is sent to a Groq-hosted LLM, along with a tool definition for `get_temperature`.
3. The model decides it needs current weather data and requests the tool be called.
4. The app calls Open-Meteo for live data on Brussels and returns the result to the model.
5. The model produces a final natural-language answer using that real data.

## Requirements

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- A free [Groq API key](https://console.groq.com)

## Setup

1. Clone or unzip the project.
2. Export your Groq API key as an environment variable:

   ```bash
   export GROQ_API_KEY=your_real_key_here
   ```

3. Check `src/main/resources/application.properties` and confirm the model name is still valid (Groq's available models change over time — check [console.groq.com](https://console.groq.com) if you hit a "model not found" error):

   ```properties
   groq.api.key=${GROQ_API_KEY}
   groq.api.url=https://api.groq.com/openai/v1/chat/completions
   groq.model=llama-3.3-70b-versatile
   ```

## Build & Run

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` by default.

## Usage

**Hand-rolled agent:**

```bash
curl "http://localhost:8080/agent/ask?question=What%27s+the+temperature+in+Brussels+right+now%3F"
```

**LangChain4j agent:**

```bash
curl "http://localhost:8080/agent/ask-langchain?question=What%27s+the+temperature+in+Brussels+right+now%3F"
```

Both should return a response along the lines of:

```
The current temperature in Brussels is approximately X°C.
```

## Project structure

```
src/main/java/com/example/groqagent/
├── GroqWeatherAgentApplication.java   # Spring Boot entry point
├── controllers/
│   └── AgentController.java           # REST endpoints
└── services/
    ├── WeatherService.java            # Fetches live temperature from Open-Meteo
    ├── GroqAgentService.java          # Hand-rolled Groq client + tool-call loop
    └── LangchainGroqService.java      # LangChain4j-based agent (same behavior)
```

## Notes

- Tool-calling behavior can vary slightly with phrasing when `tool_choice` is set to `"auto"`, since the model decides for itself whether a tool is needed. If you want deterministic behavior for a single-purpose agent like this one, force the tool with `tool_choice` set to the specific function, and/or add a system message instructing the model to always use the tool for weather questions.
- LangChain4j is not natively built for Groq, but since Groq's API is OpenAI-compatible, the `langchain4j-open-ai` module works by pointing `baseUrl` at `https://api.groq.com/openai/v1`.
- This project is for learning/demo purposes — error handling, retries, and conversation memory are minimal.