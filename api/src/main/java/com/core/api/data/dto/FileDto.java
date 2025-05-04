package com.core.api.data.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDto {
    String filename;
    String fileSha;
    String status;
    int additions;
    int deletions;
    int changes;
    String patch;
    String content;
}
