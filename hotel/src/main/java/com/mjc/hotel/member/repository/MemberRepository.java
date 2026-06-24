package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
