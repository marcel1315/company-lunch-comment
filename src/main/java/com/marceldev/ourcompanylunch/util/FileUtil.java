package com.marceldev.ourcompanylunch.util;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileUtil {

  public static Optional<String> getExtension(MultipartFile file) {
    String original = file.getOriginalFilename();

    if (original == null) {
      log.debug("No filename {}", file.toString());
      return Optional.empty();
    }

    // Prepare a whole path including directory.
    int lastSlashIndex = original.lastIndexOf("/");
    if (lastSlashIndex != -1) {
      original = original.substring(lastSlashIndex + 1);
    }

    int lastDotIndex = original.lastIndexOf(".");
    if (lastDotIndex == -1) {
      return Optional.empty();
    }
    return Optional.of(original.substring(lastDotIndex + 1));
  }
}
