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

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;
import victor.training.reactive.reactor.lite.domain.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;

/**
 * Learn how to deal with errors.
 *
 * @author Sebastien Deleuze
 * @see Exceptions#propagate(Throwable)
 */
public class Part07Errors {
   private static final Logger log = LoggerFactory.getLogger(Part07Errors.class);
   // imagine a network call
   private static Scheduler rmiScheduler = Schedulers.newBoundedElastic(4, 500, "RMI-");
   //========================================================================================

   // TODO Return a Mono<User> containing User.SAUL when an error occurs in the input Mono, else do not change the input Mono.
   Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
      return mono.onErrorReturn(User.SAUL);
   }

//========================================================================================

   // TODO Return a Flux<User> containing User.SAUL and User.JESSE when an error occurs in the input Flux, else do not change the input Flux.
   Flux<User> betterCallSaulAndJesseForBogusFlux(Flux<User> flux) {
      return flux.onErrorResume(t -> Flux.just(User.SAUL, User.JESSE));
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
   // TODO retrieve all Orders with retrieveOrder. In case any fails, return an empty list.
   public Mono<List<Order>> catchReturnDefault(List<Integer> ids) {

      return Flux.fromIterable(ids)
          .flatMap(this::retrieveOrder)
          .collectList()
          .onErrorResume(t -> Mono.just(emptyList())); // bad practice ?
//          .onErrorResume(t -> Mono.empty());
//          .flatMapMany(Flux::fromIterable); //strange
   }

   //========================================================================================
   // TODO return those items that were retrieve successfully
   public Mono<List<Order>> catchReturnBestEffort(List<Integer> ids) {

      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id).onErrorResume(t -> Mono.empty()))
          .collectList()
          ;
//      List<Order> orders = new ArrayList<>();
//      for (Integer id : ids) {
//         try {
//            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
//            orders.add(order);
//         } catch (Exception e) {
//         }
//      }
//      return Mono.just(orders);
   }

   //========================================================================================
   // TODO return the items that were retrieved, never request further items after a failure (==> no async prefetch)
   public Mono<List<Order>> catchAndStop(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id)) // scrambles order
//          .flatMapSequential(id -> retrieveOrder(id)) // eats memory
//          .concatMap(id -> retrieveOrder(id)) // slow
          .onErrorResume(t -> Mono.empty()) // replaces the error singlal with a completion signal
          .collectList()
          ;
//      List<Order> orders = new ArrayList<>();
//      try {
//         for (Integer id : ids) {
//            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
//            orders.add(order);
//         }
//      } catch (Exception e) {
//      }
//      return Mono.just(orders);
   }

   //========================================================================================
   // TODO fail at first error, rethrowing the exception wrapped in a CustomException
   public Mono<List<Order>> catchRethrow(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id))
          .collectList()
          .onErrorMap(originalException -> new CustomException(originalException)) // returns a Mono.error(CustomException(originalexcept)
          ;
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

   //========================================================================================
   // TODO fail at any error, log the error and rethrow it
   public Mono<List<Order>> logRethrow(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id))
          .collectList()
         .doOnError(e ->   log.error("BOOM",e));


//      try {
//         List<Order> orders = new ArrayList<>();
//         for (Integer id : ids) {
//            Order order = retrieveOrder(id).block();
//            orders.add(order);
//         }
//         return Mono.just(orders);
//      } catch (Exception e) {
//         log.error("BOOM", e);
//         throw e;
//      }
   }

   //========================================================================================
   // TODO for any item, if an error occurs fall back by calling another storage (blocking) - "retrieveOrderBackup"
   public Mono<List<Order>> recover(List<Integer> ids) {

      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id) .onErrorResume(e -> retrieveOrderBackup(id))     )

          .publishOn(Schedulers.parallel())
          .flatMap(o -> {
             log.info("Stuff");
             return Mono.just(o);
          })

          .collectList()
          ;

//      List<Order> orders = new ArrayList<>();
//      for (Integer id : ids) {
//         Order order;
//         try {
//            order = retrieveOrder(id).block();
//         } catch (Exception e) {
//            order = retrieveOrderBackup(id).block();
//         }
//
//         orders.add(order);
//      }
//      return Mono.just(orders);
   }

   //========================================================================================
   // TODO  close the writer at the end signal (error or completion)
   // TODO [pro] what if the subscriber retries? Tip: Mono.fromSupplier
   public Mono<Void> tryFinally(List<Integer> ids) throws IOException {
      return Mono.defer(() -> {
         FileWriter writer;
         try {
            writer = new FileWriter("a.txt");
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
         log.info("Creating file");

         return Flux.fromIterable(ids)
             .flatMap(this::retrieveOrder)
             .map(Order::toString)
             .doOnNext(Unchecked.consumer(writer::write))
             .doOnTerminate(() -> {
                log.info("Closing file");
                try {
                   writer.close();
                } catch (IOException e) {
                   e.printStackTrace();
                }
             })
             .then();
      });
//      try(FileWriter writer = new FileWriter("a.txt")) {
//         for (Integer id : ids) {
//            Order order = retrieveOrder(id).block();
//            writer.write(order.toString());
//         }
//      }
//      return Mono.empty();
   }


   public static class CustomException extends RuntimeException {
      public CustomException(Throwable cause) {
         super(cause);
      }
   }

   private Mono<Order> retrieveOrder(int id) {
//      return Mono.defer(() -> {
      return Mono.fromSupplier(() -> {
         log.info("RETRIEVE ORDER " + id);
         ThreadUtils.sleep(10 + new Random().nextInt(100));// blocking call you can't really make unblokcing. (reactive) ~ JDBC, Spring Remoting, CORBA, EJB, RMI,
         if (id < 0) {
//            if (Math.random() > .5)
            throw new RuntimeException();
         }
         return new Order(id);
      })
          .subscribeOn(rmiScheduler)
          .publishOn(Schedulers.boundedElastic());
   }
   private Mono<Order> retrieveOrderBackup(int id) { // imagine a local fast storage (~cache)
      return Mono.fromSupplier(() -> {
         log.info("Calling backup CORBA");
         ThreadUtils.sleep(1000);
         return new Order(id).backup();
      }).subscribeOn(Schedulers.boundedElastic());
   }

   public static class Order {
      private final Integer id;
      private boolean backup; // mutable data, God help us all

      public Order(Integer id) {
         this.id = id;
      }

      public Integer getId() {
         return id;
      }

      public Order backup() {
         this.backup = true;
         return this;
      }

      public boolean isBackup() {
         return backup;
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
