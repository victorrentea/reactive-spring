package victor.training.reactor.lite;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;
import reactor.test.publisher.TestPublisher;
import victor.training.reactivespring.start.ThreadUtils;
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
	@Test
	public void burstFlux() {
		Flux<String> flux = Flux.<String>create(sink -> {
			sink.next("A");
			sink.next("B");
			ThreadUtils.sleep(1000);
			sink.next("C");
			ThreadUtils.sleep(1000);
			sink.next("D");
			sink.complete();
		})
			.log()
			.subscribeOn(Schedulers.boundedElastic());

		long t0 = System.currentTimeMillis();
		for (String s : flux.toIterable()) {
			long t1 = System.currentTimeMillis();
			log.debug("Processing " + s + " after " + (t1-t0));
			t0=t1;
			ThreadUtils.sleep(500); // do stuff with element
		}
	}

	private static final Logger log = LoggerFactory.getLogger(Part10ReactiveToBlockingTest.class);
}


// Vreau un flux care emite 2 elemente instantaneu, apoi cate 1 element / secunda.

// Consumerul face toIterable, intre fiecare next() sta 500 ms.