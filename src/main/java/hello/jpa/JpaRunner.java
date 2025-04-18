package hello.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hello.jpa.domain.Address;
import hello.jpa.domain.AddressEntity;
import hello.jpa.domain.Member;
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

      member.getAddressHistory().add(new AddressEntity(new Address("old1", "street1", "10000")));
      member.getAddressHistory().add(new AddressEntity(new Address("old2", "street1", "10000")));

      //
      em.persist(member);

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
