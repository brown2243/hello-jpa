package hello.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.domain.Address;
import hello.jpa.domain.Child;
import hello.jpa.domain.Member;
import hello.jpa.domain.Parent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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

      Address address = new Address();
      address.setCity("hello");
      address.setStreet("world");
      member.setHomeAddress(address);

      member.getFavoriteFoods().add("치킨");
      member.getFavoriteFoods().add("족발");
      member.getFavoriteFoods().add("피자");

      member.getAddressHistory().add(new Address("old1", "street1", "10000"));
      member.getAddressHistory().add(new Address("old2", "street1", "10000"));

      //
      em.persist(member);

      em.flush();
      em.clear();

      Member findMember = em.find(Member.class, 1L);

      // // 갑타입 변경 - bad case
      // findMember.getHomeAddress().setCity("newCity");
      // 갑타입 변경 - good case
      Address a = findMember.getHomeAddress();
      findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));

      // 이거는 업데이트가 없다
      findMember.getFavoriteFoods().remove("치킨");
      findMember.getFavoriteFoods().add("한식");

      //
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
