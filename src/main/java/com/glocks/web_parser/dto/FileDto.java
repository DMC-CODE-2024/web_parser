package com.glocks.web_parser.dto;


import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class FileDto {
    
    String fileName;
    String filePath;
    long totalRecords;
    long successRecords;
    long failedRecords;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public FileDto(String fileName, String filePath, long totalRecords, long successRecords, long failedRecords) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.totalRecords = totalRecords;
        this.successRecords = successRecords;
        this.failedRecords = failedRecords;
    }

    public FileDto(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        long fileRecordCount = getFileRecordCount(filePath + "/" + fileName);
        this.totalRecords = fileRecordCount > 0 ? fileRecordCount-1 : fileRecordCount; // to remove header count
        this.successRecords = 0;
        this.failedRecords = 0;
    }


    public long getFileRecordCount(String file) {
        try {
            File file1 = new File(file);
            logger.info("Getting the file size for file {}", file1.toURI());
            Path pathFile = Paths.get(file1.toURI());
            return (long) Files.lines(pathFile).filter(line -> line.length() > 0).count(); // to remove empty lines from count.
//            return (long) Files.lines(pathFile).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }


}
