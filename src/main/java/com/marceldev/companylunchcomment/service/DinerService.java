package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.DuplicateDinerTagException;
import com.marceldev.companylunchcomment.exception.ImageUploadFail;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.exception.DinerMaxImageCountExceedException;
import com.marceldev.companylunchcomment.repository.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DinerService {

  @Value("${s3.diner-max-image-count}")
  private int dinerMaxImageCount;

  private final DinerRepository dinerRepository;

  private final DinerImageRepository dinerImageRepository;

  private final S3Manager s3Manager;

  /**
   * 식당 생성
   */
  public void createDiner(CreateDinerDto createDinerDto) {
    saveDiner(createDinerDto.toEntity());
  }

  /**
   * 식당 수정
   */
  public void updateDiner(long id, UpdateDinerDto dto) {
    Diner diner = getDiner(id);

    if (dto.getLink() != null) {
      diner.setLink(dto.getLink());
    }
    if (dto.getLatitude() != null) {
      diner.setLatitude(dto.getLatitude());
    }
    if (dto.getLongitude() != null) {
      diner.setLongitude(dto.getLongitude());
    }

    saveDiner(diner);
  }

  /**
   * 식당 태그 추가
   */
  @Transactional
  public void addDinerTag(long id, AddDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach((tag) -> {
      if (!diner.getTags().contains(tag)) {
        diner.addTag(tag);
      } else {
        throw new DuplicateDinerTagException(tag);
      }
    });

    saveDiner(diner);
  }

  /**
   * 식당 태그 삭제
   */
  public void removeDinerTag(long id, RemoveDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::removeTag);
    saveDiner(diner);
  }

  /**
   * 식당 사진 추가
   * 사진의 순서값은 이미 있는 사진의 가장 큰 값에 +100씩 함
   */
  public void addDinerImage(long id, MultipartFile file) {
    Diner diner = getDiner(id);

    try {
      checkMaxImageCount(diner);

      String key = s3Manager.uploadFile(id, file);
      DinerImage dinerImage = DinerImage.builder()
          .link(key)
          .orders(getNextImageOrder(diner))
          .diner(diner)
          .build();
      dinerImageRepository.save(dinerImage);

    } catch (IOException e) {
      log.error(e.getMessage());
      throw new ImageUploadFail(file.getOriginalFilename());
    }
  }

  private Diner getDiner(long id) {
    return dinerRepository.findById(id)
        .orElseThrow(() -> new DinerNotFoundException(id));
  }

  private void saveDiner(Diner diner) {
    try {
      dinerRepository.save(diner);
    } catch (RuntimeException e) {
      throw new InternalServerError("식당 저장 실패");
    }
  }

  private int getNextImageOrder(Diner diner) {
    // 1개씩 order가 붙어있으면 order 수정시에 여러 image들의 order를 수정해야하므로 간격을 줌
    int orderStep = 100;

    return dinerImageRepository.findTopByDinerOrderByOrdersDesc(diner)
        .map(DinerImage::getOrders)
        .map((value) -> value + orderStep)
        .orElse(orderStep);
  }

  private void checkMaxImageCount(Diner diner) {
    int count = dinerImageRepository.countByDiner(diner);
    if (count >= dinerMaxImageCount) {
      throw new DinerMaxImageCountExceedException();
    }
  }
}
