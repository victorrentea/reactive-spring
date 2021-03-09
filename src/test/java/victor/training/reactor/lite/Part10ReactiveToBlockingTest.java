package victor.training.reactor.lite;

import java.util.Iterator;

import victor.training.reactor.lite.domain.User;
import victor.training.reactor.lite.repository.ReactiveRepository;
import victor.training.reactor.lite.repository.ReactiveUserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Learn how to turn Reactive API to blocking one.
 *
 * @author Sebastien Deleuze
 */
public class Part10ReactiveToBlockingTest {

	Part10ReactiveToBlocking workshop = new Part10ReactiveToBlocking();
	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void monoToValue() {
		Mono<User> mono = repository.findFirst();
		User user = workshop.monoToValue(mono);
		assertThat(user).isEqualTo(User.SKYLER);
	}

//========================================================================================

	@Test
	public void fluxToValues() {
		Flux<User> flux = repository.findAll();
		Iterable<User> users = workshop.fluxToValues(flux);
		Iterator<User> it = users.iterator();
		assertThat(it.next()).isEqualTo(User.SKYLER);
		assertThat(it.next()).isEqualTo(User.JESSE);
		assertThat(it.next()).isEqualTo(User.WALTER);
		assertThat(it.next()).isEqualTo(User.SAUL);
		assertThat(it.hasNext()).isFalse();
	}

}
