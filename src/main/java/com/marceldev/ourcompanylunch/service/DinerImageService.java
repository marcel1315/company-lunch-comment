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
   * 식당 이미지 추가 이미지의 순서값은 이미 있는 이미지의 가장 큰 값에 +100씩 함 썸네일을 생성해서 별도로 저장
   */
  @Transactional
  public void addDinerImage(long dinerId, MultipartFile image) {
    Diner diner = getDiner(dinerId);
    checkMaxImageCount(diner);
    String extension = FileUtil.getExtension(image)
        .orElseThrow(ImageWithNoExtensionException::new);

    // 하나의 InputStream 을 두번 쓸 수는 없어서 BufferedInputStream 을 사용
    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(
        image.getInputStream())) {
      // readLimit 을 크게 잡아 mark 가 invalidated 되지 않도록 함. reset 해서 처음 위치로 가기
      bufferedInputStream.mark(Integer.MAX_VALUE);

      // 썸네일 생성
      bufferedInputStream.reset();
      ByteArrayOutputStream resizedOutputStream = MakeThumbnailUtil.resizeFile(
          bufferedInputStream, extension
      );
      InputStream thumbnailInputStream = new ByteArrayInputStream(
          resizedOutputStream.toByteArray()
      );

      // S3 로 원본과 썸네일 업로드
      bufferedInputStream.reset();
      String keyImage = genDinerImageKey(dinerId, extension, false);
      uploadDinerImageToStorage(
          keyImage, bufferedInputStream, extension, image.getSize()
      );
      String keyThumbnail = genDinerImageKey(dinerId, extension, true);
      uploadDinerImageToStorage(
          keyThumbnail, thumbnailInputStream, extension, resizedOutputStream.size()
      );

      // DB 에 이미지 정보 저장
      saveDinerImage(diner, keyImage, false);
      saveDinerImage(diner, keyThumbnail, true);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 식당 이미지 제거
   */
  @Transactional
  public void removeDinerImage(long imageId) {
    // dinerId는 추후에 사용자 개념이 들어오면, diner를 지울 수 있는지 확인할 때 쓰려고 남겨놓음

    DinerImage dinerImage = dinerImageRepository.findById(imageId)
        .orElseThrow(() -> new DinerImageNotFoundException(imageId));
    String key = dinerImage.getS3Key();

    deleteDinerImageFromStorage(key);
    try {
      dinerImageRepository.delete(dinerImage);
    } catch (RuntimeException e) {
      //TODO: 저장소에 이미지는 지워지고, 이미지 정보만 DB에 남게되면 어떻게 하지?
      // -> 파일이 존재하지 않으면 deleteDinerImageFromStorage에서 throw하지 않기
      throw new InternalServerErrorException("이미지 정보 DB삭제 실패");
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
      throw new InternalServerErrorException("식당 이미지 저장 실패");
    }
  }

  private int getNextImageOrder(Diner diner) {
    // 1개씩 order 가 붙어있으면 order 수정시에 여러 image 들의 order 를 수정해야하므로 간격을 줌
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
