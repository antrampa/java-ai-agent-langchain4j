package com.example.groqagent.controllers;

import com.example.groqagent.services.GroqAgentService;
import com.example.groqagent.services.LangchainGroqService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final GroqAgentService groqAgentService;
    private final LangchainGroqService langchainGroqService;

    public AgentController(GroqAgentService groqAgentService, LangchainGroqService langchainGroqService) {
        this.groqAgentService = groqAgentService;
        this.langchainGroqService = langchainGroqService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) throws Exception {
        return groqAgentService.ask(question);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String question) throws Exception {
        return "Hello There: " + question;
    }

    @GetMapping("/ask-langchain")
    public String askLangchain(@RequestParam String question) {
        return langchainGroqService.ask(question);
    }
}
