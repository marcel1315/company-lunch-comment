package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerImageRepository extends JpaRepository<DinerImage, Long> {

  /**
   * 같은 Diner를 가진 DinerImage 중 가장 큰 orders 값을 가진 DinerImage
   */
  Optional<DinerImage> findTopByDinerOrderByOrdersDesc(Diner diner);

  /**
   * image의 갯수
   */
  int countByDiner(Diner diner);

  /**
   * dinerId에 해당하는 모든 image를 제거
   */
  void deleteByDinerId(Long dinerId);
}
