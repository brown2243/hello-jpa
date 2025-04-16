package hello.jpa.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Team extends BaseEntity {

  private String name;
  private String city;
  private String street;
  private String zipcode;

}
