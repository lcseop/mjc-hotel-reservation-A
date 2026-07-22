package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            select count(member)
            from Member member
            where lower(member.email) = lower(:email)
              and (member.deleted = false or member.deleted is null)
            """)
    long countActiveByEmail(@Param("email") String email);

    @Query("""
            select member
            from Member member
            where lower(member.email) = lower(:email)
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<Member> findActiveByEmail(@Param("email") String email);

    @Query("""
            select member
            from Member member
            where member.sid = :memberSid
              and member.status = com.mjc.hotel.member.entity.MemberStatus.ACTIVE
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<Member> findActiveBySid(@Param("memberSid") Long memberSid);

    @Query("""
            select member
            from Member member
            where lower(member.email) = lower(:email)
              and (member.deleted = false or member.deleted is null)
            order by member.sid
            """)
    List<Member> findAllActiveByEmail(@Param("email") String email);
}
