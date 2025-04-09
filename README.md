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

## 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리

## 실전 예제 2 - 연관관계 매핑 시작
