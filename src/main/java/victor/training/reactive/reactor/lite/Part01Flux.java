package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class Part01Flux {

//========================================================================================

	// Return an empty Flux
	public Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	// Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	public Flux<String> fooBarFluxFromValues() {
		return Flux.just("foo","bar");
	}

//========================================================================================

	// Create a Flux from a List that contains 2 values "foo" and "bar"
	public Flux<String> fluxFromList(List<String> list) {
		return Flux.fromIterable(list);
	}

//========================================================================================

	// Create a Flux that emits an IllegalStateException
	public Flux<String> errorFlux() {
		return Flux.error(new IllegalStateException("Exception launch ... "));
	}

//========================================================================================

	// Create a Flux that emits increasing values from 0 to 9 each 100ms
	public Flux<Long> countEach100ms() {
		return Flux.interval(Duration.ofMillis(100)).take(10);
	}

}
