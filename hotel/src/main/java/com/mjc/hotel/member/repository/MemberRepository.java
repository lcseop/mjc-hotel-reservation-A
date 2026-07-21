package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            select count(member)
            from Member member
            where lower(member.email) = lower(:email)
              and (member.deleted = false or member.deleted is null)
            """)
    long countActiveByEmail(@Param("email") String email);
}
