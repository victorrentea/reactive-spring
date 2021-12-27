package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Part04Transform {

//========================================================================================

	// Capitalize the user username, firstname and lastname
	public Mono<User> capitalizeOne(Mono<User> mono) {
		return mono.map(user -> new User(
				user.getUsername().toUpperCase(),
				user.getFirstname().toUpperCase(),
				user.getLastname().toUpperCase()));
	}

//========================================================================================

	// Capitalize the users username, firstName and lastName
	public Flux<User> capitalizeMany(Flux<User> flux) {
		return flux.map(user -> new User(
				user.getUsername().toUpperCase(),
				user.getFirstname().toUpperCase(),
				user.getLastname().toUpperCase()));
	}

//========================================================================================

	// Capitalize the users username, firstName and lastName using #asyncCapitalizeUser
	public Flux<User> asyncCapitalizeMany(Flux<User> flux) {
		return flux.flatMap(this::asyncCapitalizeUser);
	}

	protected Mono<User> asyncCapitalizeUser(User u) {
		return Mono.just(new User(u.getUsername().toUpperCase(), u.getFirstname().toUpperCase(), u.getLastname().toUpperCase()));
	}

}
