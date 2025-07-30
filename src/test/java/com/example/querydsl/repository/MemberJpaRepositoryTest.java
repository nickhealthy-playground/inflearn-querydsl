package com.example.querydsl.repository;

import com.example.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    /**
     * 순수 JPA 리포지토리와 Querydsl
     */
    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(member).isEqualTo(findMember);

        List<Member> memberList = memberJpaRepository.findAll();
        assertThat(memberList).containsExactly(member);

        List<Member> findMember2 = memberJpaRepository.findByName("member1");
        assertThat(findMember2).containsExactly(member);

        // QueryDSL
        List<Member> findMembersQueryDSL = memberJpaRepository.findAll_QueryDSL();
        assertThat(findMembersQueryDSL).containsExactly(member);

        List<Member> findMemberQueryDSL = memberJpaRepository.findByName_QueryDSL("member1");
        assertThat(findMemberQueryDSL).containsExactly(member);
    }

}