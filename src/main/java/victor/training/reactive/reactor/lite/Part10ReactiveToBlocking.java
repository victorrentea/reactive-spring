package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Learn how to turn Reactive API to blocking one.
 *
 * @author Sebastien Deleuze
 */
public class 	Part10ReactiveToBlocking {

//========================================================================================

	// TODO Return the user contained in that Mono
	User monoToValue(Mono<User> mono) {
		return mono.block();
	}

//========================================================================================

	// TODO Return the users contained in that Flux
	Iterable<User> fluxToValues(Flux<User> flux) {
//		flux.toStream().collect(Collectors.toList())
		Iterable<User> users = flux.toIterable();
		Iterator<User> iterator = users.iterator();

		if (iterator.hasNext()) {
			System.out.println(iterator.next());
		}


		return flux.toIterable();
	}

}
