package com.example.alp1tes1;

import org.springframework.boot.SpringApplication;

public class TestAlp1tes1Application {

	public static void main(String[] args) {
		SpringApplication.from(Alp1tes1Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
