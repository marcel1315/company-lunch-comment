package com.marceldev.companylunchcomment.component;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FCMPushNotification {

  public void sendPushNotification(String token, String message) {
    Message msg = Message.builder()
        .putData("message", message)
        .setToken(token)
        .build();

    try {
      String response = FirebaseMessaging.getInstance().send(msg);
      log.debug("Successfully sent message: " + response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
