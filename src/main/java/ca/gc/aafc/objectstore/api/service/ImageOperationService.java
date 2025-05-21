package ca.gc.aafc.objectstore.api.service;

import com.twelvemonkeys.image.ResampleOp;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import org.springframework.stereotype.Service;

/**
 * Service class responsible to apply operations/manipulations on images.
 */
@Service
public class ImageOperationService {

  /**
   * Resize the provided {@link BufferedImage} with a factor.
   * @param original
   * @param factor
   * @return
   */
  public BufferedImage resize(BufferedImage original, float factor) {
    BufferedImageOp
      resampler = new ResampleOp((int) (original.getWidth() * factor),
      (int) (original.getHeight() * factor), ResampleOp.FILTER_LANCZOS);
    return resampler.filter(original, null);
  }

}
