package com.marceldev.companylunchcomment.repository.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerSubscription;
import com.marceldev.companylunchcomment.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerSubscriptionRepository extends JpaRepository<DinerSubscription, Long> {

  Optional<DinerSubscription> findByDinerAndMember(Diner diner, Member member);

  boolean existsByDinerAndMember(Diner diner, Member member);
}
