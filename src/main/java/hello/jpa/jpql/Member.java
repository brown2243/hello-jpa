package hello.jpa.jpql;

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
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;
  private int age;

  @ManyToOne()
  @JoinColumn(name = "team_id")
  private Team team;

  @Override
  public String toString() {
    return "Member [id=" + id + ", username=" + username + ", age=" + age + "]";
  }
}
