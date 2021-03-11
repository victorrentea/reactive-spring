package victor.training.reactor.lite;

import victor.training.reactor.lite.domain.User;
import victor.training.reactor.lite.repository.BlockingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for
 * blocking code that produces or receives data.
 *
 * For those who know RxJava:
 *  - RxJava subscribeOn = Reactor subscribeOn
 *  - RxJava observeOn = Reactor publishOn
 *  - RxJava Schedulers.io <==> Reactor Schedulers.elastic
 *
 * @author Sebastien Deleuze
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactive {

//========================================================================================

	// TODO Create a Flux for reading all users from the blocking repository
	//  deferred until the flux is subscribed, and run it with an elastic scheduler
	Flux<User> blockingRepositoryToFlux(BlockingRepository<User> repository) {
		 return Flux.defer(() -> {
			 Iterable<User> all = repository.findAll();// 1sec
			 Flux<User> flux = Flux.fromIterable(all);
			 return flux;
		 })
			 .subscribeOn(Schedulers.boundedElastic());
	}

//========================================================================================

	// TODO Insert users contained in the Flux parameter in the blocking repository
	//  using an elastic scheduler and return a Mono<Void> that signal the end of the operation
	Mono<Void> fluxToBlockingRepository(Flux<User> flux, BlockingRepository<User> repository) {


		Mono<Void> mono = Mono.<Void>fromRunnable(() -> {
			for (User user : flux.toIterable()) {
				repository.save(user);
			}
			// COD

		}).subscribeOn(Schedulers.boundedElastic());


		return mono;
	}

}
