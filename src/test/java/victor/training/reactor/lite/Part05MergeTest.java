package victor.training.reactor.lite;

import victor.training.reactor.lite.domain.User;
import victor.training.reactor.lite.repository.ReactiveRepository;
import victor.training.reactor.lite.repository.ReactiveUserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Learn how to merge flux.
 *
 * @author Sebastien Deleuze
 */
public class Part05MergeTest {

	Part05Merge workshop = new Part05Merge();

	final static User MARIE = new User("mschrader", "Marie", "Schrader");
	final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

	ReactiveRepository<User> repositoryWithDelay = new ReactiveUserRepository(500);
	ReactiveRepository<User> repository          = new ReactiveUserRepository(MARIE, MIKE);

//========================================================================================

	@Test
	public void mergeWithInterleave() {
		Flux<User> flux = workshop.mergeFluxWithInterleave(repositoryWithDelay.findAll(), repository.findAll());
		StepVerifier.create(flux)
				.expectNext(MARIE, MIKE, User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void mergeWithNoInterleave() {
		Flux<User> flux = workshop.mergeFluxWithNoInterleave(repositoryWithDelay.findAll(), repository.findAll());
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL, MARIE, MIKE)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void multipleMonoToFlux() {
		Mono<User> skylerMono = repositoryWithDelay.findFirst();
		Mono<User> marieMono = repository.findFirst();
		Flux<User> flux = workshop.createFluxFromMultipleMono(skylerMono, marieMono);
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, MARIE)
				.verifyComplete();
	}

}
