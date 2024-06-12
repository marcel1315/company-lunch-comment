package com.marceldev.companylunchcomment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString(exclude = "dinerImages")
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

  @Setter
  private Point location;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "JSON DEFAULT '[]'", nullable = false)
  private LinkedHashSet<String> tags;

  @OneToMany(mappedBy = "diner", fetch = FetchType.LAZY)
  private List<DinerImage> dinerImages;

  private Integer distance;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @OneToMany(mappedBy = "diner", fetch = FetchType.LAZY)
  private final List<Comments> comments = new ArrayList<>();

  public void addTag(String tag) {
    tags.add(tag);
  }

  public void removeTag(String tag) {
    tags.remove(tag);
  }
}
