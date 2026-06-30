package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.MemberAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAuthAccountRepository extends JpaRepository<MemberAuthAccount, Long> {

    List<MemberAuthAccount> findByMember_Sid(Long memberSid);
}
