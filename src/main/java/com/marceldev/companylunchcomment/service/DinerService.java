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
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.exception.MemberUnauthorizedException;
import com.marceldev.companylunchcomment.repository.diner.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DinerService {

  private final S3Manager s3Manager;

  private final DinerImageRepository dinerImageRepository;

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  /**
   * 식당 생성
   */
  @Transactional
  public void createDiner(CreateDinerDto createDinerDto) {
    Company company = getCompany();
    Diner diner = createDinerDto.toEntity();
    diner.setCompany(company);
    dinerRepository.save(diner);
  }

  /**
   * 식당 목록 조회
   */
  public Page<DinerOutputDto> getDinerList(GetDinerListDto dto, Pageable pageable) {
    Company company = getCompany();
    return dinerRepository.getList(company.getId(), dto, pageable);
  }

  /**
   * 식당 상세 조회
   */
  public DinerDetailOutputDto getDinerDetail(long id) {
    Diner diner = getDiner(id);
    List<String> dinerImageKeys = diner.getDinerImages().stream()
        .filter(dinerImage -> !dinerImage.isThumbnail())
        .map(DinerImage::getS3Key)
        .toList();
    List<String> dinerThumbnailKeys = diner.getDinerImages().stream()
        .filter(DinerImage::isThumbnail)
        .map(DinerImage::getS3Key)
        .toList();
    List<String> imageUrls = getImageUrls(dinerImageKeys);
    List<String> thumbnailUrls = getImageUrls(dinerThumbnailKeys);

    Integer distance = dinerRepository.getDistance(diner.getCompany().getId(), diner.getId());
    return DinerDetailOutputDto.of(diner, thumbnailUrls, imageUrls, distance);
  }

  /**
   * 식당 수정
   */
  @Transactional
  public void updateDiner(long id, UpdateDinerDto dto) {
    Diner diner = getDiner(id);
    diner.setLink(dto.getLink());
    diner.setLocation(dto.getLocation());
  }

  /**
   * 식당 제거
   */
  @Transactional
  public void removeDiner(long id) {
    Diner diner = getDiner(id);
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

  @Transactional
  public void addDinerTag(long id, AddDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::addTag);
  }

  @Transactional
  public void removeDinerTag(long id, RemoveDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::removeTag);
  }

  private List<String> getImageUrls(List<String> s3Keys) {
    List<String> imageUrls = new ArrayList<>();
    try {
      imageUrls = s3Manager.getPresignedUrls(s3Keys);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
    }
    return imageUrls;
  }

  /**
   * diner를 반환함. 회원이 식당에 접근 가능한지 확인
   */
  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  /**
   * company를 반환함. 로그인한 회원이 회사를 선택했는지 확인.
   */
  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotExistException();
    }
    return member.getCompany();
  }

  /**
   * member를 반환함
   */
  private Member getMember() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    if (user == null) {
      throw new MemberUnauthorizedException();
    }
    return memberRepository.findByEmail(user.getUsername())
        .orElseThrow(MemberNotExistException::new);
  }
}
