package com.mjc.hotel.student;

import com.mjc.hotel.student.entity.Student;
import com.mjc.hotel.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class StudentServiceTests {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    @Commit
    public void test() {
        Student student = Student
                .builder()
                .name("seop")
                .build();

        studentRepository.save(student);
    }
}
