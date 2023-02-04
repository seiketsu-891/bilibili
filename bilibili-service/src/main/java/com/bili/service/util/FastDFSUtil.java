package com.bili.service.util;

import com.bili.domain.exception.ConditionException;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Component
public class FastDFSUtil {
    private static final String DEFAULT_GROUP = "group_1";
    private static final String PATH_KEY = "path-key:";
    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key";
    private static final String UPLOADED_NO_KEY = "uploaded-no-key";
    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;
    @Resource
    private FastFileStorageClient storageClient;
    @Resource
    private AppendFileStorageClient appendFileStorageClient;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Upload a large file by slices
     * This method will be repeatedly executed until the full large file is uploaded.
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

    public String getFileType(MultipartFile file) {
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
     * Divide a file into multiple small files
     * In practical usage,this implementation is typically handled by front-end developers.
     * However, for the purposes of learning, we have included its implementation in this demonstration.
     */
    public void divideFileIntoSlices(MultipartFile multipartFile) throws Exception {
        final int SLICE_SIZE = 1024 * 1024 * 2;
        String fileType = this.getFileType(multipartFile); // need this to manually add extension to divided small files
        File file = this.convertMultipleFileToFile(multipartFile); // temp file
        int fileNo = 1; // used for directory name

        for (int currBytePosition = 0; currBytePosition < file.length(); currBytePosition += SLICE_SIZE) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(currBytePosition);  // read from currBytePosition
            byte[] readBytes = new byte[SLICE_SIZE];
            int readLen = randomAccessFile.read(readBytes);

            FileOutputStream os = new FileOutputStream("/Downloads/" + fileNo + "." + fileType);
            os.write(readBytes, 0, readLen);

            os.close();
            randomAccessFile.close();
            fileNo++;
        }
        // delete temp file
        file.delete();
    }

    public void deleteFile(String filePath) {
        storageClient.deleteFile(filePath);
    }

    private File convertMultipleFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String[] nameAndExtension = originalFilename.split("\\.");
        File tempFile = File.createTempFile(nameAndExtension[0], "." + nameAndExtension[1]);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public void getVideosOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String relativePath) throws Exception {
        // get file info
        FileInfo fileInfo = storageClient.queryFileInfo(DEFAULT_GROUP, relativePath);
        long fileSizeTotal = fileInfo.getFileSize();

        // get headers from request
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String h = headerNames.nextElement();
            headers.put(h, request.getHeader(h));
        }

        // set range
        // The Range HTTP request header indicates the part of a document that the server should return
        // This header is commonly used for video streaming and partial file downloads.
        String range = request.getHeader("Range");

        if (StringUtil.isNullOrEmpty(range)) {
            range = "bytes=0-" + (fileSizeTotal - 1);
        }
        // The line of code below will split "range" to 2 to 3 parts.
        // ex: "bytes=0-1" will be split into ["", "0", "1"];
        // ex: "bytes=0" will be split into["", "0"];
        String[] rangeData = range.split("bytes=|-");

        long rangeStart = 0, rangeEnd = fileSizeTotal - 1;
        if (rangeData.length >= 2) {
            rangeStart = Long.parseLong(rangeData[1]);
        }
        if (rangeData.length >= 3) {
            rangeEnd = Long.parseLong(rangeData[2]);
        }

        // set information in header
        long len = rangeEnd - rangeStart + 1;
        String contentRange = "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSizeTotal;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // status 206
        // The HTTP 206 Partial Content success status response code indicates that
        // the request has succeeded and the body contains the requested ranges of data,
        // as described in the Range header of the request.

        String url = httpFdfsStorageAddr + relativePath;
        // retrieves the HTTP resource(a portion of video) specified by the url argument
        // and writes its contents to an OutputStream passed as an argument response.getOutputStream().
        HttpUtil.get(url, headers, response);
    }
}
