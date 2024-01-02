package com.whosetube.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whosetube.core.model.BaseRequest;
import com.whosetube.core.model.BaseResponse;
import com.whosetube.core.model.Media;
import com.whosetube.core.service.FileProcessorService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;

@Log4j2
@RestController
@RequestMapping("/api/media/upload")
public class MediaUploaderController {

    private ObjectMapper objectMapper;
    private FileProcessorService fileProcessorService;
    @Autowired
    private GatewayDiscordClient gatewayDiscordClient;

    @PostMapping(value = "/video")
    public ResponseEntity<?> uploadVideo(@RequestParam("baseRequest") String toUpload,
                                         @RequestParam("videoFile") MultipartFile videoFile) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(BaseRequest.class, Media.class);
        BaseRequest<Media> mediaBaseRequest = this.objectMapper.readValue(toUpload, javaType);
        mediaBaseRequest.getData().setVideoFile(videoFile);
        log.info("To upload - {}", mediaBaseRequest);
        this.fileProcessorService.processFile(mediaBaseRequest);
        return ResponseEntity.ok("uploaded");
    }

    @GetMapping(value = "/video")
    public ResponseEntity<?> downloadVideo() throws IOException {
        Snowflake snowflake = Snowflake.of("1188817673165996096");
        MessageChannel channel = (MessageChannel) gatewayDiscordClient.getChannelById(snowflake).block();
        this.fileProcessorService.mergeFile(channel);
        return ResponseEntity.ok("downloaded");
    }

    @GetMapping("/")
    public ResponseEntity<BaseResponse<String>> helloWorld(@RequestBody(required = false) @NotNull BaseRequest<?> request) throws InterruptedException {
        log.info("request - {}", request.toString());
        Thread.sleep(5000);
        return ResponseEntity.ok(new BaseResponse<String>(
                request.getRequestId(),
                request.getRequestTime(),
                LocalDateTime.now(),
                "Hello World!"
        ));
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setFileProcessorService(FileProcessorService fileProcessorService){
        this.fileProcessorService = fileProcessorService;
    }

}
