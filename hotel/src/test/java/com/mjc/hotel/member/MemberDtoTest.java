package com.mjc.hotel.member;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberDtoTest {

    @DisplayName("회원 요청 DTO는 호텔 DTO와 같은 Lombok 형식과 요청 필드를 가진다")
    @Test
    public void memberRequestDtoTest() throws NoSuchFieldException, NoSuchMethodException {
        assertCommonDtoShape(
                MemberRequestDto.class,
                String.class,
                String.class,
                String.class,
                MemberStatus.class,
                MemberRole.class,
                Boolean.class,
                Boolean.class
        );

        assertField(MemberRequestDto.class, "name", String.class);
        assertField(MemberRequestDto.class, "phone", String.class);
        assertField(MemberRequestDto.class, "email", String.class);
        assertField(MemberRequestDto.class, "status", MemberStatus.class);
        assertField(MemberRequestDto.class, "role", MemberRole.class);
        assertField(MemberRequestDto.class, "emailVerified", Boolean.class);
        assertField(MemberRequestDto.class, "phoneVerified", Boolean.class);
    }

    @DisplayName("회원 응답 DTO는 호텔 DTO와 같은 Lombok 형식과 응답 필드를 가진다")
    @Test
    public void memberResponseDtoTest() throws NoSuchFieldException, NoSuchMethodException {
        assertCommonDtoShape(
                MemberResponseDto.class,
                Long.class,
                String.class,
                String.class,
                String.class,
                MemberStatus.class,
                MemberRole.class,
                Boolean.class,
                Boolean.class,
                Boolean.class,
                LocalDateTime.class
        );

        assertField(MemberResponseDto.class, "sid", Long.class);
        assertField(MemberResponseDto.class, "name", String.class);
        assertField(MemberResponseDto.class, "phone", String.class);
        assertField(MemberResponseDto.class, "email", String.class);
        assertField(MemberResponseDto.class, "status", MemberStatus.class);
        assertField(MemberResponseDto.class, "role", MemberRole.class);
        assertField(MemberResponseDto.class, "emailVerified", Boolean.class);
        assertField(MemberResponseDto.class, "phoneVerified", Boolean.class);
        assertField(MemberResponseDto.class, "deleted", Boolean.class);
        assertField(MemberResponseDto.class, "deletedAt", LocalDateTime.class);
    }

    private void assertCommonDtoShape(Class<?> dtoClass, Class<?>... constructorTypes) throws NoSuchMethodException {
        assertThat(dtoClass.getDeclaredConstructor()).isNotNull();
        Constructor<?> allArgsConstructor = dtoClass.getDeclaredConstructor(constructorTypes);
        assertThat(allArgsConstructor).isNotNull();
        assertThat(dtoClass.getDeclaredMethod("builder")).isNotNull();
    }

    private void assertField(Class<?> dtoClass, String fieldName, Class<?> fieldType) throws NoSuchFieldException {
        Field field = dtoClass.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);

        String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method getter = Arrays.stream(dtoClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("get" + capitalizedFieldName))
                .findFirst()
                .orElseThrow();
        Method setter = Arrays.stream(dtoClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("set" + capitalizedFieldName))
                .findFirst()
                .orElseThrow();

        assertThat(getter.getReturnType()).isEqualTo(fieldType);
        assertThat(setter.getParameterTypes()).containsExactly(fieldType);
    }
}
