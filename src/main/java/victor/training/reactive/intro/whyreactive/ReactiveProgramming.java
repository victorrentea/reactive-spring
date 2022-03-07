package victor.training.reactive.intro.whyreactive;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveProgramming {
   @Test
   public void test() {
//      Mono<>
//      CompletableFuture
      Flux<String > ids = fetchIds();

      Flux<String> combinations =
          ids.flatMap(id -> {
             Mono<String> nameTask = fetchName(id);
             Mono<Integer> statTask = fetchStat(id);

             return nameTask.zipWith(statTask,
                 (name, stat) -> "Name " + name + " has stats " + stat);
          });

      Mono<List<String>> result = combinations.collectList();

      List<String> results = result.block();
      assertThat(results).containsExactly(
          "Name NameJoe has stats 103",
          "Name NameBart has stats 104",
          "Name NameHenry has stats 105",
          "Name NameNicole has stats 106",
          "Name NameABSLAJNFOAJNFOANFANSF has stats 121"
      );
   }

   private Flux<String> fetchIds() {
      return Flux.fromIterable(Data.IDS);
   }

   private Mono<String> fetchName(String id) {
      return Mono.just(Data.NAMES.get(id));
   }

   private Mono<Integer> fetchStat(String id) {
      return Mono.just(Data.STATS.get(id));
   }
}
