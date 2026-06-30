package com.mjc.hotel.member.service;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberSignupRequestDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository memberAuthAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final TermRepository termRepository;
    private final MemberDtoMapper memberDtoMapper;

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long sid) {
        return memberRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. sid=" + sid));
    }

    @Transactional
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long sid, Member requestMember) {
        Member member = getMember(sid);
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
    public Member signup(MemberSignupRequestDto request) {
        List<MemberTermAgreement> termAgreements = Collections.emptyList();

        if (request.getTermAgreements() != null) {
            termAgreements = request.getTermAgreements().stream()
                    .map(termAgreementRequest -> {
                        Term term = termRepository.findById(termAgreementRequest.getSid())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. sid=" + termAgreementRequest.getSid()));
                        return memberDtoMapper.toTermAgreement(termAgreementRequest, term);
                    })
                    .toList();
        }

        return createMember(
                memberDtoMapper.toEntity(request),
                memberDtoMapper.toAuthAccount(request.getAuthAccount()),
                termAgreements
        );
    }

    @Transactional
    public void deleteMember(Long sid) {
        Member member = getMember(sid);
        member.markDeleted();
    }
}
