package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.DuplicateDinerTagException;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DinerService {

  private final DinerRepository dinerRepository;

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
}
