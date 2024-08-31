package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerRepository extends JpaRepository<Diner, Long>, DinerRepositoryCustom {

}
