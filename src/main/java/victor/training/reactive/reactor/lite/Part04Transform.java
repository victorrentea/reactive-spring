package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Learn how to transform values.
 *
 * @author Sebastien Deleuze
 */
public class Part04Transform {

//========================================================================================

	// TODO Capitalize the user username, firstname and lastname
	Mono<User> capitalizeOne(Mono<User> mono) {
		return mono.map(User::capitalize);
	}


//========================================================================================

	// TODO Capitalize the users username, firstName and lastName
	Flux<User> capitalizeMany(Flux<User> flux) {
		return flux.map(User::capitalize);
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName using #asyncCapitalizeUser
	Flux<User> asyncCapitalizeMany(Flux<User> flux) {
		return flux.flatMap(u -> asyncCapitalizeUser(u)); // bad
//		return flux.map(user -> asyncCapitalizeUser(user).block()); // bad
	}

	Mono<User> asyncCapitalizeUser(User u) {
		// imagine that involves a WebClient (NON blocking REST call)
		return Mono.just(new User(u.getUsername().toUpperCase(), u.getFirstname().toUpperCase(), u.getLastname().toUpperCase()))
			.map(us -> us.capitalize() )// in memory immediate computation
			.flatMap(us -> asyncCapitalizeUser(us))
			.doOnNext(System.out::println); // async operation
	}

}
