package com.marceldev.companylunchcomment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Diner extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 20)
  private String name;

  @Column(length = 2048)
  @Setter
  private String link;

  @Column(length = 20)
  @Setter
  private String latitude;

  @Column(length = 20)
  @Setter
  private String longitude;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "JSON DEFAULT '[]'", nullable = false)
  private LinkedHashSet<String> tags;

  public void addTag(String tag) {
    tags.add(tag);
  }

  public void removeTag(String tag) {
    tags.remove(tag);
  }
}
