package com.bili.service;

import com.bili.dao.FileDao;
import com.bili.domain.File;
import com.bili.service.util.FastDFSUtil;
import com.bili.service.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class FileService {
    @Resource
    private FileDao fileDao;
    @Resource
    private FastDFSUtil fastDFSUtil;

    public String uploadFileBySlices(MultipartFile fileSlice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        // If the same file has been uploaded, we don't need to upload again.
        File fileDb = fileDao.getFileByMD5(fileMd5);
        if (fileDb != null) {
            return fileDb.getUrl();
        }

        String filePath = fastDFSUtil.uploadFileBySlices(fileSlice, fileMd5, sliceNo, totalSliceNo);
        if (!StringUtil.isNullOrEmpty(filePath)) {
            File newFile = new File();
            newFile.setMd5(fileMd5);
            newFile.setType(fastDFSUtil.getFileType(fileSlice));
            newFile.setUrl(filePath);
            newFile.setCreateTime(new Date());
            fileDao.addFile(newFile);
        }
        return filePath;
    }

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }
}
