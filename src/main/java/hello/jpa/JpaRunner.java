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

      Member member = new Member();
      member.setUsername("hello");
      member.setAge(25);
      em.persist(member);

      em.flush();
      em.clear();

      List<Member> resultList = em.createQuery("select m from Member as m",
          Member.class).getResultList();

      // bad case
      // 묵시적 조인
      // join이 나가는 지 예측하기 어렵다
      em.createQuery("select m.team from Member m", Team.class);

      // good case
      // 명시적 조인
      em.createQuery("select t from Member m join m.team t", Team.class);

      // order 안의 값 타입
      em.createQuery("select o.address from Order o ", Order.class);

      Member member2 = resultList.get(0);
      member2.setAge(20);

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
