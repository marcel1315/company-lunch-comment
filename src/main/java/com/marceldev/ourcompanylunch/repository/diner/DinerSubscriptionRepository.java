package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DinerSubscriptionRepository extends JpaRepository<DinerSubscription, Long> {

  Optional<DinerSubscription> findByDinerAndMember(Diner diner, Member member);

  @Query("select ds from DinerSubscription ds"
      + " join fetch ds.member"
      + " where ds.diner.id = :dinerId")
  Set<DinerSubscription> findDinerSubscriptionByDinerId(long dinerId);

  boolean existsByDinerAndMember(Diner diner, Member member);
}
