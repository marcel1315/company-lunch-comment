package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.DinerDetailOutputDto;
import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DinerService {

  private final DinerRepository dinerRepository;

  private final S3Manager s3Manager;

  /**
   * 식당 생성
   */
  public void createDiner(CreateDinerDto createDinerDto) {
    saveDiner(createDinerDto.toEntity());
  }

  /**
   * 식당 목록 조회 page는 1부터 시작
   */
  public Page<DinerOutputDto> getDinerList(GetDinerListDto dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage() - 1, // Suppose getting 1-based index from client
        dto.getPageSize(),
        Sort.by(
            Sort.Direction.valueOf(dto.getDinerSort().getDirection()),
            dto.getDinerSort().getField()
        )
    );

    return dinerRepository.findAll(pageable)
        .map(DinerOutputDto::of);
  }

  /**
   * 식당 상세 조회
   */
  public DinerDetailOutputDto getDinerDetail(long id) {
    Diner diner = getDiner(id);
    List<String> dinerImageKeys = diner.getDinerImages().stream().map(DinerImage::getS3Key).toList();
    List<String> imageUrls = new ArrayList<>();
    try {
      imageUrls = s3Manager.getPresignedUrls(dinerImageKeys);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
    }

    return DinerDetailOutputDto.of(diner, imageUrls);
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
    dto.getTags().forEach(diner::addTag);
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
