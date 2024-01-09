package com.whosetube.core.service;

import com.whosetube.core.model.BaseRequest;
import com.whosetube.core.model.Media;
import discord4j.core.object.entity.channel.MessageChannel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public interface FileProcessorService {

    void processFile(BaseRequest<Media> mediaBaseRequest) throws IOException;
    void mergeFile(MessageChannel channel, String fileName) throws IOException;

}
