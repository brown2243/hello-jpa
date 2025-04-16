package hello.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@Component
public class JpaRunner implements CommandLineRunner {

  @Autowired
  private EntityManagerFactory emf;

  @PersistenceContext
  private EntityManager em;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
      tx.begin();
      //
      Member member = new Member();
      member.setName("HELLO");
      em.persist(member);
      //
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      e.printStackTrace();
    } finally {
      em.close();
    }

    //
    // Book book = new Book();
    // book.setName("JPA");
    // book.setAuthor("김영한");
    // em.persist(book);

    // Movie movie = new Movie();
    // movie.setDirector("AAAA");
    // movie.setActor("BBBB");
    // movie.setName("바람과 함께 사라지다");
    // movie.setPrice(10000L);

    // em.persist(movie);

    // em.flush();
    // em.clear();

    // Movie findMovie = em.find(Movie.class, movie.getId());

    // for (Member m : member.getTeam().getMembers()) {
    // System.out.println("m" + m.getName());
    // }

    // 조회
    // Member member = em.find(Member.class, 1L);
    // Team team2 = em.find(Team.class, 2L);

    // System.out.println(member.getTeam().getName());
    // // 회원1에 새로운 팀B 설정
    // member.setTeam(team2);
    // System.out.println(member.getTeam().getName());

    // List<Member> members = em.createQuery("select m from Member m", Member.class)
    // .setFirstResult(1)

    // .setMaxResults(8)
    // .getResultList();

    // for (Member member : members) {
    // System.out.println("member.name = " + member.getUsername());
    // }

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
