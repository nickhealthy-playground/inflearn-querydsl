package com.example.querydsl;

import com.example.querydsl.entity.Hello;
import com.example.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		// given
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
		QHello qHello = QHello.hello; // Querydsl Q타입 동작 확인

		// when
		Hello result = jpaQueryFactory.selectFrom(qHello).fetchOne();

		// then
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
