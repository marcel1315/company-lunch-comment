package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DinerRepositoryCustom {

  Page<DinerOutputDto> getList(long companyId, GetDinerListRequest dto, Pageable pageable);

  Integer getDistance(long companyId, long dinerId);
}
