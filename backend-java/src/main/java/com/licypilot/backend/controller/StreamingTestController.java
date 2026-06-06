package com.licypilot.backend.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StreamingTestController {
    private final ChatModel chatModel;

    public StreamingTestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping(value = "/api/test-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testStream() {
        return chatModel.stream("Conte até 10 como um pirata.");
    }
}
