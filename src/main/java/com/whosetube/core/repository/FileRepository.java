package com.whosetube.core.repository;

import com.whosetube.core.entity.UploadedMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<UploadedMedia, UUID> {

    UploadedMedia findByFileName(String fileName);

}
