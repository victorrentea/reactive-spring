/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import java.time.Duration;
import java.util.function.Supplier;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;

/**
 * Learn how to use StepVerifier to test Mono, Flux or any other kind of Reactive Streams Publisher.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/test/release/api/reactor/test/StepVerifier.html">StepVerifier Javadoc</a>
 */
public class Part03StepVerifier {

//========================================================================================

	// TODO Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then completes successfully.
	void expectFooBarComplete(Flux<String> flux) {
		fail();
	}

//========================================================================================

	// TODO Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then a RuntimeException error.
	void expectFooBarError(Flux<String> flux) {
		fail();
	}

//========================================================================================

	// TODO Use StepVerifier to check that the flux parameter emits a User with "swhite"username
	// and another one with "jpinkman" then completes successfully.
	void expectSkylerJesseComplete(Flux<User> flux) {
		fail();
	}

//========================================================================================

	// TODO Expect 10 elements then complete and notice how long the test takes.
	void expect10Elements(Flux<Long> flux) {
		fail();
	}

//========================================================================================

	// TODO Expect the value "later" to arrive 1 hour after subscribe(). Make the test complete in <1 second
	// Manipiulate virtual with StepVerifier#withVirtualTime/.thenAwait
	// TODO expect no signal for 30 minutes
	void expectDelayedElement() {
		timeBoundFlow();
	}

	Mono<String> timeBoundFlow() {
		return Mono.just("later").delayElement(Duration.ofHours(1));
	}

	private void fail() {
		throw new AssertionError("workshop not implemented");
	}

}
