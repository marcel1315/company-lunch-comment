package com.marceldev.ourcompanylunch.repository.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerImageRepository extends JpaRepository<DinerImage, Long> {

  /**
   * DinerImage has a largest order value within DinerImage that has same Diner.
   */
  Optional<DinerImage> findTopByDinerOrderByOrdersDesc(Diner diner);

  /**
   * An amount of images.
   */
  int countByDinerAndThumbnail(Diner diner, boolean thumbnail);

  /**
   * Remove all DinerImages that has same diner id.
   */
  void deleteByDinerId(Long dinerId);
}
