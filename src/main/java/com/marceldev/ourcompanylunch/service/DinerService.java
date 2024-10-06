package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.dto.diner.DinerDetailOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListRequest;
import com.marceldev.ourcompanylunch.dto.diner.UpdateDinerRequest;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

  @Transactional
  public CreateDinerResponse createDiner(CreateDinerRequest dto) {
    Company company = getCompany();
    Diner diner = dto.toEntity();
    diner.setCompany(company);
    diner = dinerRepository.save(diner);
    return CreateDinerResponse.of(diner);
  }

  public Page<DinerOutputDto> getDinerList(GetDinerListRequest dto) {
    Company company = getCompany();
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
    return dinerRepository.getList(company.getId(), dto, pageable);
  }

  public DinerDetailOutputDto getDinerDetail(long id) {
    Diner diner = getDiner(id);
    List<String> dinerImageKeys = Optional.ofNullable(diner.getDinerImages())
        .orElse(Collections.emptyList()).stream()
        .filter(dinerImage -> !dinerImage.isThumbnail())
        .map(DinerImage::getS3Key)
        .toList();
    List<String> dinerThumbnailKeys = Optional.ofNullable(diner.getDinerImages())
        .orElse(Collections.emptyList()).stream()
        .filter(DinerImage::isThumbnail)
        .map(DinerImage::getS3Key)
        .toList();
    List<String> imageUrls = getImageUrls(dinerImageKeys);
    List<String> thumbnailUrls = getImageUrls(dinerThumbnailKeys);

    Integer distance = dinerRepository.getDistance(diner.getCompany().getId(), diner.getId());
    return DinerDetailOutputDto.of(diner, thumbnailUrls, imageUrls, distance);
  }

  @Transactional
  public void updateDiner(long id, UpdateDinerRequest dto) {
    Diner diner = getDiner(id);
    diner.setLink(dto.getLink());
    diner.setLocation(dto.getLocation());
  }

  @Transactional
  public void removeDiner(long id) {
    Diner diner = getDiner(id);
    List<String> dinerImageKeys = Optional.ofNullable(diner.getDinerImages())
        .orElse(Collections.emptyList())
        .stream()
        .map(DinerImage::getS3Key)
        .toList();

    dinerImageRepository.deleteByDinerId(id);
    dinerRepository.delete(diner);

    // After removing diner and dinerImage, remove the image stored in S3.
    // Even if removing the S3 image failed, if the diner is removed from DB, it succeeds without exception.
    try {
      dinerImageKeys.forEach(s3Manager::removeFile);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
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
   * Get diner. Check if the member can access the diner.
   */
  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  /**
   * Get company. Check if the member can access the company.
   */
  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotFoundException();
    }
    return member.getCompany();
  }

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
