package com.mjc.hotel.member.service;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberAuthAccountRequestDto;
import com.mjc.hotel.member.dto.MemberTermAgreementRequestDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TermRepository termRepository;
    private final MemberDtoMapper memberDtoMapper;
    private final PasswordEncoder passwordEncoder;

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long sid) {
        return memberRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. sid=" + sid));
    }

    public MemberAuthAccount getAuthAccount(Long sid) {
        return findAuthAccount(sid);
    }

    public List<MemberAuthAccount> getAuthAccountsByMember(Long memberSid) {
        getMember(memberSid);
        return memberAuthAccountRepository.findByMember_Sid(memberSid);
    }

    public MemberTermAgreement getTermAgreement(Long sid) {
        return findTermAgreement(sid);
    }

    public List<MemberTermAgreement> getTermAgreementsByMember(Long memberSid) {
        getMember(memberSid);
        return memberTermAgreementRepository.findByMember_Sid(memberSid);
    }

    @Transactional
    public Member saveMember(Member member) {
        prepareRequiredMemberValues(member);
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long sid, Member requestMember) {
        Member member = getMember(sid);
        member.setName(requestMember.getName());
        member.setPhone(requestMember.getPhone());
        if (requestMember.getEmail() != null) {
            member.setEmail(requestMember.getEmail());
        }
        if (requestMember.getStatus() != null) {
            member.setStatus(requestMember.getStatus());
        }
        if (requestMember.getRole() != null) {
            member.setRole(requestMember.getRole());
        }
        if (requestMember.getEmailVerified() != null) {
            member.setEmailVerified(requestMember.getEmailVerified());
        }
        if (requestMember.getPhoneVerified() != null) {
            member.setPhoneVerified(requestMember.getPhoneVerified());
        }
        if (requestMember.getPoint() != null) {
            member.setPoint(requestMember.getPoint());
        }

        return member;
    }

    @Transactional
    public Member createMember(
            Member member,
            MemberAuthAccount authAccount,
            List<MemberTermAgreement> termAgreements
    ) {
        prepareRequiredMemberValues(member);
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
    public MemberAuthAccount createAuthAccount(MemberAuthAccountRequestDto request) {
        Member member = getMember(request.getMemberSid());
        MemberAuthAccount authAccount = memberDtoMapper.toAuthAccount(request, member);
        authAccount.setPasswordHash(resolvePasswordHash(request.getPassword(), request.getPasswordHash()));
        return memberAuthAccountRepository.save(authAccount);
    }

    @Transactional
    public MemberAuthAccount updateAuthAccount(Long sid, MemberAuthAccountRequestDto request) {
        MemberAuthAccount authAccount = findAuthAccount(sid);
        authAccount.setMember(getMember(request.getMemberSid()));
        authAccount.setProvider(request.getProvider());
        authAccount.setProviderUserId(request.getProviderUserId());
        authAccount.setPasswordHash(resolvePasswordHash(request.getPassword(), request.getPasswordHash()));
        authAccount.setLastLoginAt(request.getLastLoginAt());

        return authAccount;
    }

    @Transactional
    public MemberTermAgreement createTermAgreement(MemberTermAgreementRequestDto request) {
        Member member = getMember(request.getMemberSid());
        Term term = findTerm(request.getTermSid());
        return memberTermAgreementRepository.save(memberDtoMapper.toTermAgreement(request, member, term));
    }

    @Transactional
    public MemberTermAgreement updateTermAgreement(Long sid, MemberTermAgreementRequestDto request) {
        MemberTermAgreement termAgreement = findTermAgreement(sid);
        termAgreement.setMember(getMember(request.getMemberSid()));
        termAgreement.setTerm(findTerm(request.getTermSid()));
        termAgreement.setIsAgreed(request.getIsAgreed());
        termAgreement.setAgreedAt(request.getAgreedAt());
        termAgreement.setWithdrawnAt(request.getWithdrawnAt());

        return termAgreement;
    }

    @Transactional
    public void deleteMember(Long sid) {
        Member member = getMember(sid);
        member.markDeleted();
    }

    @Transactional
    public void deleteAuthAccount(Long sid) {
        MemberAuthAccount authAccount = findAuthAccount(sid);
        authAccount.markDeleted();
    }

    @Transactional
    public void deleteTermAgreement(Long sid) {
        MemberTermAgreement termAgreement = findTermAgreement(sid);
        termAgreement.markDeleted();
    }

    private MemberAuthAccount findAuthAccount(Long sid) {
        return memberAuthAccountRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 로그인 인증 정보입니다. sid=" + sid));
    }

    private MemberTermAgreement findTermAgreement(Long sid) {
        return memberTermAgreementRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 약관 동의입니다. sid=" + sid));
    }

    private Term findTerm(Long sid) {
        return termRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. sid=" + sid));
    }

    private String resolvePasswordHash(String password, String passwordHash) {
        if (password != null && !password.isBlank()) {
            return passwordEncoder.encode(password);
        }
        return passwordHash;
    }

    private void prepareRequiredMemberValues(Member member) {
        if (member.getEmail() == null) {
            throw new IllegalArgumentException("회원 이메일은 필수입니다.");
        }
        if (member.getStatus() == null) {
            member.setStatus(MemberStatus.ACTIVE);
        }
        if (member.getRole() == null) {
            member.setRole(MemberRole.USER);
        }
        if (member.getEmailVerified() == null) {
            member.setEmailVerified(false);
        }
        if (member.getPhoneVerified() == null) {
            member.setPhoneVerified(false);
        }
        if (member.getPoint() == null) {
            member.setPoint(0);
        }
    }
}
