package victor.training.reactive.reactor.lite;

import reactor.core.publisher.ConnectableFlux;
import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Learn how to merge flux.
 *
 * @author Sebastien Deleuze
 */
public class Part05Merge {

//========================================================================================

	// TODO Merge flux1 and flux2 values with interleave
	Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
		return flux1.mergeWith(flux2);
	}

//========================================================================================

	// TODO Merge flux1 and flux2 values with no interleave (flux1 values and then f  values)
	Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {


		// Hot publisher = external events that occur ar random moments
		// multiple subscribers will find out about data published AFTER they subscribe

		// COld publisher (99%) = nothing happens until you .subscribe()
		// Imagine you want to bring 1TB of data from Mono and process it with 2 subscriber

		Flux<String> f = Flux.just("a","b").delayElements(Duration.ofMillis(100));//~mongo.query();

//		ConnectableFlux<String> conn = f.publish();
//		conn.autoConnect(2);
//		conn.subscribe(func1); // might miss the data.
//		sleep(1);
//		conn.subscribe(func2); // might miss the data.
//		conn.connect();

		f.delayUntil(this::team1Func)
			.delayUntil(this::team2Func)
			.subscribe();

		// or (better):
//		f.flatMap(func2).subscribe(func1);

		return flux1.concatWith(flux2);
	}

	public Mono<Void> team1Func(String data) {
		return Mono.empty();
	}
	public Mono<Void> team2Func(String data) {
		return Mono.empty();
	}

//========================================================================================

	// TODO Create a Flux containing the value of mono1 then the value of mono2
	Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return null;
	}

}
