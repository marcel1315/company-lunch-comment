package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.diner.AddDinerTagsDto;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerDto;
import com.marceldev.ourcompanylunch.dto.diner.DinerDetailOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListDto;
import com.marceldev.ourcompanylunch.dto.diner.RemoveDinerTagsDto;
import com.marceldev.ourcompanylunch.dto.diner.UpdateDinerDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.AlreadySubscribedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerSubscriptionNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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

  private final DinerSubscriptionRepository dinerSubscriptionRepository;

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
  public Page<DinerOutputDto> getDinerList(GetDinerListDto dto) {
    Company company = getCompany();
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
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

    // diner 와 dinerImage 가 완전히 지워진 후, s3에 저장된 이미지를 지움
    // s3 이미지 저장 과정에서 실패하더라도, DB 에서 제거되었다면 exception 을 내지 않고 성공함
    // TODO: DB 에서 제거하기 전에 S3 접속이 온전한지 체크하기?
    try {
      dinerImageKeys.forEach(s3Manager::removeFile);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  /**
   * 태그 추가
   */
  @Transactional
  public void addDinerTag(long id, AddDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::addTag);
  }

  /**
   * 태그 제거
   */
  @Transactional
  public void removeDinerTag(long id, RemoveDinerTagsDto dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::removeTag);
  }

  /**
   * 식당 구독
   */
  @Transactional
  public void subscribeDiner(long id) {
    Diner diner = getDiner(id);
    Member member = getMember();

    if (dinerSubscriptionRepository.existsByDinerAndMember(diner, member)) {
      throw new AlreadySubscribedException();
    }

    dinerSubscriptionRepository.save(DinerSubscription.builder()
        .diner(diner)
        .member(member)
        .build());
  }

  /**
   * 식당 구독 취소
   */
  @Transactional
  public void unsubscribeDiner(long id) {
    Diner diner = getDiner(id);
    Member member = getMember();

    DinerSubscription dinerSubscription = dinerSubscriptionRepository.findByDinerAndMember(diner,
            member)
        .orElseThrow(DinerSubscriptionNotFoundException::new);

    dinerSubscriptionRepository.delete(dinerSubscription);
  }

  private List<String> getImageUrls(List<String> s3Keys) {
    List<String> imageUrls = new ArrayList<>();
    try {
      imageUrls = s3Manager.getUrls(s3Keys);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
    }
    return imageUrls;
  }

  /**
   * diner 를 반환함. 회원이 식당에 접근 가능한지 확인
   */
  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  /**
   * company 를 반환함. 로그인한 회원이 회사를 선택했는지 확인.
   */
  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotFoundException();
    }
    return member.getCompany();
  }

  /**
   * member 를 반환함
   */
  private Member getMember() {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    if (email == null) {
      throw new MemberUnauthorizedException();
    }
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }
}
