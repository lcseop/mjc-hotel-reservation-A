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
            where authAccount.provider = :provider
              and authAccount.providerUserId = :providerUserId
              and (authAccount.deleted = false or authAccount.deleted is null)
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<MemberAuthAccount> findActiveByProviderAndProviderUserId(
            @Param("provider") MemberAuthProvider provider,
            @Param("providerUserId") String providerUserId
    );

    @Query("""
            select authAccount
            from MemberAuthAccount authAccount
            join fetch authAccount.member member
            where member.sid = :memberSid
              and authAccount.provider = :provider
              and (authAccount.deleted = false or authAccount.deleted is null)
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<MemberAuthAccount> findActiveByMemberSidAndProvider(
            @Param("memberSid") Long memberSid,
            @Param("provider") MemberAuthProvider provider
    );

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

    @Query("""
            select authAccount
            from MemberAuthAccount authAccount
            join fetch authAccount.member member
            where lower(member.email) = lower(:email)
              and authAccount.provider = com.mjc.hotel.member.entity.MemberAuthProvider.LOCAL
              and member.status = com.mjc.hotel.member.entity.MemberStatus.ACTIVE
              and (authAccount.deleted = false or authAccount.deleted is null)
              and (member.deleted = false or member.deleted is null)
            order by authAccount.sid
            """)
    List<MemberAuthAccount> findAllActiveLocalByEmail(@Param("email") String email);

    @Query("""
            select authAccount
            from MemberAuthAccount authAccount
            join fetch authAccount.member member
            where authAccount.sid = :authAccountSid
              and authAccount.provider = com.mjc.hotel.member.entity.MemberAuthProvider.LOCAL
              and member.status = com.mjc.hotel.member.entity.MemberStatus.ACTIVE
              and (authAccount.deleted = false or authAccount.deleted is null)
              and (member.deleted = false or member.deleted is null)
            """)
    Optional<MemberAuthAccount> findActiveLocalBySid(
            @Param("authAccountSid") Long authAccountSid
    );
}
