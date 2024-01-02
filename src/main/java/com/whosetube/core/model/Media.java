package com.whosetube.core.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Media {

    MultipartFile videoFile;
    String request;

}
