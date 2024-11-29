
package com.project.tailsroute.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileResource extends ByteArrayResource {

    private final String filename;

    public MultipartFileResource(MultipartFile file) throws IOException {
        super(file.getBytes());
        this.filename = file.getOriginalFilename();
    }

    @Override
    public String getFilename() {
        return this.filename; // MultipartFile의 파일 이름 반환
    }
}
