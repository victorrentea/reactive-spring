package victor.training.reactivespring.start;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

@EnableAsync
@SpringBootApplication
public class AsyncApp {
	public static void main(String[] args) {
		SpringApplication.run(AsyncApp.class, args).close(); // Note: .close added to stop executors after CLRunner finishes
	}

//	@Bean
//	public ThreadPoolTaskExecutor barExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(2);
//		executor.setMaxPoolSize(2);
//		executor.setQueueCapacity(500);
//		executor.setThreadNamePrefix("bar-");
//		executor.initialize();
//		executor.setWaitForTasksToCompleteOnShutdown(true);
//		return executor;
//	}
}

@Slf4j
@Component
@RequiredArgsConstructor
class Drinker implements CommandLineRunner {
	private final Barman barman;

	// TODO [1] Executors
	// TODO [2] ThreadPoolTaskExecutor (spring)
	// TODO [3] @Async
	// TODO [4] HTTP
	// TODO [5] Errors
	public void run(String... args) throws Exception {
		log.debug("Submitting my order");
		long t0 = currentTimeMillis();

		Beer beer = barman.getOneBeer();
		Vodka vodka = barman.getOneVodka();

		long t1 = currentTimeMillis();
		log.debug("Got my order in {} : {}", t1-t0, Arrays.asList(beer, vodka));
	}
}

@Slf4j
@Service
class Barman {
	public Beer getOneBeer() {
		 log.debug("Pouring Beer...");
		 ThreadUtils.sleep(1000);
		 return new Beer();
	 }
	
	 public Vodka getOneVodka() {
		 log.debug("Pouring Vodka...");
		 ThreadUtils.sleep(1000);
		 return new Vodka();
	 }
}

@Data
class Beer {
	public final String type = "blond";
}

@Data
class Vodka {
	public final String type = "deadly";
}
