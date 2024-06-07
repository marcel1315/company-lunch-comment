package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.DinerDetailOutputDto;
import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.repository.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DinerService extends AbstractDinerService {

  private final S3Manager s3Manager;

  private final DinerImageRepository dinerImageRepository;

  public DinerService(MemberRepository memberRepository, DinerRepository dinerRepository,
      S3Manager s3Manager, DinerImageRepository dinerImageRepository) {
    super(memberRepository, dinerRepository);
    this.s3Manager = s3Manager;
    this.dinerImageRepository = dinerImageRepository;
  }

  /**
   * 식당 생성
   */
  @Override
  void createDinerAfterCheck(CreateDinerDto createDinerDto, Company company) {
    Diner diner = createDinerDto.toEntity();
    diner.setCompany(company);
    dinerRepository.save(diner);
  }

  /**
   * 식당 목록 조회
   */
  @Override
  Page<DinerOutputDto> getDinerListAfterCheck(GetDinerListDto dto, Company company,
      Pageable pageable) {
    return dinerRepository.findByCompanyId(dto.getCompanyId(), pageable)
        .map(DinerOutputDto::of);
  }

  /**
   * 식당 상세 조회
   */
  @Override
  DinerDetailOutputDto getDinerDetailAfterCheck(long id, Diner diner) {
    List<String> imageUrls = getImageUrls(diner);
    return DinerDetailOutputDto.of(diner, imageUrls);
  }

  /**
   * 식당 수정
   */
  @Override
  void updateDinerAfterCheck(long id, UpdateDinerDto dto, Diner diner) {
    diner.setLink(dto.getLink());
    diner.setLatitude(dto.getLatitude());
    diner.setLongitude(dto.getLongitude());
  }

  /**
   * 식당 제거
   */
  @Override
  void removeDinerAfterCheck(long id, Diner diner) {
    List<String> dinerImageKeys = diner.getDinerImages().stream()
        .map(DinerImage::getS3Key)
        .toList();

    dinerImageRepository.deleteByDinerId(id);
    dinerRepository.delete(diner);

    // diner와 dinerImage가 완전히 지워진 후, s3에 저장된 이미지를 지움
    // s3 이미지 저장 과정에서 실패하더라도, DB에서 제거되었다면 exception을 내지 않고 성공함
    // TODO: DB에서 제거하기 전에 S3 접속이 온전한지 체크하기?
    try {
      dinerImageKeys.forEach(s3Manager::removeFile);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Override
  void addDinerTagAfterCheck(long id, AddDinerTagsDto dto, Diner diner) {
    dto.getTags().forEach(diner::addTag);
  }

  @Override
  void removeDinerTagAfterCheck(long id, RemoveDinerTagsDto dto, Diner diner) {
    dto.getTags().forEach(diner::removeTag);
  }

  private List<String> getImageUrls(Diner diner) {
    List<String> imageUrls = new ArrayList<>();
    List<String> dinerImageKeys = diner.getDinerImages().stream()
        .map(DinerImage::getS3Key)
        .toList();
    try {
      imageUrls = s3Manager.getPresignedUrls(dinerImageKeys);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
    }
    return imageUrls;
  }
}
