package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    /**
     * 순수 JPA 리포지토리와 Querydsl
     */
    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(member).isEqualTo(findMember);

        List<Member> memberList = memberRepository.findAll();
        assertThat(memberList).containsExactly(member);

        List<Member> findMember2 = memberRepository.findByName("member1");
        assertThat(findMember2).containsExactly(member);
    }

    @Test
    void searchTest() {
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("teamB");
        condition.setAgeGoe(35);
        condition.setAgeLoe(42);

        List<MemberTeamDto> memberTeamDtosByWhere = memberRepository.searchByWhere(condition);
        assertThat(memberTeamDtosByWhere).extracting("username").containsExactly("member4");
    }

    @Test
    void searchSimple() {
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> page = memberRepository.searchSimple(condition, pageRequest);

        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    void searchComplex() {
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> page = memberRepository.searchComplex(condition, pageRequest);

        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    /**
     * QuerydslPredicateExecutor - 실무에서 사용하기 어렵다.
     */
    @Test
    void querydslQueryPredicateExecutor() {
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

        QMember member = QMember.member;
        Iterable<Member> findMember = memberRepository.findAll(
                member.age.between(10, 40)
                        .and(member.name.eq("member1"))
        );

        for (Member m : findMember) {
            System.out.println("m = " + m);
        }
    }

}