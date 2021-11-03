package victor.training.reactive.intro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
public class Utils {
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}

	public static void waitForEnter() {
		System.out.println("\nHit [ENTER] to continue");
		new Scanner(System.in).next();
	}

	public static void installBlockHound(List<Tuple2<String, String>> excludedClassMethods) {
		log.warn("Installing BlockHound to detect I/O in non-blocking threads");

		Builder builder = BlockHound.builder();
		ServiceLoader.load(BlockHoundIntegration.class).forEach(integration -> integration.applyTo(builder));
		for (Tuple2<String, String> classMethod : excludedClassMethods) {
			builder.allowBlockingCallsInside(classMethod.getT1(), classMethod.getT2());
		}
		builder.install();
	}

	public static void startRefreshWireMockStubsFromJsonContinuously() {
		refreshWireMockStubsFromJsonBlocking();
		File stubFolder = new File("src/test/resources/mappings");
		log.info("Start refreshing Wiremock mappings continuously when any folder in changes in {}",stubFolder.getAbsolutePath());
		Flux.interval(Duration.ofSeconds(1))
			.publishOn(boundedElastic()) // I'm gonna block ! :D
			// .doOnNext(t -> log.debug("POLL folder"))
			.map(t -> extractFileNamesAndLastChanged(stubFolder))
			.distinctUntilChanged()
			.skip(1)
			.doOnNext(t -> log.info("CHANGE detected in mappings > REFRESH wiremock"))
			.flatMap(t -> refreshWireMockStubsFromJson()).subscribe();
	}

	private static Set<String> extractFileNamesAndLastChanged(File folder) {
		return Stream.of(folder.listFiles())
			.map(file -> file.getName() + "-"+file.lastModified())
			.collect(toSet());
	}

	public static void refreshWireMockStubsFromJsonBlocking() {
		refreshWireMockStubsFromJson().block();
	}


	public static Mono<ResponseEntity<Void>> refreshWireMockStubsFromJson() {
		return WebClient.create().post().uri("http://localhost:9999/__admin/mappings/reset").retrieve().toBodilessEntity();
	}
}
