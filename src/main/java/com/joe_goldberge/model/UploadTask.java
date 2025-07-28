package com.joe_goldberge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadTask {
    private String url;         // Media file URL
    private String fileName;    // New file name after renaming
    private String userId;
    private String firstName;
}

