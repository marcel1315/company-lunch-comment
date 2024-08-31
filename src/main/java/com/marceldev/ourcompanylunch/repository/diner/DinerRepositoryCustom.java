package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DinerRepositoryCustom {

  Page<DinerOutputDto> getList(long companyId, GetDinerListDto dto, Pageable pageable);

  Integer getDistance(long companyId, long dinerId);
}
