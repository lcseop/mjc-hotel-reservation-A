package com.mjc.hotel.auth.oauth.principal;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class SocialOidcUser implements OidcUser, SocialPrincipal {

    private final OidcUser delegate;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long memberSid;
    private final MemberAuthProvider provider;

    public SocialOidcUser(OidcUser delegate, Member member, MemberAuthProvider provider) {
        this.delegate = delegate;
        this.memberSid = member.getSid();
        this.provider = provider;

        Set<GrantedAuthority> mappedAuthorities = new LinkedHashSet<>(delegate.getAuthorities());
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
        this.authorities = Collections.unmodifiableSet(mappedAuthorities);
    }

    @Override
    public Long getMemberSid() {
        return memberSid;
    }

    @Override
    public MemberAuthProvider getProvider() {
        return provider;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
