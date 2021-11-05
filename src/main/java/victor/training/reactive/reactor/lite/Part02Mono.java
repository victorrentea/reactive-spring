package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;

/**
 * Learn how to create Mono instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 */
public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	public Mono<String> fooMono() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono of data. data can come null.
	public Mono<String> optionalMono(String data) {
		return null;
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	public Mono<String> errorMono() {
		return null;
	}

}
