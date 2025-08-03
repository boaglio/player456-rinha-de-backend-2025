package com.boaglio.player456.old;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {
//
//   	@Bean
//	public Executor taskExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setThreadNamePrefix("boaglio-");
//		executor.setCorePoolSize(3); // Increased for 0.55 CPU
//		executor.setMaxPoolSize(6);  // Allow more scaling for burst tasks
//		executor.setQueueCapacity(20); // Increased for 155MB memory
//		executor.setKeepAliveSeconds(30); // Reduced to free resources faster
//		executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
//		executor.setAllowCoreThreadTimeOut(true); // Allow core threads to timeout
//		executor.setThreadFactory(new LowResourceThreadFactory("boaglio-"));
//		executor.initialize();
//		return executor;
//	}
}
