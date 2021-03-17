package victor.training.reactor.lite;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;

/**
 * Learn how to create Flux instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 */
public class Part01Flux {

//========================================================================================

	// TODO Return an empty Flux
	Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================


//	public static void main(String[] args) {
//		Flux<String> flux = Flux.create(sink -> {
//			sink.next("foo");
//			sink.next("bar");
//		});
//
////		for (String s : flux.toIterable()) {
////			System.out.println(s);
////		}
//
//		List<String> list = flux.collectList().block();
//		System.out.println(list);
//
//	}

	// TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	Flux<String> fooBarFluxFromValues() {
//		return Flux.just("foo", "bar");
		return Flux.create(sink -> {
			// typical use: you have some sort of call-back based system
			sink.next("foo");
			sink.next("bar");
			sink.complete();
		});
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	Flux<String> fooBarFluxFromList() {
		List<String> ids = asList("foo", "bar");
		return Flux.fromIterable(ids);
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	Flux<String> errorFlux() {
//		return Flux.error(new IllegalStateException());
		return Flux.create(sink -> {
			sink.error(new IllegalStateException());
		});
	}

//========================================================================================

		// TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
	Flux<Long> countEach100ms() {
		return Flux.interval(ofMillis(100)).take(10);
	}

}
