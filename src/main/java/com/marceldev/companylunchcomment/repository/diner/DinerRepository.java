package com.marceldev.companylunchcomment.repository.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerRepository extends JpaRepository<Diner, Long>, DinerRepositoryCustom {

}
