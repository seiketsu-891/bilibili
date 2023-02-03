package com.bili.api;

import com.bili.domain.JsonResponse;
import com.bili.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
public class FileApi {
    @Resource
    private FileService fileService;

    /**
     * generate md5 strings for a file
     */
    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception {
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }


    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile fileSlice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        String path = fileService.uploadFileBySlices(fileSlice, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(path);
    }
}
