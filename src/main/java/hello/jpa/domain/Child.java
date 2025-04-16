package hello.jpa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Child {
  @Id
  @GeneratedValue
  private Long id;
  private String name;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Parent parent;
}
