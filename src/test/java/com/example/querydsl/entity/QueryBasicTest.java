package com.example.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryBasicTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
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
}
