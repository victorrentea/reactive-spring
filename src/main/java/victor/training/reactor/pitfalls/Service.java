package victor.training.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class Service {
	Mono<String> create(String s) {
		return Mono.just("Create " + s).doOnNext(log::info);
	}
	Mono<Void> update(String s) {
		return Mono.just("Update " + s).doOnNext(log::info).then();
	}
}
@RequiredArgsConstructor
class Foo {
	private final Service service;
	
	public Mono<Void> problem() {
		return service.update("foo");
	}
}

class FooTest {
	private Foo foo = new Foo(new Service());
	@Test
	public void test() {
		StepVerifier.create(foo.problem()).verifyComplete();
	}
}