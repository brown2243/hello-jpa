package hello.jpa;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class JpaRunner implements CommandLineRunner {

  @PersistenceContext
  private EntityManager em;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    // 실행 시점에 동작할 코드
    System.out.println("JpaRunner START!!!!!");

    List<Member> members = em.createQuery("select m from Member m", Member.class)
        .setFirstResult(1)
        .setMaxResults(8)
        .getResultList();

    for (Member member : members) {
      System.out.println("member.name = " + member.getName());
    }

    // Member member = em.find(Member.class, 1L);
    // member.setName("hello JPA");

    // // spring이 없으면 아래의 코드가 필요하다
    // EntityTransaction tx = em.getTransaction();
    // tx.begin();
    // try {
    // Member member = new Member();
    // member.setId(0L);
    // member.setName("hello");

    // em.persist(member);
    // tx.commit();

    // } catch (Exception e) {
    // tx.rollback();
    // } finally {
    // em.close();
    // }

    System.out.println("JpaRunner END!!!!!");
  }
}
