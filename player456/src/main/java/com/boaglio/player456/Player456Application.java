package com.boaglio.player456;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//@EnableAsync
@SpringBootApplication
public class Player456Application {

	// main variables
	public static final int RETRY_RATE_IN_MS = 200;
	public static final int RETRY_TIMES = 5;
	public static final int SERVICE_TIMEOUT_DEFAULT_IN_MS = 5000;
	public static final int SERVICE_TIMEOUT_FALLBACK_IN_MS = 500;

	public static void main(String[] args) {
		SpringApplication.run(Player456Application.class, args);
	}

}
