package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {
	
	@GetMapping("/student")
	public student getStudent() {
		return new student("First", "Last");
		
	}
	
	@GetMapping("/getStudents")
	public List<student> getStudents() {
		
		List <student> students = new ArrayList<>();
		
		students.add(new student("AA", "BB"));
		students.add(new student("AA", "CC"));
		students.add(new student("AA", "DD"));
		students.add(new student("AA", "EE"));
		students.add(new student("AA", "FF"));
		students.add(new student("AA", "GG"));
		students.add(new student("AA", "HH"));
		students.add(new student("AA", "JJ"));
		
		return students;
	}
	
	@GetMapping("/student/{firstName}/{lastName}")
	public student studentPathVariable(@PathVariable("firstName") String firstName, 
			@PathVariable("lastName") String lastName) {
		
		return new student(firstName, lastName);
	}
	
	@GetMapping("/studentQuery")
	public student studentQueryParam(@RequestParam(name="firstName") String firstName) {
		
		return new student(firstName, "lastName");
	}

}
