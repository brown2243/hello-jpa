package hello.jpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ORDER_ITEM")
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long orderPrice;
  private Long count;

  @JoinColumn(name = "order_id")
  @ManyToOne(fetch = FetchType.LAZY)
  Order order;

  @JoinColumn(name = "item_id")
  @ManyToOne(fetch = FetchType.LAZY)
  Item item;
}
