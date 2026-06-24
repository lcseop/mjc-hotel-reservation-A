package com.mjc.hotel.repository;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.term.entity.Term;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTypeTests {

    @Test
    void memberRepositoryUsesMemberAndLongTypes() throws Exception {
        assertRepositoryTypes(
                "com.mjc.hotel.member.repository.MemberRepository",
                Member.class
        );
    }

    @Test
    void termRepositoryUsesTermAndLongTypes() throws Exception {
        assertRepositoryTypes(
                "com.mjc.hotel.term.repository.TermRepository",
                Term.class
        );
    }

    private static void assertRepositoryTypes(String className,
                                              Class<?> entityType) throws Exception {
        Class<?> repositoryType = Class.forName(className);
        assertTrue(repositoryType.isInterface());
        assertTrue(JpaRepository.class.isAssignableFrom(repositoryType));

        Type genericInterface = repositoryType.getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
        assertEquals(JpaRepository.class, parameterizedType.getRawType());
        assertEquals(entityType, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(Long.class, parameterizedType.getActualTypeArguments()[1]);
    }
}
