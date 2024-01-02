package com.whosetube.core.service;

import com.whosetube.core.model.BaseRequest;
import com.whosetube.core.model.Media;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class FileProcessorServiceImpl implements FileProcessorService{

    private GatewayDiscordClient gatewayDiscordClient;

    @Override
    public void processFile(BaseRequest<Media> mediaBaseRequest) throws IOException {
        Snowflake snowflake = Snowflake.of("1188817673165996096");
        MessageChannel channel = (MessageChannel) gatewayDiscordClient.getChannelById(snowflake).block();

        if(mediaBaseRequest.getData().getVideoFile().getSize() / (1024.0 * 1024) > 25){
            splitFile(mediaBaseRequest.getData().getVideoFile(), channel);
            log.info("FILE ADDED");
        }

    }

    public void splitFile(MultipartFile file, MessageChannel channel) throws IOException {
        int chunkNumber = 0;
        byte[] toWrite = file.getBytes();
        int i = 0;
        while(i < toWrite.length){
            String chunkName = file.getName() + ".part" + String.format("%03d", chunkNumber++);
            final int x = i;
            if(i + 25 * 1024 * 1024 + 1 < toWrite.length){
                channel.createMessage(spec -> spec.addFile(chunkName, new ByteArrayInputStream(Arrays.copyOfRange(toWrite, x, (x + 25 * 1024 * 1024))))).block();
            }else{
                channel.createMessage(spec -> spec.addFile(chunkName, new ByteArrayInputStream(Arrays.copyOfRange(toWrite, x, toWrite.length)))).block();
            }
            i += 25 * 1024 * 1024;
        }

    }

    public void mergeFile(MessageChannel channel) throws IOException {
        List<String> urls = Arrays.asList("https://cdn.discordapp.com/attachments/1188817673165996096/1191032399962853406/videoFile.part000?ex=65a3f65c&is=6591815c&hm=28e09ae3428b9b52864338786641f2087098d01632ea42eee6acb3c0fbbb5fd4&",
                "https://cdn.discordapp.com/attachments/1188817673165996096/1191032419743174657/videoFile.part001?ex=65a3f661&is=65918161&hm=ff4f4daa039d5cac9d676e9b240ca62442c7959ace451e486abc69b40e8673cf&");
        FileOutputStream fos = new FileOutputStream("test.mp4");
        for(String url : urls){
            try(BufferedInputStream fileStream = new BufferedInputStream(new URL(url).openStream())){
                byte[] buffer = new byte[4096];
                int bytesRead;
                ByteArrayOutputStream opStream = new ByteArrayOutputStream();
                while((bytesRead = fileStream.read(buffer)) != -1){
                    opStream.write(buffer, 0, bytesRead);
                }
                fileStream.close();
                opStream.close();
                fos.write(opStream.toByteArray());
                log.info("CHUNK DOWNLOADED");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        fos.close();
    }

    public void uploadFile(MessageChannel channel, File chunk, byte[] buffer){
        channel.createMessage(spec -> spec.addFile(chunk.getName(), new ByteArrayInputStream(buffer))).block();
    }

    @Autowired
    public void setGatewayDiscordClient(GatewayDiscordClient gatewayDiscordClient){
        this.gatewayDiscordClient = gatewayDiscordClient;
    }

}
