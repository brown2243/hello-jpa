# 1. 강좌 소개

- 시작일 250407

## 강의 목표

- JPA 내부 동작 방식 이해
- JPA 가 생성하는 SQL에 대해 이해
- JPA 가 언제 SQL을 실행하는지에 이해

# 2. JPA 소개

## SQL 중심적인 개발의 문제점

- 어플리케이션은 객체지향
- 데이터베이스는 관계형
- 객체를 관계형 DB에 관리하는 시대

- 객체에 필드 추가 변경시, 모든 쿼리를 변경해야 함
- 관계형 DB를 사용하는 이상 SQL에 의존적인 개발을 할 수 밖에 없다

### 객체와 RDBS의 차이

- 상속의 개념이 없다.
  - 모델링 기법인 **슈퍼타입 및 서브타입 도출 -** https://kkjsw17.tistory.com/12
- 연관관계
  - 객체는 참조
  - 테이블은 외래키
- 객체는 참조로 탐색가능(객체 그래프)
  - DAO 쿼리를 확인 해야 한다.
- 객체답게 모델링을 하면 매핑작업이 늘어난다.

### 객체를 컬렉션에 저장하듯 DB에 저장하는 ORM

## JPA 소개

- ORM: object relational mapping 객체 관계 매핑
- 객체는 객체대로설계
- DB는 DB대로 설계
- ORM 프레임워크가 중간에서 매핑

- JPA는 APP과 JDBC API 사이에서 동작
- EJB - entity bean(자바 표준)
- 개빈킹의 hibernate 오픈
- JPA(자바 표준) extends hibernate

## JPA는 표준 명세

- 표준명세 = 인터페이스의 모음
- JPA 2.1 표준명세를 구현한 3가지 구현체
- 하이버네이트(90%이상), 이클립스링크, 데이타누클리스

## JPA를 왜 사용해야하는가

- SQL 중심개발에서 객체중심개발
- 생산성, 유지보수, 성능
- 패러다임 불일치 해결
- 표준

값 변경하고 save하지말고 set 메서드만 실행해도 그게 db에 반영이 된다고??

이것이 **변경 감지(Dirty Checking) ㄷㄷ**

## ✅ 트랜잭션의 생명주기 (Spring 기준)

### 🔁 기본 흐름:

1. `@Transactional` 메서드 호출 (프록시가 감지)
2. 트랜잭션 시작 (`begin transaction`)
3. 메서드 실행
4. 메서드가 정상 종료 → `commit`
5. 예외 발생 시 → `rollback`

## 트랜잭션이 끝나는 시점은?

> ✅ @Transactional 메서드의 실행이 끝나고, Spring이 commit 또는 rollback을 수행할 때입니다.

## 패러다임 불일치 해결

1. JPA와 상속
2. 연관관계
3. 객체 그래프 탐색
4. 비교하기

## JPA 성능 최적화 기능

- 1차 캐시와 동일성 보장 - 같은 트랜잭션 안에서만
- 트랜직션 지원하는 지연 쓰기 - 배치 SQL 처리
- 지연 로딩
  - 연관된 엔티티를 참조할때 조회
- 즉시 로딩
  - 연관된 엔티티 전부 조회

와 매핑된 엔티티도 한번에 조회가 되는구나!!!

조인쿼리 날린다고 queryDsl 써봤는데…

### ORM은 객체와 RDB 두 기둥위에 있는 기술

# 3. JPA 시작하기

## project 생성

- 사용할 스프링 부트 버전의 doc을 보고 적절한 버전 사용

## 어플리케이션 개발

- 방언: SQL표준이 아닌 특정 DB의 고유기능

- data를 변경하는 모든작업은 트랜잭션안에서 실행해야한다.

### spring 프로젝트를 세팅했는데 member 저장이 안됌

- Spring에서 주입받는 EntityManager는 **스프링이 관리하는 Proxy(EntityManager)**
- 그래서 직접 트랜잭션을 수동으로 제어하면 안된다 함
- `@Transactional` 사용하니 됌

#### ✅ 중요 포인트 요약

| 잘못된 방식 (순수 JPA 스타일)                 | 올바른 Spring 방식                 |
| --------------------------------------------- | ---------------------------------- |
| `EntityTransaction tx = em.getTransaction()`  | `@Transactional`으로 트랜잭션 관리 |
| `tx.begin()`, `tx.commit()` 수동 호출         | 생략하고 `@Transactional`에 맡김   |
| `Persistence.createEntityManagerFactory(...)` | `@PersistenceContext`로 주입받음   |

---

#### 💡 왜 Spring에서 직접 트랜잭션을 못 여는가?

- `SharedEntityManager`는 **ThreadLocal 기반**의 Proxy입니다.
- 이 EntityManager는 스프링이 **트랜잭션 범위 내에서 자동으로 열고 닫아줍니다**.
- 사용자가 직접 `tx.begin()` 등을 호출하면 **컨텍스트 밖에서 트랜잭션을 제어하려는 시도**가 되어 예외가 발생합니다.

---

#### 🛠 추가 개선 팁

- `@Transactional`은 클래스 레벨 또는 메서드 레벨에서 붙일 수 있어요.
- 단, **`CommandLineRunner`에서 사용할 땐 반드시 `run()` 메서드에 붙여야 정상 동작**합니다.

### 📦 정리

```java
@Component
public class JpaRunner implements CommandLineRunner {

  @PersistenceContext
  private EntityManager em;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    System.out.println("JpaRunner START!!!!!");
    Member member = new Member();

    member.setId(1L);
    member.setName("hello");

    em.persist(member);

    System.out.println("JpaRunner END!!!!!");
  }
}
```

### 수정

이건 진짜 대박이다...

```java
Member member = em.find(Member.class, 1L);
member.setName("hello JPA");
```

### 주의

- 엔티티 매니저 팩토리는 하나만 생성해서 애플리케이션 전체에서 공유
- 엔티티 매니저는 쓰레드간에 공유X (사용하고 버려야 한다).
- JPA의 모든 데이터 변경은 트랜잭션 안에서 실행
- DB는 다 트랜잭션 개념을 가지고 있다

### JPQL 소개

- 가장 단순한 조회 방법
- EntityManager.find()
- 객체 그래프 탐색(a.getB().getC())
- 나이가 18살 이상인 회원을 모두 검색하고 싶다면?

- 내가 원하면 데이터를 최적화해서 가져와야하고, 통계성 쿼리도 날려야함
- 기승전 쿼리

```java
// 가져오는 대상이 테이블이 아니고 객체
List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
```

- paging을 지원하는데, DB에 맞게 SQL 생성

### JPQL이 필요한 이유

- JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색 쿼리
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 **검색 조건이 포함된 SQL이 필요**

### JPQL

- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리
- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 객체 지향 SQL
