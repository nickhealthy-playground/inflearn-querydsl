package com.example.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryBasicTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("member1 찾기")
    void startJPQL() {
        Member findMember = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", "member1")
                .getSingleResult();

        assertThat(findMember.getName()).isEqualTo("member1");
    }

    @Test
    @DisplayName("member1 찾기")
    void startQuerydsl() {
        queryFactory = new JPAQueryFactory(em);
        QMember m = member;

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.name.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getName()).isEqualTo("member1");
    }

    @Test
    void startQtype() {
        queryFactory = new JPAQueryFactory(em);

        // 기본 인스턴스 사용
        // QMember member = QMember.member;

        // 별칭 직접 지정 - 셀프 조인이 아닌 이상 기본 인스턴스로 사용
        QMember m1 = new QMember("m1");

        Member findMember = queryFactory
                .selectFrom(m1)
                .where(m1.name.eq("member1")).fetchOne();

        assertThat(findMember.getName()).isEqualTo("member1");
    }


    @Test
    @DisplayName("Querydsl 조건 쿼리")
    void searchCondition() {
        member.name.eq("member1"); // username = 'member1'
        member.name.ne("member1"); //username != 'member1'
        member.name.eq("member1").not(); // username != 'member1'
        member.name.isNotNull(); //이름이 is not null
        member.age.in(10, 20); // age in (10,20)
        member.age.notIn(10, 20); // age not in (10, 20)
        member.age.between(10, 30); //between 10, 30
        member.age.goe(30); // age >= 30
        member.age.gt(30); // age > 30
        member.age.loe(30); // age <= 30
        member.age.lt(30); // age < 30
        member.name.like("member%"); //like 검색
        member.name.contains("member"); // like ‘%member%’ 검색
        member.name.startsWith("member"); //like ‘member%’ 검색
    }

    @Test
    @DisplayName("결과 조회 쿼리")
    void resultQuery() {
        queryFactory = new JPAQueryFactory(em);

//        // 리스트 조회, 데이터 없으면 빈 리스트 반환
//        List<Member> fetchList = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        // 단 건 조회, 결과가 없으면 null, 두 개 이상이면 NonUniqueResultException 발생
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        // 첫 번째 결과 조회
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();

        // 페이징 정보 포함, total count 쿼리 추가 실행(총 2회 쿼리 실행)
        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        List<Member> content = fetchResults.getResults();
        assertThat(content.size()).isEqualTo(4);

        long total = fetchResults.getTotal();
        assertThat(total).isEqualTo(4);

        long limit = fetchResults.getLimit();
        System.out.println("limit = " + limit);

        long offset = fetchResults.getOffset();
        assertThat(offset).isEqualTo(0);

        long totalCount = queryFactory
                .selectFrom(member)
                .fetchCount();

        assertThat(totalCount).isEqualTo(4);

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        queryFactory = new JPAQueryFactory(em);

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.name.asc().nullsLast()) // nullsLast(), nullsFirst(): null 데이터 순서 부여
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getName()).isEqualTo("member5");
        assertThat(member6.getName()).isEqualTo("member6");
        assertThat(memberNull.getName()).isNull();
    }

    /**
     * 조회 건수 제한
     */
    @Test
    void paging1() {
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1) // 0부터 시작
                .limit(2) // 최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    /*전체 조회 수가 필요할 때*/
    @Test
    void paging2() {
        QueryResults<Member> fetchResults = queryFactory.selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(fetchResults.getTotal()).isEqualTo(4);
        assertThat(fetchResults.getLimit()).isEqualTo(2);
        assertThat(fetchResults.getOffset()).isEqualTo(1);
        assertThat(fetchResults.getResults().size()).isEqualTo(2);
    }


    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */
    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * TeamA에 소속된 모든 회원
     */
    @Test
    void join() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result).extracting("name").containsExactly("member1", "member2");

        List<Member> result2 = queryFactory
                .selectFrom(member)
                .where(member.team.name.eq("teamA"))
                .fetch();

        assertThat(result2).extracting("name").containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조회)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // from 절에 여러 엔티티를 선택해서 세타 조인
                .where(member.name.eq(team.name))
                .fetch();

        assertThat(result).extracting("name").containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 전체 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t ON t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM MEMBER m LEFT JOIN TEAM t ON m.TEAM_ID = t.id and t.name = 'teamA'
     */
    @Test
    void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t ON m.name = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.name = t.name
     */
    @Test
    void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 패치 조인
     */
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        // `emf.getPersistenceUnitUtil().isLoaded()` fetch join 로딩 여부 확인 가능
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.name.eq("member1"))
                .fetchOne();

        // `emf.getPersistenceUnitUtil().isLoaded()` fetch join 로딩 여부 확인 가능
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember); // fetch join 로딩 여부 확인 가능
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    /**
     * 서브쿼리 eq 사용
     */
    @Test
    void subQuery() {
        // 나이가 가장 많은 회원 조회
        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(members).extracting("age").containsExactly(40);
    }

    @Test
    void subQueryGoe() {
        // 나이가 평균 나이 이상인 회원
        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(members).extracting("age").containsExactly(30, 40);
    }

    @Test
    void subQueryIn() {
        // 서브쿼리 여러 건 처리, in 사용
        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(members).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    void subQuerySelectExpression() {
        // SELECT절 서브쿼리 사용
        QMember memberSub = new QMember("memberSub");

        List<Tuple> list = queryFactory
                .select(member.name,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : list) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * CASE 문
     */
    @Test
    void simpleCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(10, 20)).then("0~20")
                        .when(member.age.between(21, 30)).then("21~30")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
