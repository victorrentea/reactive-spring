package victor.training.reactive.intro.whyreactive;

import org.junit.jupiter.api.Test;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletableFutures {

   public static CompletableFuture<List<String>> fetchIds() {
      // pretend time passes
      ThreadUtils.sleep(10);
      return CompletableFuture.completedFuture(Data.IDS);
   }

   public static CompletableFuture<Integer> fetchStat(String id) {
      // pretend time passes
      ThreadUtils.sleep(10);
      return CompletableFuture.completedFuture(Data.STATS.get(id));
   }

   public static CompletableFuture<String> fetchName(String id) {
      // pretend time passes
      ThreadUtils.sleep(10);
      return CompletableFuture.completedFuture(Data.NAMES.get(id));
   }

   @Test
   public void test() {
      CompletableFuture<List<String>> ids = fetchIds();

      CompletableFuture<List<String>> result = ids.thenComposeAsync(l -> {
         Stream<CompletableFuture<String>> zip =
             l.stream().map(i -> {
                CompletableFuture<String> nameTask = fetchName(i);
                CompletableFuture<Integer> statTask = fetchStat(i);

                return nameTask.thenCombineAsync(statTask, (name, stat) -> "Name " + name + " has stats " + stat);
             });
         List<CompletableFuture<String>> combinationList = zip.collect(Collectors.toList());
         CompletableFuture<String>[] combinationArray = combinationList.toArray(new CompletableFuture[combinationList.size()]);

         CompletableFuture<Void> allDone = CompletableFuture.allOf(combinationArray);
         return allDone.thenApply(v -> combinationList.stream()
               .map(CompletableFuture::join)
               .collect(Collectors.toList()));
      });

      List<String> results = result.join();
      assertThat(results).contains(
          "Name NameJoe has stats 103",
          "Name NameBart has stats 104",
          "Name NameHenry has stats 105",
          "Name NameNicole has stats 106",
          "Name NameABSLAJNFOAJNFOANFANSF has stats 121");
   }

}
