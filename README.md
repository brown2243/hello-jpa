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
- 트랜직션 지원하는 지연 쓰기
- SQL 모아뒀다가, 배치 SQL 처리 가능
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

```java
// spring이 제어하는 em 주입 받음 -> tx 수동제어 불가
@PersistenceContext
private EntityManager em;

// EntityManagerFactory를 주입받아 em 생성하면 가능
@Autowired
private EntityManagerFactory emf;

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

```

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

# 4. 영속성 관리 - 내부 동작 방식

## 영속성 컨텍스트 1

### JPA에서 가장 중요한 2가지

- Object와 RDB 매핑 ORM
  - DB와 객체가 잘 연동 될 수 있게 설계
- 영속성 컨텍스트(persistance context)

  - JPA가 내부적으로 동작하는 방식

- 엔티티매니저팩토리와 엔티티매니저
- 엔티티매니저팩토리는 요청을 받으면 엔티티 매니저를 생성
- 엔티티매니저는 DB 커넥션을 사용함

- 영속성 컨텍스트: 엔티티를 영구저장하는 환경

  - persist 메서드는 영속성 컨텍스트에 저장한다는 의미

- 엔티티 라이프사이클

  - 비영속: 영속성 컨텍스트와 관계없음
  - 영속: 영속성 컨텍스트에 저장되어 관리되는 상태 // persist
  - 준영속: 영속성 컨텍스트에서 분리된 상태 // detech
  - 삭제: 영속성 컨텍스트에서 삭제 // remove

- 객체가 영속상태가 된다고해서 바로 DB에 쿼리에 날라가는게 아니라, 트랜잭션을 커밋하는 시점

### 영속성 컨텍스트의 이점

- 1차 캐시
- 동일성 보장
- 트랜잭션을 보장하는 쓰기지연
- 변경 감지(dirty checking)
- 지연 로딩(lazy loading)

## 영속성 컨텍스트 2

### 엔티티 조회, 1차 캐시

- 조회할때, 컨텍스트부터 확인해서 있다면 DB까지 가지않는다
  - react-query랑 굉장히 유사
- 1차 캐시는 하나의 트랜잭션에서만 유효하다.

  - 다른 요청으로 인한 트랜잭션과 같은 캐시를 공유하지 않는다는 의미
  - JPA에서 어플리케이션 전체에서 공유하는 캐시는 2차 캐시
  - 그렇기에 유의미한 성능이점은 없다.

- 데이터를 매번 DB에서 읽지 않고, 애플리케이션이 메모리에 보관해두고 사용하는 방식(1차 캐시)을 통해,
- 트랜잭션 동안 같은 데이터를 반복해서 읽어도 값이 변하지 않도록 보장하는 것. 이걸 DB 설정 없이 애플리케이션 내부에서 구현한 것이다."
  - 트랜잭션 격리 수준:REPEATABLE READ
    - 트랜잭션 격리 = DB에서 동시에 여러 작업이 일어날 때, 데이터의 일관성을 유지하기 위해 격리 수준을 설정
    - REPEATABLE READ = "트랜잭션 내에서 동일한 데이터를 여러 번 읽어도 값이 변하지 않도록 보장"하는 등급

### 엔티티 등록 - 쓰기 지연

- JPA에서는 데이터 변경이 반드시 트랜잭션 안에서 이루어져야 하며, 엔티티 매니저는 변경 내용을 즉시 DB에 반영하지 않는다.
- 트랜잭션 커밋 직전에 flush()를 통해 SQL을 한꺼번에 전송한다.
- Hibernate는 이 과정에서 JDBC Batch 기능을 활용해 SQL들을 버퍼에 모았다가, 여러 쿼리를 한 번에 처리할 수 있어 성능을 최적화한다.

- JPA는 객체를 만들 때 리플렉션(Reflection)을 사용하기 때문에, 반드시 기본 생성자(파라미터 없는 생성자)가 필요하다.

  - 내부적으로 리플렉션을 통해 객체를 만들고 필드에 값을 채움
  - 이때 리플렉션은 기본 생성자가 있어야만 객체를 생성

- JPA의 목적은 컬렉션을 사용하는 것처럼 객체를 다루는 것

  - 컬렉션에서 데이터를 변경했다면, 변경이 되어야 함

- 커밋을 하면 내부적으로 flush가 호출 됨
- 1차 캐시에는 엔티티와, 스냅샷(최초로 1차캐시에 들어온 시점의 데이터)
- 값을 전부 비교

- 리플렉션: 런타임에 클래스, 메서드, 필드 등 객체의 구조를 조사하고 조작

  - 기능
    - 클래스 이름, 필드 목록, 메서드 목록을 런타임에 조회
    - 접근 제한(private 등)을 무시하고 필드나 메서드에 접근
    - 객체의 클래스 타입을 모르더라도 동적으로 인스턴스를 생성
  - 기본 흐름:
    1. 클래스 로딩
    2. Class 객체 생성
    3. 리플렉션 API로 필드/메서드/생성자 조사
    4. 필요하면 setAccessible(true)로 private 접근 무시
    5. 값을 읽거나 설정하거나, 인스턴스를 생성
  - 단점
    - 성능 저하 - 리플렉션은 일반 코드보다 느림 (최적화 어려움)
    - 타입 안정성 낮음 - 컴파일 타임에 타입 체크가 안 됨 (런타임 오류 가능)
    - 유지보수 어려움 - 코드 흐름 추적이 어려움, 리팩토링 어려움
  - 일반 로직에서는 잘 안 쓰고, 프레임워크나 라이브러리 수준에서 주로 사용

## 플러시

- 영속성 컨텍스트의 변경내용을 DB에 반영하는 것
  - 영속성 컨텍스트를 비우는 것이 아님
- 트랜잭션이라는 작업단위가 중요하다 - 커밋직전에만 동기화하면 됨

### 플러시 발생

- 변경 감지
- 엔티티 수정
  - (쓰기 지연 SQL 저장소에 등록)
- SQL 저장소의 쿼리를 DB에 전송

### 영속성 컨텍스트를 플러시 하는 법

- em.flush() - 직접 호출
- tx.commit() - 자동 호출
- JPQL query - 자동 호출

### flush mode

- auto: commit or query 실행 시 flush(기본)
- commit: commit만 flush

## 준영속 상태

- 영속상태의 엔티티를 영속성 컨텍스트에서 분리

### 준영속상태로 만드는 법

- em.detach(): 특정 엔티티만
- em.clear(): 컨텍스트 초기화
- em.close(): 컨텍스트 종료

# 5. 엔티티 매핑

## 객체와 테이블 매핑

- 객체와 테이블 매핑: @`Entity`, `@Table`
- 필드와 컬럼 매핑: `@Column`
- 기본 키 매핑: `@Id`
- 연관관계 매핑: `@ManyToOne`,`@JoinColumn`

### @Entity

- @Entity가 붙은 클래스는 JPA가 관리, 엔티티라 한다.
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
- 주의
  - 기본 생성자 필수(파라미터가 없는 public 또는 protected 생성자)
  - final 클래스, enum, interface, inner 클래스 사용X
  - 저장할 필드에 final 사용 X

### @Entity 속성 정리

- 속성: name
- JPA에서 사용할 엔티티 이름을 지정한다.
- 기본값: 클래스 이름을 그대로 사용(예: Member)
- 같은 클래스 이름이 없으면 가급적 기본값을 사용한다.

- @Table은 엔티티와 매핑할 테이블 지정

## 데이터베이스 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 이렇게 생성된 DDL은 개발 장비에서만 사용
- 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

### 데이터베이스 스키마 자동 생성 - 속성

`hibernate.ddl-auto`

- create: 기존테이블 삭제 후 다시 생성 (DROP + CREATE)
- create-drop: create와 같으나 종료시점에 테이블 DROP
- update: 변경분만 반영(운영DB에는 사용하면 안됨)
- validate: 엔티티와 테이블이 정상 매핑되었는지만 확인
- none: 사용하지 않음(관례상)

### 데이터베이스 스키마 자동 생성 - 주의

- **운영 장비에는 절대 create, create-drop, update 사용하면 안된다.**
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- **스테이징과 운영 서버는 validate 또는 none**

- **로컬아니면 그냥 쓰지마라!!!**
- **스크립트짜서 직접 적용해라**
- alter query는 해당 테이블에 락이 걸리는데, 그동안 서비스가 중단될 수 있다.

### DDL 생성 기능

- 제약조건 추가: 회원 이름은 필수, 10자 초과X
- @Column(nullable = false, length = 10)
- 유니크 제약조건 추가
- @Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE",
  columnNames = {"NAME", "AGE"} )})
- DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고
  JPA의 실행 로직에는 영향을 주지 않는다.

### ✅ 운영 환경에서 스키마 변경하는 방법

#### 1. **DDL 스크립트를 수동으로 작성하거나 자동 생성**

- 개발 환경에서 `create` 또는 `update` 옵션을 설정하고 테스트 DB에서 Hibernate가 생성한 DDL을 확인
- `spring.jpa.show-sql=true`, `spring.jpa.properties.hibernate.format_sql=true` 옵션으로 쿼리 로그를 예쁘게 출력
- 또는 `schema-generation.scripts.create-target` 옵션을 써서 Hibernate로부터 DDL 스크립트를 생성 가능

```properties
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.javax.persistence.schema-generation.scripts.action=create
spring.jpa.properties.javax.persistence.schema-generation.scripts.create-target=ddl/create.sql
```

---

#### 2. **DBA 또는 개발자가 직접 스크립트 검토**

- 테이블 락, 인덱스 생성 비용 등 고려
- 위험한 `alter table`은 업무 시간 외나 배포창구에서 실행
- 스크립트는 형상관리(Git 등)에 포함

---

#### 3. **버전 관리 도구 사용 (🛠️ 추천!)**

**[Flyway](https://flywaydb.org/) 또는 Liquibase**를 이용하면 스키마 변경을 코드처럼 관리 가능

#### 예: Flyway

- `resources/db/migration` 폴더에 `V1__create_user_table.sql`처럼 작성
- 애플리케이션 시작 시 자동 적용되거나, CLI/CI에서 수동 적용

### 👉 실무에서는 다음을 추천

- 개발 단계에서 DDL 미리 확인
- 변경 사항을 DDL로 작성
- Flyway/Liquibase 같은 도구로 관리
- 운영 배포 전에 DBA와 리뷰 및 테스트

## 필드와 컬럼 매핑

### 매핑 어노테이션 정리

- `@Column`: 컬럼 매핑
- `@Temporal`: 날짜 타입 매핑
- `@Enumerated`: enum 타입 매핑
- `@Lob`: BLOB, CLOB 매핑
- `@Transient`: 특정 필드를 컬럼에 매핑하지 않음(매핑 무시) - 메모리에서만 사용

### @Column

- name: 필드와 매핑할 테이블의 컬럼 이름 객체의 필드 이름
- insertable,updatable: 등록, 변경 가능 여부
- nullable(DDL) null 값의 허용 여부를 설정한다.
  - false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.
- unique(DDL) @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다.
- columnDefinition(DDL) 데이터베이스 컬럼 정보를 직접 줄 수 있다.
  - ex) varchar(100) default ‘EMPTY'
- length(DDL) 문자 길이 제약조건, String 타입에만 사용한다.
  - 255
- precision,scale(DDL): BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다).
  - precision은 소수점을 포함한 전체 자 릿수를, scale은 소수의 자릿수
  - 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다루어야 할 때만 사용한다.
  - precision=19,scale=2

### @Enumerated

- 자바 enum 타입을 매핑할 때 사용
- 주의! ORDINAL 사용X
- EnumType.ORDINAL: enum 순서를 데이터베이스에 저장(기본값)
- EnumType.STRING: enum 이름을 데이터베이스에 저장

### @Temporal

- 날짜 타입(java.util.Date, java.util.Calendar)을 매핑할 때 사용
  - 과거
- LocalDate(날짜만), LocalDateTime(날짜 + 시간), Instant(타임스탬프)을 사용할 때는 생략 가능(최신 하이버네이트 지원)
  - 권장 타입

### @Lob

- 데이터베이스 BLOB, CLOB 타입과 매핑
- @Lob에는 지정할 수 있는 속성이 없다.
- 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
- CLOB: String, char[], java.sql.CLOB
- BLOB: byte[], java.sql. BLOB

### @Transient

- 필드 매핑X

## 기본 키 매핑

### 기본 키 매핑 어노테이션

- @Id
  - 직접 할당
- @GeneratedValue
  - 자동할당
  - IDENTITY: 데이터베이스에 위임, MYSQL(auto increament)
  - SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
  - @SequenceGenerator 필요
  - TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용
  - @TableGenerator 필요
  - AUTO: 방언에 따라 자동 지정, 기본값

### 권장하는 식별자 전략

- 기본 키 제약 조건:
  - null 아님,
  - 유일,
  - 변하면 안된다.
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다.
  - 대리키(대체키)를 사용하자.
  - 비즈니스와 상관없는 값을 사용하라
  - 예를 들어 주민등록번호도 기본 키로 적절하기 않다.
- 권장: Long형 + 대체키 + 키 생성전략 사용
  - 스노우 플레이크

### IDENTITY 전략 - 특징

- 기본 키 생성을 데이터베이스에 위임
- 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용 (예: MySQL의 AUTO_INCREMENT)
- JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
- AUTO_INCREMENT는 **데이터베이스에 INSERT SQL을 실행한 이후에 ID 값을 알 수 있음**
- IDENTITY 전략은 em.persist() 시점에 즉시 **INSERT SQL 실행하고 DB에서 식별자를 조회**
  - insert 시점에 id를 jdbc driver에서 알려줌(추가적 select 필요x)
- **모아서 insert하는게 불가능**
  - 일반적으로 성능에 유의미한 단점은 아니다.

### SEQUENCE 전략 - 특징

- 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트(예: 오라클 시퀀스)
- 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용
- 미리 시퀀스테이블에서 next pk 받아옴
- 그 후 commit 시점에 flush
- 시퀀스 테이블에서 매번 받아오는 부분을 최적화
  - 한번의 요청으로 allocationSize(기본값 50)만큼, 다음 pk를 미리 받아옴
  - 즉, DB 시퀀스는 1, 2, 3, … 하나씩만 올라가지만, Hibernate는 그걸 allocationSize만큼 묶어서 캐싱
  - **DB 시퀀스와 allocationSize는 반드시 맞춰야 함**
    - 데이터베이스 시퀀스 값이 하나씩 증가하도록 설정되어 있으면 이 값을 반드시 1로 설정

## 실전 예제 - 1. 요구사항 분석과 기본 매핑

- 아티팩트(artifact)란 빌드 결과물로 만들어지는 JAR, WAR 등의 파일 이름
- spring boot에서는 프로젝트 이름

- 제약조건은 엔티티에 다 적는게 나중에 찾기 좋음
- DB칼럼과, 자바 컨벤션 네이밍의 불일치
  - 스프링부트 하이버네이트 설정에서 관례를 바꿀 수 있음
  - **자바의 카멜케이스를 읽어서 언더스코어로 부트가 바꿔줌(기본 ㄷㄷㄷㄷㄷ)**

### 데이터 중심 설계의 문제점

- 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
- 테이블의 외래키를 객체에 그대로 가져옴
- 객체 그래프 탐색이 불가능
- 참조가 없으므로 UML도 잘못됨

# 6. 연관관계 매핑 기초

- **객체와 테이블 연관관계의 차이를 이해**
- **객체의 참조와 테이블의 외래 키를 매핑**
- 용어 이해
- 방향(Direction): 단방향, 양방향
- 다중성(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1),다대다(N:M) 이해
- 연관관계의 주인(Owner): 객체 양방향 연관관계는 관리 주인이 필요
  - JPA에서 가장 어려운 내용

## 연관관계가 필요한 이유

- ‘객체지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것이다.’ 토끼책 + 오브젝트 추천

## 단방향 연관관계

### 객체를 테이블에 맞추어 모델링

- (참조 대신에 외래 키를 그대로 사용)
- (외래 키 식별자를 직접 다룸)

### 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.

- 테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾는다.
- 객체는 참조를 사용해서 연관된 객체를 찾는다.
- 테이블과 객체 사이에는 이런 큰 간격이 있다.

## 양방향 연관관계와 연관관계의 주인 1- 기본

- 강의중 가장 중요한 시점
  - 영속성 컨텍스트의 메커니즘
  - 양방향 연관관계와 연관관계의 주인

### 양방향 매핑

- **테이블의 연관관계는 외래키 하나로 양방향이 다있다**
  - 방향이라는게 없음 포린키 하나면 양쪽다 확인가능
- 하지만 객체는 그렇지 않음

- `@OneToMany(mappedBy = "team")`

  - 양방향 관계에서 반대편 어디에 걸려있는지

- **객체는 가급적 단방향이 좋다**
  - 양방향은 신경쓸게 많음

### 연관관계의 주인과 mappedBy

- mappedBy = JPA의 멘탈붕괴 난이도
- mappedBy는 처음에는 이해하기 어렵다.
- **객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.**

### 객체와 테이블이 관계를 맺는 차이

- 객체 연관관계 = 2개
- 회원 -> 팀 연관관계 1개(단방향)
- 팀 -> 회원 연관관계 1개(단방향)
- 테이블 연관관계 = 1개
- 회원 <-> 팀의 연관관계 1개(양방향)

### 객체의 양방향 관계

- 객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단뱡향 관계 2개다.
- 객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야 한다.

### 테이블의 양방향 연관관계

- 테이블은 외래 키 하나로 두 테이블의 연관관계를 관리
- MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계 가짐(양쪽으로 조인할 수 있다.)
- 둘 중 하나로 외래 키를 관리해야 한다.

### 연관관계의 주인(Owner)

양방향 매핑 규칙

- 객체의 두 관계중 하나를 연관관계의 주인으로 지정
- **연관관계의 주인만이 외래 키를 관리(등록, 수정)**
- **주인이 아닌쪽은 읽기만 가능**
- **주인은 mappedBy 속성 사용X**
- 주인이 아니면 mappedBy 속성으로 주인 지정
- 누구를 주인으로?

  - **외래 키가 있는 있는 곳을 주인으로 정해라**
  - 여기서는 Member.team이 연관관계의 주인

- team에서 members의 특정 member 값을 바꾼다면?
  - team의 속성을 변경했는데 다른 테이블로 update쿼리가 나감
  - 그래서 read-only
- 쿼리로 인한 성능이슈도 있음

- 디비의 입장에서도 외래키가 테이블이 N, 없는 테이블이 1

핵심 문제:
**객체 세계(object model)**는 양방향 참조가 가능함 (Order → Member, Member → Order)

**하지만 데이터베이스 세계(DB model)**에서는 **외래 키(FK)**는 한 쪽 테이블에만 존재

즉, DB에는 관계가 항상 단방향이고, **외래 키를 가진 쪽이 관계를 "소유"**함

그래서 JPA에서는 둘 중 하나를 "주인"으로 지정하고, 주인만 외래 키를 등록/수정/삭제할 수 있게 함

### 연관관계의 주인(Owner) 외래 키가 있는 있는 곳

## 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리

- 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다.
- 순수한 객체 관계를 고려하면 항상 양쪽다 값을 입력해야 한다.
- 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다. (순수한 객체 관계를 고려하면 항상 양쪽다 값을 입력해야 한다.)

### 양방향 연관관계 주의 - 실습(여기까지 하면 실무 ok)

- 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자
- getter, setter 때문에 로직이 들어가면 `change` 를 사용함 - 영한쌤
- 연관관계 편의 메소드를 생성하자
  - 양쪽에 있으면 문제 생길 수 있기 때문에 한쪽만 설정하자.

```java
public void changeTeam(Team team) {
  this.team = team;
  team.getMembers().add(this);
}
```

---

- 양방향 매핑시에 무한 루프를 조심하자
- 예: toString(), lombok, JSON 생성 라이브러리
  - member.toString() -> team.toString()
  - 양쪽을 무한호출
  - JSON 역시 마찬가지
  - 가능한 롬복 toString은 쓰지마라

### 컨트롤러에서는 엔티티를 반환하지마라

1. 무한루프의 가능성
2. 엔티티 변경마다 API 스펙이 변경

### 양방향 매핑 정리

- **단방향 매핑만으로도 이미 연관관계 매핑은 완료**
  - 처음에는 무조건 단방향 매핑으로 설계를 완료해라
- **양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐**
- JPQL에서 역방향으로 탐색할 일이 많음
- **단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨(테이블에 영향을 주지 않음)**

## 실전 예제 2 - 연관관계 매핑 시작

- 어지간하면 양방향은 안함
- 포린키로 양방향으로 다찾기 가능
- 그래도 비즈니스적으로 의미 있을 수도 있다
- 멤버를 보면서 멤버의 오더스를 꺼낼일이 있을까?
  - 따로 조회해라...!

# 7. 다양한 연관관계 매핑

## 연관관계 매핑시 고려사항 3가지

- 다중성
  - 다대일: @ManyToOne - 가장 많이
  - 일대다: @OneToMany - 필요할 때
  - 일대일: @OneToOne - 가끔 나옴
  - 다대다: @ManyToMany - 실무 금지
  - 대칭성으로 생각해라
    - 반대케이스
- 단방향, 양방향
  - 테이블
    - 외래 키 하나로 양쪽 조인 가능
    - 사실 방향이라는 개념이 없음
  - 객체
    - 참조용 필드가 있는 쪽으로만 참조 가능
    - 한쪽만 참조하면 단방향
    - 양쪽이 서로 참조하면 양방향
- 연관관계의 주인
  - 테이블은 외래 키 하나로 두 테이블이 연관관계를 맺음
  - 객체 양방향 관계는 A->B, B->A 처럼 참조가 2군데
  - 객체 양방향 관계는 참조가 2군데 있음. 둘중 테이블의 외래 키를 관리할 곳을 지정해야함
  - 연관관계의 주인: 외래 키를 관리하는 참조
  - 주인의 반대편: 외래 키에 영향을 주지 않음, 단순 조회만 가능

연관관계 주인을 제대로 설정하지 않으면 생길 수 있는 문제들

1. DB에 외래 키 값이 null로 저장됨
2. 양쪽 객체 상태는 연결됐지만 DB는 반영 안됨

```java
//  연관관계 설정 시 Best Practice
// 양방향 관계를 설정할 때는 반드시 **주인(외래 키 가진 쪽)**을 먼저 설정하고, 반대편도 같이 세팅
public void setTeam(Team team) {
    this.team = team;
    if (!team.getMembers().contains(this)) {
        team.getMembers().add(this);
    }
}
```

## 다대일 [N:1]

- 외래 키가 있는 쪽이 연관관계의 주인
- 양쪽을 서로 참조하도록 개발

## 일대다 [1:N]

### 단방향

- 표준스펙에 있어 설명하지만 이 모델을 거의 가져가지 않음
- 일대다 단방향은 일대다(1:N)에서 일(1)이 연관관계의 주인
- 테이블 일대다 관계는 항상 다(N) 쪽에 외래 키가 있음
- 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
- @JoinColumn을 꼭 사용해야 함. **그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블을 하나 추가함)**

### 일대다 단방향 매핑의 단점

- **엔티티가 관리하는 외래 키가 다른 테이블에 있음**
- 연관관계 관리를 위해 추가로 UPDATE SQL 실행
- **일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자**

### 일대다 양방향 정리

- 이런 매핑은 공식적으로 존재X
- @JoinColumn(insertable=false, updatable=false)
- 읽기 전용 필드를 사용해서 양방향 처럼 사용하는 방법
- 다대일 양방향을 사용하자

## 일대일 [1:1]

- 일대일 관계는 그 반대도 일대일
- 주 테이블이나 대상 테이블 중에 외래 키 선택 가능
  - **주 테이블에 외래 키**
  - 대상 테이블에 외래 키
- 외래 키에 데이터베이스 유니크(UNI) 제약조건 추가

- 다대일 양방향 매핑 처럼 외래 키가 있는 곳이 연관관계의 주인
- 반대편은 mappedBy 적용

### 일대일 정리

- 주 테이블에 외래 키
  - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
  - 객체지향 개발자 선호
  - JPA 매핑 편리
  - 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
  - 단점: 값이 없으면 외래 키에 null 허용
- 대상 테이블에 외래 키

  - 대상 테이블에 외래 키가 존재
  - 전통적인 데이터베이스 개발자 선호
  - 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
  - 단점: 프록시 기능의 한계로 **지연 로딩으로 설정해도 항상 즉시 로딩됨**(프록시는 뒤에서 설명)

- 너무 먼 미래를 생각하지 않는다.

## 다대다 [N:M]

- **관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음**
- **연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함**
- **객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 가능**
- @ManyToMany 사용
- @JoinTable로 연결 테이블 지정
- 다대다 매핑: 단방향, 양방향 가능

### 다대다 매핑의 한계

- 편리해 보이지만 실무에서 사용X
- 연결 테이블이 단순히 연결만 하고 끝나지 않음
- 주문시간, 수량 같은 데이터가 들어올 수 있음

### 다대다 한계 극복

- **연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)**
- @ManyToMany -> @OneToMany, @ManyToOne

### JPA에서는 @ManyToMany를 쓰면 자동으로 조인 테이블을 만들어서 N:M 관계를 처리하지만 권장하지 않는 이유

1. 연결 테이블에 추가 속성을 넣을 수 없음
2. 연관관계 주인 제어가 불편함 - 중간 테이블에 접근할 수 없기 때문에 커스터마이징이 제한됨
3. 삭제, 업데이트 시 제어가 어렵다 - 중간 테이블만 삭제하거나 관리하는 게 번거롭거나 불가능한 상황이 생김
4. 확장성 & 유지보수 문제 - 연결 테이블이 단순할 때는 괜찮지만, 실무는 변하지 않는 단순함은 거의 없음

나중에 속성 추가하려면 결국 @ManyToMany → 엔티티로 승격 해야 함 → 리팩토링 지옥

---

## ✅ 정규화를 쉽게 말하면?

> **"중복 없이, 데이터가 이상하지 않게, 변경에 강하게 만드는 데이터 분리 공식"**
> ➡️ **정규화는 이런 비효율과 중복을 없애는 방법**

### 🔁 정규화 단계 요약 (쉽게!)

| 단계  | 이름        | 쉽게 말하면                          | 목적                  |
| ----- | ----------- | ------------------------------------ | --------------------- |
| 1NF   | 제1정규형   | **셀 안에 값은 하나만!** 리스트 안됨 | 원자성 확보           |
| 2NF   | 제2정규형   | **중복 데이터는 따로 빼자!**         | 부분 종속 제거        |
| 3NF   | 제3정규형   | **주제 벗어난 정보는 나눠라!**       | 이행 종속 제거        |
| BCNF~ | 고급 정규형 | 거의 안 씀                           | 특별한 종속 관계 제거 |

---

### 🧪 실무에서는 어디까지 쓰냐 -> 💼 보통 실무에서는:

- **3NF (제3정규형)**까지만 적용하는 경우가 많고
- 때로는 **"약간 정규화를 깨는"** 비정규화도 일부러 함 (속도, 단순화 목적)

| 정규화는... | 설명                                              |
| ----------- | ------------------------------------------------- |
| 무엇인가?   | 중복 제거 + 변경에 강한 구조로 테이블 나누는 기법 |
| 왜 하는가?  | 무결성 유지, 확장성 확보, 중복 최소화             |
| 실무에서는? | 보통 3NF까지, 필요시 일부 비정규화                |
| 실수하면?   | 중복, 삭제 이상, 삽입 이상, 성능 문제 발생 가능   |

# 8. 고급 매핑

## 상속관계 매핑

- 관계형 데이터베이스는 상속 관계X
- **슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사**
- 해당 모델링 기법을 DB에서 구현하는 세가지 방법

- **조인을 기본으로, 정말 단순하면 단일로 가라**

### 상속관계 매핑: 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑

- 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
- 각각 테이블로 변환 -> 조인 전략
- 통합 테이블로 변환 -> 단일 테이블 전략
- 서브타입 테이블로 변환 -> 구현 클래스마다 테이블 전략

### 주요 어노테이션

- `@Inheritance(strategy=InheritanceType.XXX)`
  - JOINED: 조인 전략 - JPA와 가장 유사함
  - SINGLE_TABLE: 단일 테이블 전략
  - TABLE_PER_CLASS: 구현 클래스마다 테이블 전략
- `@DiscriminatorColumn(name=“DTYPE”)`
  - 운영에서는 있는게 좋다
- `@DiscriminatorValue(“XXX”)`

- 그냥 상속하면 한테이블에 다 떄려박음 -> 단일 테이블 전략
- 어노테이션만 바꾸면, DB가 변경되어도 코드의 변경이 거의없다.

### 조인 전략

**정석이라고 보면 된다.**

- 장점
  - **테이블 정규화**
  - **외래 키 참조 무결성 제약조건 활용가능**
  - 저장공간 효율화
- 단점
  - 조회시 조인을 많이 사용, 성능 저하
  - 조회 쿼리가 복잡함
  - 데이터 저장시 INSERT SQL 2번 호출
    - 큰 단점은 아니다

### 단일 테이블 전략

dType이 기본

- 장점
  - **조인이 필요 없으므로 일반적으로 조회 성능이 빠름**
  - **조회 쿼리가 단순함**
- 단점
  - 자식 엔티티가 매핑한 컬럼은 모두 null 허용
    - 치명적
  - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다.
    - 상황에 따라서 조회 성능이 오히려 느려질 수 있다.
    - 임계점을 넘어야하는데 이런경우는 사실 거의 없다

### 구현 클래스마다 테이블 전략

- **이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천X**
  - **쓰지마라!!!!**
  - 찾을때가 문제 union all로 다뒤짐
- 장점
  - 서브 타입을 명확하게 구분해서 처리할 때 효과적
    - 인설트, 셀렉트에선 좋을 수 있음
  - not null 제약조건 사용 가능
- 단점
  - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
    - 치명적
  - 자식 테이블을 통합해서 쿼리하기 어려움
    - 치명적
  - 변경하기가 어려움

## Mapped Superclass - 매핑 정보 상속

- 공통 매핑 정보가 필요할 때 사용(id, name)
- 상속관계 매핑X
- 엔티티X, 테이블과 매핑X
- **부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공**
- 조회, 검색 불가(em.find(BaseEntity) 불가)
- **직접 생성해서 사용할 일이 없으므로 추상 클래스 권장**

- 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- **참고: @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능**

## 실전 예제 4 - 상속관계 매핑

- JPA에서 영속하기 직전 특정 메서드 호출 같은 방식으로 생성시간 같은 값 채우기가능
- 실전에서 상속관계를 쓰느냐?
  - 노가다를 하더라도 복잡도를 관리하는 측면에서 안나누는게 방법일 수도 있음
  - 상속관계로 사용하다 데이터양이 너무 많아지자, 복잡해서 테이블을 단순하게 관리하기도 함

# 9. 프록시와 연관관계 관리

## 목차

- 프록시
  즉시 로딩과 지연 로딩
  지연 로딩 활용
  영속성 전이: CASCADE
  고아 객체
  영속성 전이 + 고아 객체, 생명주기
  실전 예제 - 5.연관관계 관리

## 프록시

### 프록시 기초

- em.find() vs em.getReference()
- em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
- em.getReference(): 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회

### 프록시 특징

- 실제 클래스를 상속 받아서 만들어짐
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상)
- 프록시 객체는 실제 객체의 참조(target)를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출

- 프록시 객체는 처음 사용할 때 한 번만 초기화
- **프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님**
  - 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능
- **영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환**
- 프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시 주의해야함
  - (== 비교 실패, 대신 instance of 사용)
  - 실무에서 이럴일이 있을까...?
- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면문제 발생
  - (하이버네이트는 org.hibernate.LazyInitializationException 예외를 터트림)

### 프록시 확인

- 프록시 인스턴스의 초기화 여부 확인

  - PersistenceUnitUtil.isLoaded(Object entity)

- 프록시 클래스 확인 방법

  - entity.getClass().getName() 출력(..javasist.. or HibernateProxy...)

- 프록시 강제 초기화

  - org.hibernate.Hibernate.initialize(entity);

- 참고: JPA 표준은 강제 초기화 없음
  - 강제 호출: member.getName()

## 즉시 로딩과 지연 로딩

- 지연 로딩 LAZY을 사용해서 프록시로 조회
- 즉시 로딩 EAGER를 사용해서 함께 조회

### 프록시와 즉시로딩 주의

- **가급적 지연 로딩만 사용(특히 실무에서)**
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- **즉시 로딩은 JPQL에서 N+1 문제를 일으킨다.**

  - 쿼리를 1개 날렸는데 실제로 N개 나감
  - N + 1 문제는 JPA에서 엔티티를 조회할 때, 예상보다 더 많은 SQL 쿼리가 실행
  - "1": 최초의 JPQL 쿼리로 엔티티 목록을 조회하는 쿼리.
  - "N": 조회된 각 엔티티와 연관된 엔티티(예: @OneToMany, @ManyToOne)를 조회하기 위해 추가로 실행되는 쿼리들.

- **@ManyToOne, @OneToOne은 기본이 즉시 로딩 -> LAZY로 설정**
- @OneToMany, @ManyToMany는 기본이 지연 로딩

### 지연 로딩 활용 - 실무

- 모든 연관관계에 지연 로딩을 사용해라!
- **실무에서 즉시 로딩을 사용하지 마라!**
- JPQL fetch 조인이나, 엔티티 그래프 기능을 사용해라!(뒤에서 설명)
- **즉시 로딩은 상상하지 못한 쿼리가 나간다.**

## 영속성 전이(CASCADE)와 고아 객체

- 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속상태로 만들도 싶을 때
- 예: 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장.

### 영속성 전이: CASCADE - 주의!

- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐
- **대상을 하나의 엔티티로만 관리하는게 아니라면 안쓰는게 맞다.**
  - 엔티티의 라이프사이클이 같을 때
  - 단일 소유자
    - 게시글과 댓글

### CASCADE의 종류

- **ALL: 모두 적용**
- **PERSIST: 영속**
- **REMOVE: 삭제**
- MERGE: 병합
- REFRESH: REFRESH
- DETACH: DETACH

### 고아 객체

- 고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
- orphanRemoval = true
- Parent parent1 = em.find(Parent.class, id);

  - parent1.getChildren().remove(0); //자식 엔티티를 컬렉션에서 제거

- DELETE FROM CHILD WHERE ID=?

### 고아 객체 - 주의

- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로보고 삭제하는 기능
- **참조하는 곳이 하나일 때 사용해야함!**
- **특정 엔티티가 개인 소유할 때 사용**
- @OneToOne, @OneToMany만 가능
- 참고: 개념적으로 부모를 제거하면 자식은 고아가 된다.
  - 따라서 고아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거된다.
  - 이것은 CascadeType.REMOVE처럼 동작한다.

### 영속성 전이 + 고아 객체, 생명주기

- CascadeType.ALL + orphanRemoval=true
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- **두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음**
- 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용

## 실전 예제 5 - 연관관계 관리

### 글로벌 페치 전략 설정

- 모든 연관관계를 지연 로딩으로
- @ManyToOne, @OneToOne은 기본이 즉시 로딩이므로 지연 로딩으로 변경

영속성 전이 설정

- Order -> Delivery를 영속성 전이 ALL 설정
- Order -> OrderItem을 영속성 전이 ALL 설정

# 10. 값 타입

## 기본값 타입

### JPA의 데이터 타입 분류

- 엔티티 타입
  - @Entity로 정의하는 객체
  - 데이터가 변해도 식별자로 지속해서 추적 가능
  - 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능
- 값 타입
  - int, Integer, String처럼 단순히 값으로 사용하는 자바 기본타입이나 객체
  - 식별자가 없고 값만 있으므로 변경시 추적 불가
  - 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체

### 값 타입 분류

- 기본값 타입
- 자바 기본 타입(int, double)
- 래퍼 클래스(Integer, Long)
- String
- 임베디드 타입(embedded type, 복합 값 타입)
- 컬렉션 값 타입(collection value type)예): String name, int age

- 생명주기를 엔티티의 의존
- 예) 회원을 삭제하면 이름, 나이 필드도 함께 삭제
- 값 타입은 공유하면X
- 예) 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안됨

### 참고: 자바의 기본 타입은 절대 공유X

- int, double 같은 기본 타입(primitive type)은 절대 공유X
- 기본 타입은 항상 값을 복사함
- Integer같은 래퍼 클래스나 String 같은 특수한 클래스는 공유 가능한 객체이지만 변경X

## 임베디드 타입

- 새로운 값 타입을 직접 정의할 수 있음
- JPA는 임베디드 타입(embedded type)이라 함
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함
- int, String과 같은 값 타입
- **이런 기능이 왜 없나 했다...!!!**

### 임베디드 타입 사용법

- @Embeddable: 값 타입을 정의하는 곳에 표시
- @Embedded: 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수

### 임베디드 타입의 장점

- 재사용
- 높은 응집도
- Period.isWork()처럼 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있음
- 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존함

### 임베디드 타입과 테이블 매핑

- 임베디드 타입은 엔티티의 값일 뿐이다.
- 임베디드 타입을 사용하기 **전과 후에 매핑하는 테이블은 같다.**
- 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음

### @AttributeOverride: 속성 재정의

- 한 엔티티에서 같은 값 타입을 사용하면?
- 컬럼 명이 중복됨
- @AttributeOverrides, @AttributeOverride를 사용해서 컬러 명 속성을 재정의

## 값 타입과 불변 객체

- 값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다.
- 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.

### 값 타입 공유 참조

- 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- 부작용(side effect) 발생

### 객체 타입의 한계

- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
- 문제는 임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입이다.
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- 객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.
- 객체의 공유 참조는 피할 수 없다.

### 불변 객체

- 객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단
- 값 타입은 불변 객체(immutable object)로 설계해야함
- 불변 객체: 생성 시점 이후 절대 값을 변경할 수 없는 객체
- **생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 됨**
- 참고: Integer, String은 자바가 제공하는 대표적인 불변 객체

**불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수 있다.**

## 값 타입의 비교

- **동일성(identity) 비교: 인스턴스의 참조 값을 비교, == 사용**
- **동등성(equivalence) 비교: 인스턴스의 값을 비교, equals() 사용**
  - equals 기본은 == 비교
  - override 해야함 - 왠만하면 자동으로 생성되는 것을 사용할 것
- 값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야 함
- 값 타입의 equals() 메소드를 적절하게 재정의(주로 모든 필드사용)

## 값 타입 컬렉션

- RDB는 기본적으로 컬렉션을 못넣는다(원자성)
  - 컬렉션은 일대다 개념
  - 요즘은 JSON 지원
-

- 값 타입을 하나 이상 저장할 때 사용
- @ElementCollection, @CollectionTable 사용
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
- 컬렉션을 저장하기 위한 별도의 테이블이 필요함

### 값 타입 컬렉션 사용

- 값 타입 저장 예제
- 값 타입 조회 예제
- **값 타입 컬렉션도 지연 로딩 전략 사용**
  - **값 타입 컬렉션은 지연 로딩이 기본이다!**
  - **값 타입 컬렉션은 라이프 사이클이 없다(엔티티 따라감)**
- 값 타입 수정 예제
  - 참고: 값 타입 컬렉션은 영속성 전에(Cascade) + 고아 객체 제거 기능을 필수로 가진다고 볼 수 있다.

```java
// 갑타입 변경 - bad case
findMember.getHomeAddress().setCity("newCity");
// 갑타입 변경 - good case
Address a = findMember.getHomeAddress();
findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));
```

```java
// More formally, removes the element with
// the lowest index {@code i} such that
// {@code Objects.equals(o, get(i))}
// == 비교(참조 값)가 아닌 equals로 동일처리
// equals 구현이 필요하다
findMember.getAddressHistory().remove(new Address("old1", "street1", "10000"));
```

### 값 타입 컬렉션의 제약사항

- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값 타입 컬렉션에 **변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제**하고, 값 타입 컬렉션에 있는 **현재 값을 모두 다시 저장**한다.
  - **값 타입 컬렉션은 쓰지마라 - 영한쌤**
- **값은 변경하면 추적이 어렵다.**
  - 오더칼럼을 사용하면 가능은 하지만 복잡하다.
- **값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 함: null 입력X, 중복 저장X**

### 값 타입 컬렉션 대안

- 실무에서는 상황에 따라 **값 타입 컬렉션 대신에 일대다 관계를 고려**
- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
- **영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용**
  - EX) AddressEntity

### 정리

- 엔티티 타입의 특징
  - 식별자O
  - 생명 주기 관리
  - 공유
- 값 타입의 특징

  - 식별자X
  - 생명 주기를 엔티티에 의존
  - 공유하지 않는 것이 안전(복사해서 사용)
  - 불변 객체로 만드는 것이 안전

- 값 타입은 정말 값 타입이라 판단될 때만 사용
  - 정말 정말 간단할 때!
- **엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안됨**
- **식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것은 값 타입이 아닌 엔티티**

## 실전 예제 6 - 값 타입 매핑

- JPA는 프록시 객체를 사용하기에, 직접 값 접근이 아닌 getter를 통해 값 접근을 해야한다.

# 11. 객체지향 쿼리 언어1 - 기본 문법

## 목차

- 객체지향 쿼리 언어 소개
- JPQL
- 기본 문법과 기능
- 페치 조인
- 경로 표현식
- 다형성 쿼리
- 엔티티 직접 사용
- Named 쿼리
- 벌크 연산

### JPA는 다양한 쿼리 방법을 지원

- JPQL
- JPA Criteria
- QueryDSL
- 네이티브 SQL
- JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용

### JPQL 소개

- 가장 단순한 조회 방법
- EntityManager.find()
- 객체 그래프 탐색(a.getB().getC())
- 나이가 18살 이상인 회원을 모두 검색하고 싶다면?

### JPQL

- JPA를 사용하면 엔티티 객체를 중심으로 개발

- 문제는 검색 쿼리
- 검색을 할 때도 **테이블이 아닌 엔티티 객체를 대상으로 검색**
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요

- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원

- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리
- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 객체 지향 SQL

- **단순 문자열이기에 동적쿼리를 만들기 어렵다!**

### Criteria 소개

- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- JPA 공식 기능
- **단점: 너무 복잡하고 실용성이 없다.**
- Criteria 대신에 **QueryDSL 사용 권장**

### QueryDSL 소개

- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- **컴파일 시점에 문법 오류를 찾을 수 있음**
- 동적쿼리 작성 편리함
- 단순하고 쉬움
- **실무 사용 권장**

### 네이티브 SQL 소개

- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
- 예) 오라클 CONNECT BY, 특정 DB만 사용하는 SQL 힌트

### JDBC 직접 사용, SpringJdbcTemplate 등

- JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스등을 함께 사용 가능
- **단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요**
- 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시

## 기본 문법과 쿼리 API

- JPQL(Java Persistence Query Language)
- JPQL은 객체지향 쿼리 언어다.따라서 테이블을 대상으로 쿼리 하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
- JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.

### JPQL 문법

- select m from Member as m where m.age > 18
- 엔티티와 속성은 대소문자 구분O (Member, age)
- JPQL 키워드는 대소문자 구분X (SELECT, FROM, where)
- 엔티티 이름 사용, 테이블 이름이 아님(Member)
- 별칭은 필수(m) (as는 생략가능)

- TypeQuery: 반환 타입이 명확할 때 사용
- Query: 반환 타입이 명확하지 않을 때 사용

### 결과 조회 API

- query.getResultList(): 결과가 하나 이상일 때, 리스트 반환

  - 결과가 없으면 빈 리스트 반환

- query.getSingleResult(): 결과가 정확히 하나, 단일 객체 반환
  - 결과가 없으면: javax.persistence.NoResultException
    - 추상화 된 API는 트라이캐치를 안해도 되지만 스프링이 해줌
  - 둘 이상이면: javax.persistence.NonUniqueResultException
- 파라미터 바인딩 - 이름 기준, 위치 기준
  - **위치 기준은 쓰지마라**

## 프로젝션(SELECT)

- **SELECT 절에 조회할 대상을 지정하는 것**
- 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
- SELECT m FROM Member m -> 엔티티 프로젝션
- SELECT m.team FROM Member m -> 엔티티 프로젝션
- SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
- SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
- DISTINCT로 중복 제거

### 프로젝션 - 여러 값 조회

- SELECT m.username, m.age FROM Member m

- 1. Query 타입으로 조회
- 2. Object[] 타입으로 조회
- 3. new 명령어로 조회

  - entity가 아닌 경우
  - 단순 값을 DTO로 바로 조회
  - 패키지 명을 포함한 전체 클래스 명 입력
  - 순서와 타입이 일치하는 생성자 필요

  ```java
  SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM
  Member m
  ```

## 페이징

- JPA는 페이징을 다음 두 API로 추상화
- setFirstResult(int startPosition) : 조회 시작 위치 (0부터 시작)
- setMaxResults(int maxResult) : 조회할 데이터 수

## 조인

- 내부 조인: `SELECT m FROM Member m [INNER] JOIN m.team t`
- 외부 조인: `SELECT m FROM Member m LEFT [OUTER] JOIN m.team t`
- 세타 조인: `select count(m) from Member m, Team t where m.username = t.name`

1. 내부 조인 (INNER JOIN): 두 테이블에서 조인 조건을 만족하는 행들만 결과로 반환.
   - 조건에 맞지 않으면 무시됨.
   - 기본적으로 사용. 속도 빠르고 의도 명확.
2. 외부 조인 (OUTER JOIN): 한쪽(또는 양쪽) 테이블에 매칭되는 행이 없어도 결과에 포함.

   - 선택적 매칭이 필요할 때 (예: 없는 값도 보여주기).
   - LEFT OUTER JOIN: 왼쪽 테이블은 무조건 포함, 오른쪽에 매칭 안 되면 NULL
   - RIGHT OUTER JOIN: 오른쪽 테이블은 무조건 포함
   - FULL OUTER JOIN: 양쪽 테이블의 모든 행 포함 (MySQL은 지원하지 않음, 대신 UNION 사용)

3. 세타 조인 (Theta JOIN): 두 테이블 간 = (등호) 외의 비교 연산자를 사용하는 조인
   - `> , <, !=, >=, <=, <>` 등을 사용 가능
   - 조건이 등치가 아닌 경우만 사용. 주로 분석용 쿼리에서 활용.

```sql
--  조건이 단순 등치가 아니기 때문에 세타 조인
-- 일반적으로 INNER JOIN과 함께 사용됨 (즉, 내부 조인의 특수한 형태)
SELECT *
FROM employee e
JOIN department d ON e.salary > d.budget;
```

### 조인 - ON 절

- ON절을 활용한 조인(JPA 2.1부터 지원)
- 1. 조인 대상 필터링
- 2. **연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)**

## 서브 쿼리

- **메인 쿼리와 서브 쿼리가 상관없게 짜야 성능이 좋다**
- **하이버네이트6 부터는 FROM 절의 서브쿼리를 지원**

### 서브 쿼리 지원 함수

- [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
  - {ALL | ANY | SOME} (subquery)
  - ALL 모두 만족하면 참
  - ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

## JPQL 타입 표현과 기타식

- 문자: ‘HELLO’, ‘She’’s’
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (패키지명 포함)
  - `where m.type = jpql.MemberType.ADMIN`
  - `where m.type = :userType` + `setParameter("userType, MemberType.ADMIN)`
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

  - dType

- SQL과 문법이 같은 식
- EXISTS, IN
- AND, OR, NOT
- =, >, >=, <, <=, <>
- BETWEEN, LIKE, IS NULL

## 조건식(CASE 등등)

```sql
-- 기본 CASE 식
select
case when m.age <= 10 then '학생요금'
when m.age >= 60 then '경로요금'
else '일반요금'
end
from Member m
-- 단순 case 식
select
case t.name
when '팀A' then '인센티브110%'
when '팀B' then '인센티브120%'
else '인센티브105%'
end
from Team t
```

### 조건식 - CASE 식

- COALESCE: 하나씩 조회해서 null이 아니면 반환

- NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

```sql
-- 사용자 이름이 없으면 이름 없는 회원을 반환
select coalesce(m.username,'이름 없는 회원') from Member m

-- 사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환
select NULLIF(m.username, '관리자') from Member m
```

## JPQL 함수

- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE, INDEX(JPA 용도)

### 사용자 정의 함수 호출

- 하이버네이트는 사용전 방언에 추가해야 한다.
- 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.
- **이미 디비에 따라 다 등록되어 있다**

# 12. 객체지향 쿼리 언어2 - 중급 문법

## 경로 표현식

- .(점)을 찍어 객체 그래프를 탐색하는 것

- 상태 필드(state field): **단순히 값을 저장하기 위한 필드**(ex: m.username)
- 연관 필드(association field): 연관관계를 위한 필드
- 단일 값 연관 필드: @ManyToOne, @OneToOne, **대상이 엔티티**(ex: m.team)
- 컬렉션 값 연관 필드: @OneToMany, @ManyToMany, **대상이 컬렉션**(ex: m.orders)

### 경로 표현식 특징

- **상태 필드(state field): 경로 탐색의 끝, 탐색X**
- 단일 값 연관 경로: **묵시적 내부 조인(inner join) 발생**, 탐색O
- 컬렉션 값 연관 경로: **묵시적 내부 조인 발생, 탐색X**
- FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
- **묵시적 내부조인이 일어나게 쿼리를 짜지마라**

- JPQL: select m.username, m.age from Member m
- SQL: select m.username, m.age from Member m

### 단일 값 연관 경로 탐색

- JPQL: select o.member from Order o
- SQL:
  select m.\*
  from Orders o
  inner join Member m on o.member_id = m.id

### 명시직 조인, 묵시적 조인

- 명시적 조인: join 키워드 직접 사용
- select m from Member m join m.team t
- 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생(내부 조인만 가능)
- select m.team from Member m

- select o.member.team from Order o -> 성공
- select t.members from Team -> 성공
- select t.members.username from Team t -> 실패 // 컬렉션
- select m.username from Team t join t.members m -> 성공

### 경로 탐색을 사용한 묵시적 조인 시 주의사항

- 항상 내부 조인
- 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌

### 실무 조언

- 가급적 묵시적 조인 대신에 명시적 조인 사용
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움

## 페치 조인 1 - 기본

## 페치 조인 2 - 한계

## 다형성 쿼리

## 엔티티 직접 사용

## Named 쿼리

## 벌크 연산
