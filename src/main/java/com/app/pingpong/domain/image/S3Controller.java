package com.app.pingpong.domain.image;

import com.app.pingpong.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_DELETE_AWS_S3;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Uploader s3Uploader;

    @GetMapping("/file")
    public BaseResponse<String> getFile(@RequestParam("name") String fileName) {
        return new BaseResponse<>(s3Uploader.getFilePath(fileName));
    }

    @PostMapping("/file")
    public BaseResponse<List<String>> uploadFile(@RequestPart List<MultipartFile> multipartFile) {
        return new BaseResponse<>(s3Uploader.uploadFiles(multipartFile));
    }

    @DeleteMapping("/file")
    public BaseResponse<String> deleteFile(@RequestParam("name") String fileName) {
        s3Uploader.deleteFile(fileName);
        return new BaseResponse<>(SUCCESS_DELETE_AWS_S3);
    }

}
