package hello.jpa.domain;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
// public class Item extends BaseEntity {
public abstract class Item extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  private String name;
  private Integer price;
  private Integer stockQuantity;

}
