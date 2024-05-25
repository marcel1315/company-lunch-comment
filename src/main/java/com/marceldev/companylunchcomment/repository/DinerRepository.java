package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Diner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerRepository extends JpaRepository<Diner, Long> {

  Page<Diner> findAll(Pageable pageable);
}
