package com.whosetube.core.repository;

import com.whosetube.core.entity.MediaChunks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChunkRepository extends JpaRepository<MediaChunks, UUID> {
}
