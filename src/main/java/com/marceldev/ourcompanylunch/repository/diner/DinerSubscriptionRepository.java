package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DinerSubscriptionRepository extends JpaRepository<DinerSubscription, Long> {

  Optional<DinerSubscription> findByDinerAndMember(Diner diner, Member member);

  // 각 member의 token을 모두 불러와 사용할 것이기 때문에 fetch join을 함
  @Query("select ds from DinerSubscription ds"
      + " join fetch ds.member"
      + " left join fetch ds.member.token"
      + " where ds.diner.id = :dinerId")
  List<DinerSubscription> findDinerSubscriptionAndTokenByDinerId(long dinerId);

  boolean existsByDinerAndMember(Diner diner, Member member);
}
