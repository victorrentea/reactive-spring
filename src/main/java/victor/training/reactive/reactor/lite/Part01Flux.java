package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;

import static java.time.Duration.ofMillis;

/**
 * Learn how to create Flux instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 */
@Slf4j
public class Part01Flux {

//========================================================================================

	// TODO Return an empty Flux
	Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	// TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	Flux<String> fooBarFluxFromValues() {
		return null;
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	Flux<String> fooBarFluxFromList() {
		return null;
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	Flux<String> errorFlux() {
		return null;
	}

//========================================================================================

		// TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
	Flux<Long> countEach100ms() {
		log.debug("STA");
//		return Flux.interval(Duration.ofMillis(100)).take(10);
//		return Flux.concat(
//						Flux.just(0),
//						Flux.range(1, 9).delayElements(ofMillis(500)))
//		return Flux.range(0, 10).delayElements(ofMillis(500))
////			.zipWith(Flux.concat(Mono.just(0), Flux.interval(Duration.ofMillis(100))))
//			.doOnEach(e -> log.debug("elem: " + e))
//			.doOnSubscribe(e -> log.debug("Incep efectiv iterarea"))
////			.map(Tuple2::getT1)
//			.map(Long::valueOf)
//			;
	// sau @Daniel:
		return 		Flux.interval(ofMillis(0), ofMillis(100))
			.take(10)
			.doOnSubscribe(subscription -> subscription.cancel())
			.doOnEach(e -> log.debug("elem: " + e))
			.doOnSubscribe(e -> log.debug("Incep efectiv iterarea"))
			;
	}

}
