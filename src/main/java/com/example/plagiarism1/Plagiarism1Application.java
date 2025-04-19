package com.example.plagiarism1;

import org.apache.tika.Tika;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Plagiarism1Application {

	public static void main(String[] args) {
		SpringApplication.run(Plagiarism1Application.class, args);
	}
	@Bean
	public Tika tika() {
		return new Tika();
	}
}
