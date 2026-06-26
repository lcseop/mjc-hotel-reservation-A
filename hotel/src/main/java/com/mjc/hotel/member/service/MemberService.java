package com.mjc.hotel.member.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository memberAuthAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));
    }

    @Transactional
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long memberId, Member requestMember) {
        Member member = getMember(memberId);
        member.setName(requestMember.getName());
        member.setPhone(requestMember.getPhone());
        member.setEmail(requestMember.getEmail());
        member.setStatus(requestMember.getStatus());
        member.setRole(requestMember.getRole());
        member.setEmailVerified(requestMember.getEmailVerified());
        member.setPhoneVerified(requestMember.getPhoneVerified());

        return member;
    }

    @Transactional
    public Member createMember(
            Member member,
            MemberAuthAccount authAccount,
            List<MemberTermAgreement> termAgreements
    ) {
        Member savedMember = memberRepository.save(member);

        if (authAccount != null) {
            authAccount.setMember(savedMember);
            memberAuthAccountRepository.save(authAccount);
        }

        if (termAgreements != null) {
            termAgreements.forEach(termAgreement -> {
                termAgreement.setMember(savedMember);
                memberTermAgreementRepository.save(termAgreement);
            });
        }

        return savedMember;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
