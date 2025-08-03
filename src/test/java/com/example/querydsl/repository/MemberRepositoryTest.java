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

}