package com.whosetube.core.service;

import com.whosetube.core.entity.MediaChunks;
import com.whosetube.core.entity.UploadedMedia;
import com.whosetube.core.model.BaseRequest;
import com.whosetube.core.model.Media;
import com.whosetube.core.repository.ChunkRepository;
import com.whosetube.core.repository.FileRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
public class FileProcessorServiceImpl implements FileProcessorService{

    private GatewayDiscordClient gatewayDiscordClient;
    private FileRepository fileRepository;
    private ChunkRepository chunkRepository;

    @Override
    public void processFile(BaseRequest<Media> mediaBaseRequest) throws IOException {
        Snowflake snowflake = Snowflake.of("1188817673165996096");
        MessageChannel channel = (MessageChannel) gatewayDiscordClient.getChannelById(snowflake).block();
        UploadedMedia media = new UploadedMedia();
        media.setFileName(mediaBaseRequest.getData().getVideoFile().getOriginalFilename());
        media.setFileExtension(media.getFileName().substring(media.getFileName().lastIndexOf(".") + 1));
        media.setOwner("ME");
        media.setUploadedDate(LocalDateTime.now());
        media.setChunks(new ArrayList<>());
        this.fileRepository.save(media);
        splitFile(mediaBaseRequest.getData().getVideoFile(), channel, media);

    }

    public void splitFile(MultipartFile file, MessageChannel channel, UploadedMedia uploadedMedia) throws IOException {
        long written = 0;
        byte[] buffer = new byte[25 * 1024 * 1024];
        List<byte[]> fileBufferList = new ArrayList<>();
        InputStream fileStream = file.getInputStream();
        while((written = fileStream.read(buffer)) != -1){
            int finalWritten = (int) written;
            fileBufferList.add(Arrays.copyOfRange(buffer, 0, finalWritten));
        }
        uploadChunks(fileBufferList, channel, file, uploadedMedia);
        this.fileRepository.save(uploadedMedia);
    }

    public void uploadChunks(List<byte[]> fileBufferList, MessageChannel channel, MultipartFile file, UploadedMedia uploadedMedia){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int startIndex = 0;
        for(int i=0;i<5;i++){
            int endIndex = startIndex + (fileBufferList.size() / 5);
            if(i == 4) endIndex = fileBufferList.size();

            final int finalStartIndex = startIndex;
            final int finalEndIndex = endIndex;
            executorService.submit(() -> {
                uploadChunksToDiscord(fileBufferList, channel, file, uploadedMedia, finalStartIndex, finalEndIndex);
            });
            startIndex = endIndex;
        }
    }

    private void uploadChunksToDiscord(List<byte[]> fileBufferList, MessageChannel channel, MultipartFile file,
                                       UploadedMedia uploadedMedia, int startIndex, int endIndex) {

        for(int i=startIndex;i<endIndex;i++){
            String chunkName = file.getOriginalFilename() + ".part" + String.format("%03d", i + 1);
            Message message = channel.createMessage(MessageCreateSpec.builder()
                    .addFile(chunkName,new ByteArrayInputStream(fileBufferList.get(i)))
                    .build()).block();

            MediaChunks mediaChunks = new MediaChunks();
            mediaChunks.setChunkUrl(message.getAttachments().getFirst().getUrl());
            mediaChunks.setUploadedMedia(uploadedMedia);
            mediaChunks.setChunkName(chunkName);
            uploadedMedia.getChunks().add(mediaChunks);
            this.chunkRepository.save(mediaChunks);
        }

    }

    public void mergeFile(MessageChannel channel, String fileName) throws IOException {
        UploadedMedia media = this.fileRepository.findByFileName(fileName);
        List<MediaChunks> chunks = media.getChunks();

        FileOutputStream fos = new FileOutputStream(fileName);
        for(MediaChunks chunk : chunks){
            try(BufferedInputStream fileStream = new BufferedInputStream(new URL(chunk.getChunkUrl()).openStream())){
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

    @Autowired
    public void setGatewayDiscordClient(GatewayDiscordClient gatewayDiscordClient){
        this.gatewayDiscordClient = gatewayDiscordClient;
    }

    @Autowired
    public void setFileRepository(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Autowired
    public void setChunkRepository(ChunkRepository chunkRepository){
        this.chunkRepository = chunkRepository;
    }

}
