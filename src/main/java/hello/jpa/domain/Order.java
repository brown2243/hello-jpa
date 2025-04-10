package hello.jpa.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import hello.jpa.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDateTime ordereDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @JoinColumn(name = "member_id")
  @ManyToOne
  Member member;

  @OneToMany(mappedBy = "order")
  List<OrderItem> orderItems = new ArrayList<>();

  public void addOrderItems(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }
}
