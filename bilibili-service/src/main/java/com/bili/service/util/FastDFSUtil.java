package com.bili.service.util;

import com.bili.domain.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FastDFSUtil {
    @Resource
    FastFileStorageClient storageClient;

    private String getFileType(MultipartFile file) {
        if (file == null) {
            throw new ConditionException("Illegal file");
        }

        String fileName = file.getName();
        int extensionStartIndex = fileName.lastIndexOf(".") + 1;
        return fileName.substring(extensionStartIndex);
    }

    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData> metaDataSet = new HashSet<>();
        String type = getFileType(file);
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), type, metaDataSet);
        return storePath.getPath();
    }

    public void deleteFile(String filePath) {
        storageClient.deleteFile(filePath);
    }
}
