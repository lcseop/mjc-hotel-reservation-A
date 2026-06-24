package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.entity.Student;
import com.mjc.hotel.room.mapper.StudentMapper;
import com.mjc.hotel.room.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentMapper studentMapper;
    private final StudentRepository studentRepository;

    @GetMapping("/mapperStudents")
    public List<Student> getStudents(){
        return studentMapper.getStudents();
    }

    @GetMapping("/repositoryStudents")
    public List<Student> getStudents2(){
        return studentRepository.findAll();
    }
}
