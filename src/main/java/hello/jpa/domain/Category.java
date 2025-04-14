package hello.jpa.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Category extends BaseEntity {

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
  private List<Item> items;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany
  private List<Category> child;

  private String name;

}
