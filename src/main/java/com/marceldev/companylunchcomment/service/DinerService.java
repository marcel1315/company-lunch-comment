package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.CreateDinerDto;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DinerService {

  private final DinerRepository dinerRepository;

  public void createDiner(CreateDinerDto createDinerDto) {
    dinerRepository.save(createDinerDto.toEntity());
  }
}
