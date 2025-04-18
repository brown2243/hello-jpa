package hello.jpa.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Member extends BaseEntity {

  // @OneToMany(mappedBy = "member")
  // private List<Order> orders;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "team_id")
  private Team team;

  private String name;

  @Embedded
  private Address homeAddress;

  @ElementCollection
  @CollectionTable(name = "favorite_food", joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "food_name") // string만
  private Set<String> favoriteFoods = new HashSet<>();

  // @ElementCollection
  // @CollectionTable(name = "address_history", joinColumns = @JoinColumn(name =
  // "member_id"))
  // private List<Address> addressHistory = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "member_id")
  private List<AddressEntity> addressHistory = new ArrayList<>();

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "city", column = @Column(name = "work_city")),
      @AttributeOverride(name = "street", column = @Column(name = "work_street")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "work_zipcode")),
  })
  private Address workAddress;
  @Embedded
  private Period workPeriod;

}
