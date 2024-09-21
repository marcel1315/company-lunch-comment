package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import com.marceldev.ourcompanylunch.exception.common.InternalServerErrorException;
import com.marceldev.ourcompanylunch.exception.diner.DinerImageNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerMaxImageCountExceedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.ImageDeleteFailException;
import com.marceldev.ourcompanylunch.exception.diner.ImageUploadFailException;
import com.marceldev.ourcompanylunch.exception.diner.ImageWithNoExtensionException;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.util.FileUtil;
import com.marceldev.ourcompanylunch.util.MakeThumbnailUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DinerImageService {

  @Value("${s3.diner-max-image-count}")
  private int dinerMaxImageCount;

  private final DinerRepository dinerRepository;

  private final DinerImageRepository dinerImageRepository;

  private final S3Manager s3Manager;

  /**
   * Order value of image is defined by the most value of an existing image's order + 100. Create a
   * thumbnail and save it in S3.
   */
  @Transactional
  public void addDinerImage(long dinerId, MultipartFile image) {
    Diner diner = getDiner(dinerId);
    checkMaxImageCount(diner);
    String extension = FileUtil.getExtension(image)
        .orElseThrow(ImageWithNoExtensionException::new);

    // Use BufferedInputStream to reuse. (Normally, input stream is used once.)
    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(
        image.getInputStream())) {
      // Set a large read limit to prevent the mark from being invalidated.
      // Thus, I can call reset. (If the mark is invalidated, I can't call reset.)
      bufferedInputStream.mark(Integer.MAX_VALUE);

      // Create a thumbnail
      bufferedInputStream.reset();
      ByteArrayOutputStream resizedOutputStream = MakeThumbnailUtil.resizeFile(
          bufferedInputStream, extension
      );
      InputStream thumbnailInputStream = new ByteArrayInputStream(
          resizedOutputStream.toByteArray()
      );

      // Upload original and thumbnail image in S3.
      bufferedInputStream.reset();
      String keyImage = genDinerImageKey(dinerId, extension, false);
      uploadDinerImageToStorage(
          keyImage, bufferedInputStream, extension, image.getSize()
      );
      String keyThumbnail = genDinerImageKey(dinerId, extension, true);
      uploadDinerImageToStorage(
          keyThumbnail, thumbnailInputStream, extension, resizedOutputStream.size()
      );

      // Save image info in DB.
      saveDinerImage(diner, keyImage, false);
      saveDinerImage(diner, keyThumbnail, true);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public void removeDinerImage(long imageId) {
    DinerImage dinerImage = dinerImageRepository.findById(imageId)
        .orElseThrow(() -> new DinerImageNotFoundException(imageId));
    String key = dinerImage.getS3Key();

    deleteDinerImageFromStorage(key);
    try {
      dinerImageRepository.delete(dinerImage);
    } catch (RuntimeException e) {
      // What if the image in S3 is removed and the image info remains in DB?
      // -> When calling deleteDinerImageFromStorage, don't throw even if the file doesn't exist.
      // TODO: How can I handle this more gracefully?
      throw new InternalServerErrorException("Fail to remove a diner image info");
    }
  }

  private void uploadDinerImageToStorage(String key, InputStream inputStream, String extension,
      long size) {
    try {
      s3Manager.uploadFile(key, inputStream, size);
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new ImageUploadFailException(key);
    }
  }

  private void deleteDinerImageFromStorage(String key) {
    try {
      s3Manager.removeFile(key);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      throw new ImageDeleteFailException(key);
    }
  }

  private void saveDinerImage(Diner diner, String key, boolean thumbnail) {
    try {
      DinerImage dinerImage = DinerImage.builder()
          .s3Key(key)
          .orders(getNextImageOrder(diner))
          .diner(diner)
          .thumbnail(thumbnail)
          .build();
      dinerImageRepository.save(dinerImage);
    } catch (RuntimeException e) {
      throw new InternalServerErrorException("Fail to save a diner image");
    }
  }

  private int getNextImageOrder(Diner diner) {
    // Set the unit of order step to 100 because unit 1 could make changing order logic complex. Changing order is not implemented yet though.
    int orderStep = 100;

    return dinerImageRepository.findTopByDinerOrderByOrdersDesc(diner)
        .map(DinerImage::getOrders)
        .map((value) -> value + orderStep)
        .orElse(orderStep);
  }

  private void checkMaxImageCount(Diner diner) {
    int count = dinerImageRepository.countByDinerAndThumbnail(diner, false);
    if (count >= dinerMaxImageCount) {
      throw new DinerMaxImageCountExceedException();
    }
  }

  private String genDinerImageKey(long dinerId, String extension, boolean thumbnail) {
    if (thumbnail) {
      return "diner/" + dinerId + "/thumbnails/" + UUID.randomUUID() + "." + extension;
    } else {
      return "diner/" + dinerId + "/images/" + UUID.randomUUID() + "." + extension;
    }
  }

  private Diner getDiner(long id) {
    return dinerRepository.findById(id)
        .orElseThrow(() -> new DinerNotFoundException(id));
  }
}
