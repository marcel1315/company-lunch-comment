package com.marceldev.ourcompanylunch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Company extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 20)
  private String name;

  @Setter
  @Column(length = 100)
  private String address;

  @Setter
  private String enterKey;

  @Setter
  private boolean enterKeyEnabled;

  @Setter
  private Point location;

  @Builder
  private Company(String name, String address, String enterKey, boolean enterKeyEnabled,
      Point location) {
    this.name = name;
    this.address = address;
    this.enterKey = enterKey;
    this.enterKeyEnabled = enterKeyEnabled;
    this.location = location;
  }
}
