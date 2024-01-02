package com.whosetube.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadedMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String filePath;
    private String fileName;
    private String fileExtension;
    private String owner;
    private LocalDateTime uploadedDate;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "uploadedMedia")
    private List<MediaChunks> chunks;
}
