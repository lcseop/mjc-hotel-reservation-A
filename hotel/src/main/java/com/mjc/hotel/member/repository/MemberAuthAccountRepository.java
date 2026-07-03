package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberAuthAccountRepository extends JpaRepository<MemberAuthAccount, Long> {

    List<MemberAuthAccount> findByMember_Sid(Long memberSid);

    @Query("""
            select authAccount
            from MemberAuthAccount authAccount
            join fetch authAccount.member member
            where member.email = :email
              and authAccount.provider = :provider
              and (authAccount.deleted = false or authAccount.deleted is null)
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<MemberAuthAccount> findLoginAuthAccount(
            @Param("email") String email,
            @Param("provider") MemberAuthProvider provider
    );
}
