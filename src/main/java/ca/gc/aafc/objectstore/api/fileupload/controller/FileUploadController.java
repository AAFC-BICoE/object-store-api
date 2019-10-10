package ca.gc.aafc.objectstore.api.fileupload.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import ca.gc.aafc.objectstore.api.fileupload.payload.FileUploadResponse;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;

@RestController
@RequestMapping("/api/v1")
public class FileUploadController {

  private final MinioFileService minioService;

  public FileUploadController(MinioFileService minioService) {
    this.minioService = minioService;
  }

  @PostMapping("/file/{bucket}")
  public FileUploadResponse handleFileUpload(@RequestParam("file") MultipartFile file,
      @PathVariable String bucket) throws InvalidKeyException, NoSuchAlgorithmException,
      InvalidBucketNameException, NoResponseException, ErrorResponseException, InternalException,
      InvalidArgumentException, InsufficientDataException, InvalidResponseException,
      RegionConflictException, InvalidEndpointException, InvalidPortException, IOException,
      XmlPullParserException, URISyntaxException {

    minioService.storeFile(file.getOriginalFilename(), file.getInputStream(), bucket);
    
    return new FileUploadResponse(file.getOriginalFilename(), file.getContentType(),
        file.getSize());
  }

}
