package com.marceldev.companylunchcomment.entity;

import com.marceldev.companylunchcomment.util.CalculateDistance;
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

  @Column(length = 20)
  @Setter
  private String latitude;

  @Column(length = 20)
  @Setter
  private String longitude;

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
  private List<Comments> comments = new ArrayList<>();

  public void addTag(String tag) {
    tags.add(tag);
  }

  public void removeTag(String tag) {
    tags.remove(tag);
  }

  public void calculateDistance(Company company) {
    if (company.getLatitude() != null
        && company.getLongitude() != null
        && latitude != null
        && longitude != null) {
      double lat1 = Double.parseDouble(company.getLatitude());
      double lon1 = Double.parseDouble(company.getLongitude());
      double lat2 = Double.parseDouble(this.latitude);
      double lon2 = Double.parseDouble(this.longitude);
      distance = CalculateDistance.calculate(lat1, lon1, lat2, lon2);
    }
  }
}
