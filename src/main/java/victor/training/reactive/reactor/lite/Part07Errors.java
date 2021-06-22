/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Learn how to deal with errors.
 *
 * @author Sebastien Deleuze
 * @see Exceptions#propagate(Throwable)
 */
public class Part07Errors {

//========================================================================================

	// TODO Return a Mono<User> containing User.SAUL when an error occurs in the input Mono, else do not change the input Mono.
	Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
		return null;
	}

//========================================================================================

	// TODO Return a Flux<User> containing User.SAUL and User.JESSE when an error occurs in the input Flux, else do not change the input Flux.
	Flux<User> betterCallSaulAndJesseForBogusFlux(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Implement a method that capitalizes each user of the incoming flux using the
	// #capitalizeUser method and emits an error containing a GetOutOfHereException error
	Flux<User> capitalizeMany(Flux<User> flux) {
		return null;
	}

	User capitalizeUser(User user) throws GetOutOfHereException {
		if (user.equals(User.SAUL)) {
			throw new GetOutOfHereException();
		}
		return new User(user.getUsername(), user.getFirstname(), user.getLastname());
	}

	protected final class GetOutOfHereException extends Exception {
		private static final long serialVersionUID = 0L;
	}


//========================================================================================

	// TODO retrieve all Products with retrieveProduct. In case any fails, return an empty list.
	public Mono<List<Order>> catchReturnDefault(List<Long> ids) {
		try {
			List<Order> products = new ArrayList<>();
			for (Long id : ids) {
				retrieveOrder(id).block();
			}
			return Mono.just(products);
		} catch (Exception e) {
			return Mono.just(emptyList());
		}
	}

	private Mono<Order> retrieveOrder(Long id) { // imagine a network call
		if (id % 4 == 0) {
			return Mono.error(new RuntimeException());
		} else {
			return Mono.just(new Order(id));
		}
	}

	public static class Order {
		private final Long id;

		public Order(Long id) {
			this.id = id;
		}
		public Long getId() {
			return id;
		}
	}



}
