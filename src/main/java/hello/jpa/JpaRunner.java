package hello.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.jpql.Member;
import hello.jpa.jpql.Order;
import hello.jpa.jpql.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

@Component
public class JpaRunner implements CommandLineRunner {

  @Autowired
  private EntityManagerFactory emf;

  // @PersistenceContext
  // private EntityManager em;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
      tx.begin();

      Team team = new Team();
      team.setName("teamA");
      em.persist(team);
      Member member = new Member();
      member.setUsername("hello");
      member.setAge(20);
      member.setTeam(team);

      // for (int i = 0; i < 100; i++) {
      // Member member = new Member();
      // member.setUsername("hello" + i);
      // member.setAge(i);
      // member.setTeam(team);

      // em.persist(member);
      // }

      em.flush();
      em.clear();

      List<Member> resultList = em.createQuery(
          "select m from Member m left join m.team t ",
          Member.class)
          .getResultList();

      // List<Member> resultList = em.createQuery(
      // "select m from Member m order by m.age desc",
      // Member.class)
      // .setFirstResult(0)
      // .setMaxResults(10)
      // .getResultList();

      // System.out.println(resultList.size());
      // resultList.forEach(member -> System.out.println(member.toString()));

      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      e.printStackTrace();
    } finally {
      em.close();
    }

    System.out.println("JpaRunner END!!!!!");
  }
}
