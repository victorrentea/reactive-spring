package victor.training.reactor.lite;

import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;
import victor.training.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

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

	// TODO Merge flux1 and flux2 values with no interleave (flux1 values and then flux2 values)
	Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {

//		List<User> list1 = flux1.collectList().block(); // NO!
//		List<User> list2 = flux2.collectList().block();
//		list2.addAll(list2);
//		return Flux.fromIterable(list2);
		return flux1.concatWith(flux2);
	}

//========================================================================================

	// TODO Create a Flux containing the value of mono1 then the value of mono2
	Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return mono1.concatWith(mono2);
	}


	public static void main(String[] args) {

		Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

		interval
			.publishOn(Schedulers.boundedElastic())
			.map(n -> {
				Mono<Integer> ms = someMethod();
				return n * ms.block();
			})
			.subscribe();
	}

	private static Mono<Integer> someMethod() {
		return Mono.just(1);
	}
}
