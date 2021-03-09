package victor.training.reactivespring.start;

import lombok.Data;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@EnableAsync
@SpringBootApplication
public class AsyncApp {
	public static void main(String[] args) {
		SpringApplication.run(AsyncApp.class, args).close(); // Note: .close added to stop executors after CLRunner finishes
	}

	@Bean
	public ThreadPoolTaskExecutor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("bar-");
		executor.initialize();
		executor.setWaitForTasksToCompleteOnShutdown(true);
		return executor;
	}

}

@Slf4j
@Component
class Drinker implements CommandLineRunner {
	@Autowired
	private Barman barman;

	// TODO [1] inject and use a ThreadPoolTaskExecutor.submit
	// TODO [2] make them return a CompletableFuture + @Async + asyncExecutor bean
	// TODO [3] Messaging...
	public void run(String... args) throws Exception {
		log.info("Submitting my order");


		// NICIODATA nu faci threaduri noi new Thread()
		// 1. e costisitor (milisecunde grele)
		// 2. PERICULOS: daca incerci for i=0..10000 new Thread(sleep (1s)).start()
		 	// threadurile din Java se mapeaza 1:1 pe threaduri din OS >>>>>>> Problema majora a limbajului Java

		// NodeJS - 1 thread
		// JS/TS async/await
		// Kotlin - corutine ~ async-await
		// Si Java incearca de vreo 7 ani Project Loom. Pana e gata, trebuie sa menajam threadurile din Java.
//		new Thread(() -> {
//
//		}).start();

		// *********************************
		//        NU IROSIM THREADURI
		// *********************************


		ExecutorService pool = Executors.newFixedThreadPool(2);

		Future<Beer> futureBeer = pool.submit(barman::getOneBeer);

		Future<Vodka> futureVodka = pool.submit(barman::getOneVodka);

		// aici sunt 3 treaduri in rulare: main doarme si alte 2 lucreaza pentru main

		log.info("Fata pleaca cu comanda");
		Vodka vodka = futureVodka.get(); // cat timp sta main aici: 1s asteptand pe unul dintre worker threads sa termine

		Beer beer = futureBeer.get(); // cat timp sta main aici: 0s - worker #2 deja a terminat

		log.info("Got my order! Thank you lad! " + Arrays.asList(beer, vodka));
	}
}

@Slf4j
@Service
class Barman {
	public Beer getOneBeer() {
		 log.info("Pouring Beer...");
		 ThreadUtils.sleep(1000); // network call
		 return new Beer();
	 }
	 public Vodka getOneVodka() {
		 log.info("Pouring Vodka...");
		 ThreadUtils.sleep(1000); // DB call, file read
		 return new Vodka();
	 }
}

@Data
class Beer {
	public static final String type = "blond";
}

@Data
class Vodka {
	public static final String type = "deadly";
}
