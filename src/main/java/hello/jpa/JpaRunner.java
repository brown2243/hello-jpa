package hello.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.jpql.Member;
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
      em.persist(member);

      em.flush();
      em.clear();

      // TypedQuery<Member> query1 = em.createQuery("select m from Member as m",
      // Member.class);
      // TypedQuery<String> query2 = em.createQuery("select m.username from Member as
      // m", String.class);
      // Query query3 = em.createQuery("select m.username, m.age from Member as m");
      TypedQuery<Member> query4 = em.createQuery("select m from Member as m where m.username = :username",
          Member.class);
      query4.setParameter("username", "hello");
      System.out.println(query4.getSingleResult().getUsername());

      // query4.setParameter(0, "hello");

      // List<Member> resultList = query1.getResultList();
      // for (Member m : resultList) {
      // System.out.println(m);
      // }

      // Member singleResult = query1.getSingleResult();
      // System.out.println(singleResult);

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
