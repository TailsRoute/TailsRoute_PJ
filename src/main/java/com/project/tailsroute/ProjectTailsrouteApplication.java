package com.project.tailsroute;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class ProjectTailsrouteApplication {

	public static void main(String[] args) {
		// BouncyCastle 프로바이더 등록
		Security.addProvider(new BouncyCastleProvider());

		SpringApplication.run(ProjectTailsrouteApplication.class, args);
	}
}