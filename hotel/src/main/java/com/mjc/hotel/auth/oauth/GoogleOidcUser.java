package com.mjc.hotel.auth.oauth;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
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

public final class GoogleOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long memberSid;
    private final String email;
    private final String displayName;
    private final MemberRole role;

    public GoogleOidcUser(OidcUser delegate, Member member) {
        this.delegate = delegate;
        this.memberSid = member.getSid();
        this.email = member.getEmail();
        this.displayName = member.getName();
        this.role = member.getRole();

        Set<GrantedAuthority> mappedAuthorities = new LinkedHashSet<>(delegate.getAuthorities());
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        this.authorities = Collections.unmodifiableSet(mappedAuthorities);
    }

    public Long getMemberSid() {
        return memberSid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getProviderUserId() {
        return delegate.getSubject();
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
