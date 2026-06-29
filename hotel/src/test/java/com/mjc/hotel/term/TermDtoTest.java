package com.mjc.hotel.term;

import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TermDtoTest {

    @DisplayName("약관 요청 DTO는 호텔 DTO와 같은 Lombok 형식과 요청 필드를 가진다")
    @Test
    public void termRequestDtoTest() throws NoSuchFieldException, NoSuchMethodException {
        assertCommonDtoShape(
                TermRequestDto.class,
                Long.class,
                String.class,
                String.class,
                String.class,
                Boolean.class,
                LocalDateTime.class
        );

        assertField(TermRequestDto.class, "sid", Long.class);
        assertField(TermRequestDto.class, "termType", String.class);
        assertField(TermRequestDto.class, "title", String.class);
        assertField(TermRequestDto.class, "version", String.class);
        assertField(TermRequestDto.class, "isRequired", Boolean.class);
        assertField(TermRequestDto.class, "effectiveAt", LocalDateTime.class);
    }

    @DisplayName("약관 응답 DTO는 호텔 DTO와 같은 Lombok 형식과 응답 필드를 가진다")
    @Test
    public void termResponseDtoTest() throws NoSuchFieldException, NoSuchMethodException {
        assertCommonDtoShape(
                TermResponseDto.class,
                Long.class,
                String.class,
                String.class,
                String.class,
                Boolean.class,
                LocalDateTime.class
        );

        assertField(TermResponseDto.class, "sid", Long.class);
        assertField(TermResponseDto.class, "termType", String.class);
        assertField(TermResponseDto.class, "title", String.class);
        assertField(TermResponseDto.class, "version", String.class);
        assertField(TermResponseDto.class, "isRequired", Boolean.class);
        assertField(TermResponseDto.class, "effectiveAt", LocalDateTime.class);
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
