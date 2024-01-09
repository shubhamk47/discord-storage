package com.whosetube.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "media_chunks")
public class MediaChunks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "uploaded_media_id")
    private UploadedMedia uploadedMedia;
    private String chunkUrl;
    private String chunkName;


}