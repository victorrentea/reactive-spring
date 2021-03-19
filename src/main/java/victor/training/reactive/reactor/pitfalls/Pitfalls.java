package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
class Pitfalls {
	public static void main(String[] args) {
		new Pitfalls().create("Test").block();
	}
	Mono<String> create(String s) {
		return Mono.fromSupplier(() -> {
			log.info("Created " + s);
			return s;
		})
			.delayElement(ofMillis(50), boundedElastic())
			// TODO also run audit:
			//  a) doOnNext(audit())
			//  b) doOnNext(.block())
			;
	}
	Mono<Void> audit(String s) {
		return Mono.fromRunnable(() -> log.info("Save audit " + s));
	}
}
