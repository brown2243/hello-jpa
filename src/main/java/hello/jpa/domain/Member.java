package hello.jpa.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Member extends BaseEntity {

  // @OneToMany(mappedBy = "member")
  // private List<Order> orders;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "team_id")
  private Team team;

  private String name;

  @Embedded
  private Address homeAddress;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "city", column = @Column(name = "work_city")),
      @AttributeOverride(name = "street", column = @Column(name = "work_street")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "work_zipcode")),
  })
  private Address workAddress;
  @Embedded
  private Period workPeriod;

}
