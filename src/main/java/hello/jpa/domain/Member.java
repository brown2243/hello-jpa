package hello.jpa.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Member extends BaseEntity {

  @OneToMany(mappedBy = "member")
  private List<Order> orders;

  private String name;
  private String city;
  private String street;
  private String zipcode;

}
