package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.MemberTermAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTermAgreementRepository extends JpaRepository<MemberTermAgreement, Long> {

    List<MemberTermAgreement> findByMember_Sid(Long memberSid);
}
