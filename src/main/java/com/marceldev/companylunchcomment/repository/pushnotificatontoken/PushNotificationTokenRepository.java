package com.marceldev.companylunchcomment.repository.pushnotificatontoken;

import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.PushNotificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationTokenRepository extends
    JpaRepository<PushNotificationToken, Long> {

  Optional<PushNotificationToken> findByMember(Member member);
}
