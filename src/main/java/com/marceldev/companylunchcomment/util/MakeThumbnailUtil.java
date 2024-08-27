package com.marceldev.companylunchcomment.util;

import com.marceldev.companylunchcomment.exception.ImageReadFailException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class MakeThumbnailUtil {

  private static final int FIXED_WIDTH = 300;

  public static ByteArrayOutputStream resizeFile(InputStream inputStream, String extension) {
    try {
      BufferedImage image = ImageIO.read(inputStream);

      if (image == null) {
        throw new ImageReadFailException();
      }

      // calculate thumbnail size
      double ratio = (double) image.getHeight() / (double) image.getWidth();
      int newWidth = Math.min(FIXED_WIDTH, image.getWidth());
      int newHeight = (int) (newWidth * ratio);

      // get resized image
      BufferedImage resizedImage = resizeImage(image, newWidth, newHeight);

      // make file
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, extension, outputStream);
      return outputStream;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {
    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight,
        Image.SCALE_DEFAULT);
    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight,
        BufferedImage.TYPE_INT_RGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return outputImage;
  }
}
