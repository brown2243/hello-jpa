package hello.jpa.domain;

import java.time.LocalDateTime;

import hello.jpa.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ORDERS")
public class Order {
  @Id
  @GeneratedValue
  @Column(name = "order_id")
  private Long id;

  private LocalDateTime orderedDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @ManyToOne
  Member member;

  // @OneToMany
  // OrderItem[] OrderItems;
}
