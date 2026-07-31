package com.mjc.hotel.member.withdrawal.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SocialUnlinkClientRegistry {

    private final Map<MemberAuthProvider, SocialUnlinkClient> clients;

    public SocialUnlinkClientRegistry(List<SocialUnlinkClient> clientList) {
        EnumMap<MemberAuthProvider, SocialUnlinkClient> registered =
                new EnumMap<>(MemberAuthProvider.class);
        for (SocialUnlinkClient client : clientList) {
            SocialUnlinkClient previous = registered.put(client.provider(), client);
            if (previous != null) {
                throw new IllegalStateException("소셜 연결 해제 Client가 중복 등록되었습니다: " + client.provider());
            }
        }
        this.clients = Map.copyOf(registered);
    }

    public SocialUnlinkClient get(MemberAuthProvider provider) {
        SocialUnlinkClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 연결 해제 제공자입니다: " + provider);
        }
        return client;
    }
}
