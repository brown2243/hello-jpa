package hello.jpa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class AddressEntity {

  @Id
  @GeneratedValue
  private Long id;
  private Address address;

  public AddressEntity(Address address) {
    this.address = address;
  }
}
