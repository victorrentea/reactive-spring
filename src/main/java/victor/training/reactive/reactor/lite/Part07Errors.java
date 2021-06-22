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

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.domain.User;

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

//========================================================================================
   // TODO retrieve all Products with retrieveProduct. In case any fails, return an empty list.
   public Mono<List<Order>> catchReturnDefault(List<Integer> ids) {
      try {
         List<Order> orders = new ArrayList<>();
         for (Integer id : ids) {
            orders.add(retrieveOrder(id).block());// TODO REMOVE blocking
         }
         return Mono.just(orders);
      } catch (Exception e) {
         return Mono.just(emptyList());
      }
   }

   //========================================================================================
   // TODO return those items that were retrieve successfully
   public Mono<List<Order>> catchReturnBestEffort(List<Integer> ids) {
      List<Order> orders = new ArrayList<>();
      for (Integer id : ids) {
         try {
            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
            orders.add(order);
         } catch (Exception e) {
         }
      }
      return Mono.just(orders);
   }

   //========================================================================================
   // TODO return the items that were retrieve, never request further items (==> no async prefetch)
   public Mono<List<Order>> catchAndStop(List<Integer> ids) {
      List<Order> orders = new ArrayList<>();
      try {
         for (Integer id : ids) {
            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
            orders.add(order);
         }
      } catch (Exception e) {
      }
      return Mono.just(orders);
   }
   //========================================================================================
   // TODO fail at first error, rethrowing the exception wrapped in a CustomException
   public Mono<List<Order>> catchRethrow(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(this::retrieveOrder)
          .collectList()
          .onErrorMap(e -> new CustomException(e));
//      try {
//         List<Order> orders = new ArrayList<>();
//         for (Integer id : ids) {
//            Order order = retrieveOrder(id);
//            orders.add(order);
//         }
//         return orders;
//      } catch (Exception e) {
//         throw new CustomException(e);
//      }
//      return null;
   }
   public static class CustomException extends RuntimeException {
      public CustomException(Throwable cause) {
         super(cause);
      }
   }

   private Mono<Order> retrieveOrder(int id) { // imagine a network call
      if (id < 0) {
         return Mono.error(new RuntimeException());
      } else {
         return Mono.just(new Order(id));
      }
   }

   public static class Order {
      private final Integer id;

      public Order(Integer id) {
         this.id = id;
      }

      public Integer getId() {
         return id;
      }

      @Override
      public String toString() {
         return "Order{" +
                "id=" + id +
                '}';
      }
   }

   protected final class GetOutOfHereException extends Exception {
      private static final long serialVersionUID = 0L;
   }


}
