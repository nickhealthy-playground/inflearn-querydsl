package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> searchByWhere(MemberSearchCondition condition);
    Page<MemberTeamDto> searchSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchComplex(MemberSearchCondition condition, Pageable pageable);
}
