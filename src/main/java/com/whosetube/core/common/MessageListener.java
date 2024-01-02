package com.whosetube.core.common;

import discord4j.core.object.entity.Message;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public abstract class MessageListener {

    public Mono<Void> processCommand(Message eventMessage){

        return Mono.just(eventMessage)
                .filter(message -> {
                    return message.getAuthor().map(user -> !user.isBot()).orElse(false);
                }).flatMap(Message::getChannel)
                .flatMap(messageChannel -> messageChannel.createMessage("FUCK U"))
                .then();

    }

}
