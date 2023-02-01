package com.bili.service.util;

import com.bili.domain.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastDFSUtil {
    private final String DEFAULT_GROUP = "group_1";
    private final String PATH_KEY = "path-key:";
    private final String UPLOADED_SIZE_KEY = "uploaded-size-key";
    private final String UPLOADED_NO_KEY = "uploaded-no-key";
    @Resource
    private FastFileStorageClient storageClient;
    @Resource
    private AppendFileStorageClient appendFileStorageClient;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

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

    /**
     * large file slice upload + breakpoint resume upload
     */
    public String uploadAppenderFile(MultipartFile file) throws IOException {
        String fileType = getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    public void modifyAppenderFile(MultipartFile file, String path, Long offset) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, path, file.getInputStream(), file.getSize(), offset);
    }

    /**
     * Upload a large file by slices
     * This method will be repeatedly executed until the full large file is uploaded.
     * In practical usage,this implementation is typically handled by front-end developers.
     * However, for the purposes of learning, we have included its implementation in this demonstration.
     */
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if (file == null || sliceNo == null || totalSliceNo == null) {
            throw new ConditionException("Illegal arguments");
        }

        String pathKey = PATH_KEY + fileMd5;
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        if (!StringUtil.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }

        if (sliceNo == 1) {
            String path = this.uploadAppenderFile(file);
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("Upload failed");
            }
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else {
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("Upload failed");
            }
            this.modifyAppenderFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        uploadedSize += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));

        Integer uploadedNo = Integer.valueOf(redisTemplate.opsForValue().get(uploadedNoKey)); // the number of slices that have been uploaded
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) { // all the slices have been uploaded
            resultPath = redisTemplate.opsForValue().get(pathKey);
            // clear data in redis
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    public void deleteFile(String filePath) {
        storageClient.deleteFile(filePath);
    }
}
