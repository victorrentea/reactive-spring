package victor.training.reactor.lite;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactivespring.start.ThreadUtils;
import victor.training.reactor.lite.domain.User;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Learn how to create Mono instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 */
public class Part02Mono {

//========================================================================================

   // TODO Return an empty Mono
   Mono<String> emptyMono() {
//		return Mono.empty();
      return Mono.create(sink -> {
         sink.success();
      });
   }

//========================================================================================

   // TODO Return a Mono that never emits any signal
   Mono<Void> monoWithNoSignal() {
//		return Mono.never();
      return Mono.create(sink -> {
//			driver.registerCallback(dataFromHW -> sink.success(dataFromHW));

      });

   }

//========================================================================================

   // TODO Return a Mono that contains a "foo" value
   Mono<String> fooMono() {
//		return Mono.just("foo");
      return Mono.create(sink -> {
         sink.success("foo");
      });
   }

//========================================================================================

   // TODO Create a Mono that emits an IllegalStateException
   Mono<String> errorMono() {
      return Mono.error(new IllegalStateException());
   }


   public static void main(String[] args) {
      Mono<List<Integer>> map = Mono.just(asList("a", "b", "c"))
          .map(list -> list.stream().map(String::toUpperCase).collect(toList()))
          .map(list -> list.stream().map(String::toUpperCase).collect(toList()))
//			.flatMapMany()
          .map(list -> list.stream().map(id -> callExSystem(id)).collect(toList()));

      Mono<List<String>> monoIds = Flux.just("a", "b", "c")
          .collectList();

      Flux.just("a", "b", "c")
          .flatMap(Part02Mono::cpuStuffReturningMono_bad)
          .collectList();

      Flux.just(1L, 2L)
          .collectList()
          .flatMapMany(ids -> findByIdsFlux(ids))
          .buffer(100)
          .flatMap(Part02Mono::enhancePaged);

      Flux.just(1L, 2L)
          .flatMap(ids -> findByIdsFlux(Collections.singletonList(ids)));

   }


   public static Mono<List<User>> findByIdsMono(List<Long> idsPage) {
      return Mono.just(Collections.emptyList());
   }

   public static Flux<User> findByIdsFlux(List<Long> idsPage) {
      return Flux.empty();
   }

   public static Mono<List<UserEnhanced>> enhancePaged(List<User> user) { //2nd data source
      return Mono.empty();
   }

   public static Mono<UserEnhanced> enhance(User user) { //2nd data source
      return Mono.empty();
   }

   private static Mono<String> cpuStuffReturningMono_bad(String s) {
      return Mono.just(s.toUpperCase());
   }


   private static Integer callExSystem(String id) {
      ThreadUtils.sleep(1000);
      return 1;
   }

}
