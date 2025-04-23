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

      Team teamA = new Team();
      teamA.setName("teamA");
      Team teamB = new Team();
      teamB.setName("teamB");

      Member member1 = new Member();
      member1.setUsername("mem1");
      member1.setAge(20);
      member1.setTeam(teamA);
      Member member2 = new Member();
      member2.setUsername("mem2");
      member2.setAge(20);
      member2.setTeam(teamA);
      Member member3 = new Member();
      member3.setUsername("mem3");
      member3.setAge(30);
      member3.setTeam(teamB);

      em.persist(teamA);
      em.persist(teamB);
      em.persist(member1);
      em.persist(member2);
      em.persist(member3);

      em.flush();
      em.clear();

      // select m from Member m
      // select m from Member m join fetch m.team
      String query = """
          select t from Team t join fetch t.members
          """;

      List<Team> result = em.createQuery(
          query,
          Team.class).getResultList();

      for (Team team : result) {
        System.out.println("team = " + team.getName() + ", " + team.getMembers().size());
        // 회원1, 팀 A SQL
        // 회원2, 팀 A 1차캐시
        // 회원3, 팀 B SQL

        // 회원 100명 -> N + 1(1은 처음 날린 쿼리, 그 쿼리의 결과로 N번 만큼 쿼리를 날리는 것을 N + 1 문제라 함)
        // 페치 조인을 하면 프록시가 아닌 실제 엔티티를 받아옴
      }

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
