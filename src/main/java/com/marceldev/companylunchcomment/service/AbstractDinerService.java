package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.DinerDetailOutputDto;
import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractDinerService {

  protected final MemberRepository memberRepository;

  protected final DinerRepository dinerRepository;

  abstract void createDinerAfterCheck(CreateDinerDto createDinerDto, Company company);

  abstract Page<DinerOutputDto> getDinerListAfterCheck(GetDinerListDto dto, Company company,
      Pageable pageable);

  abstract DinerDetailOutputDto getDinerDetailAfterCheck(long id, Diner diner);

  abstract void updateDinerAfterCheck(long id, UpdateDinerDto dto, Diner diner);

  abstract void removeDinerAfterCheck(long id, Diner diner);

  abstract void addDinerTagAfterCheck(long id, AddDinerTagsDto dto, Diner diner);

  abstract void removeDinerTagAfterCheck(long id, RemoveDinerTagsDto dto, Diner diner);

  @Transactional
  public void createDiner(CreateDinerDto createDinerDto) {
    Company company = checkMemberHasCompany();
    createDinerAfterCheck(createDinerDto, company);
  }

  public Page<DinerOutputDto> getDinerList(GetDinerListDto dto, Pageable pageable) {
    Company company = checkMemberCanAccessCompany(dto.getCompanyId());
    return getDinerListAfterCheck(dto, company, pageable);
  }

  public DinerDetailOutputDto getDinerDetail(long id) {
    Diner diner = checkMemberCanAccessDiner(id);
    return getDinerDetailAfterCheck(id, diner);
  }

  @Transactional
  public void updateDiner(long id, UpdateDinerDto dto) {
    Diner diner = checkMemberCanAccessDiner(id);
    updateDinerAfterCheck(id, dto, diner);
  }

  @Transactional
  public void removeDiner(long id) {
    Diner diner = checkMemberCanAccessDiner(id);
    removeDinerAfterCheck(id, diner);
  }

  @Transactional
  public void addDinerTag(long id, AddDinerTagsDto dto) {
    Diner diner = checkMemberCanAccessDiner(id);
    addDinerTagAfterCheck(id, dto, diner);
  }

  @Transactional
  public void removeDinerTag(long id, RemoveDinerTagsDto dto) {
    Diner diner = checkMemberCanAccessDiner(id);
    removeDinerTagAfterCheck(id, dto, diner);
  }

  // 회원이 식당에 접근 가능한지 확인
  private Diner checkMemberCanAccessDiner(long dinerId) {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    long companyId = memberRepository.findByEmail(user.getUsername())
        .map(Member::getCompany)
        .map(Company::getId)
        .orElseThrow(CompanyNotExistException::new);

    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().getId().equals(companyId))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  // 회원이 회사에 속했는지 확인
  private Company checkMemberCanAccessCompany(long companyId) {
    Company company = checkMemberHasCompany();
    if (company.getId() == companyId) {
      return company;
    } else {
      throw new CompanyNotExistException();
    }
  }

  // 회원이 회사를 선택했는지 확인
  private Company checkMemberHasCompany() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return memberRepository.findByEmail(user.getUsername())
        .map(Member::getCompany)
        .orElseThrow(CompanyNotExistException::new);
  }
}