package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.MediaEntity;
import com.mindsoccer.protocol.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, UUID> {

    List<MediaEntity> findByMediaType(MediaType mediaType);

    Page<MediaEntity> findByMediaType(MediaType mediaType, Pageable pageable);

    boolean existsByUrl(String url);
}
