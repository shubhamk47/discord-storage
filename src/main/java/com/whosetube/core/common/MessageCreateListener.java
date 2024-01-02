package com.whosetube.core.common;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class MessageCreateListener extends MessageListener implements EventListener<MessageCreateEvent> {

    @Override
    public Class<MessageCreateEvent> getEventType(){
        return MessageCreateEvent.class;
    }

    public Mono<Void> execute(MessageCreateEvent event){
        log.info("MESSAGE :: {}", event.getMessage().getAttachments().getFirst().getUrl());
        return processCommand(event.getMessage());
    }

}
