package hello.jpa.domain;

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
  private String city;
  private String street;
  private String zipcode;

}
