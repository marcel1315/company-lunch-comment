package com.marceldev.ourcompanylunch.repository.pushnotificatontoken;

import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.PushNotificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationTokenRepository extends
    JpaRepository<PushNotificationToken, Long> {

  Optional<PushNotificationToken> findByMember(Member member);
}
