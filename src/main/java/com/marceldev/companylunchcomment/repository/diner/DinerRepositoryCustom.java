package com.marceldev.companylunchcomment.repository.diner;

import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DinerRepositoryCustom {

  Page<DinerOutputDto> getList(long companyId, GetDinerListDto dto, Pageable pageable);
}
