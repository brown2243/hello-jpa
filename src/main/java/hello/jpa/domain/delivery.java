package hello.jpa.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hello.jpa.enums.DeliveryStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Delivery extends BaseEntity {

  // @OneToMany(mappedBy = "member")
  // private List<Order> orders;

  private DeliveryStatus status;

  @Embedded
  private Address homeAddress;

  @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
  private Order order;

}
