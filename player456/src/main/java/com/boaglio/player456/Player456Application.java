package com.boaglio.player456;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@SpringBootApplication
public class Player456Application {

	// main variables
	public static final int RETRY_RATE_IN_MS = 200;
	public static final int SERVICE_TIMEOUT_DEFAULT_IN_MS = 500;
	public static final int SERVICE_TIMEOUT_FALLBACK_IN_MS = 500;

	public static void main(String[] args) {
		SpringApplication.run(Player456Application.class, args);
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("boaglio-");
		executor.setCorePoolSize(3); // Increased for 0.55 CPU
		executor.setMaxPoolSize(6);  // Allow more scaling for burst tasks
		executor.setQueueCapacity(20); // Increased for 155MB memory
		executor.setKeepAliveSeconds(30); // Reduced to free resources faster
		executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
		executor.setAllowCoreThreadTimeOut(true); // Allow core threads to timeout
		executor.setThreadFactory(new LowResourceThreadFactory("boaglio-"));
		executor.initialize();
		return executor;
	}

}
