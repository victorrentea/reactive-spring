package victor.training.reactor.lite;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Part12AdvancedTest {

   private Part12Advanced workshop = new Part12Advanced();

   @Test
   public void defer() {
      Flux<Integer> flux = workshop.defer();
      List<Integer> sequence1 = flux.collectList().block();
      List<Integer> sequence2 = flux.collectList().block();
      assertThat(sequence1).isNotEqualTo(sequence2);
   }


   @Test
   public void hotPublisher() throws InterruptedException {
      assertThat(workshop.hotPublisher().blockFirst()).isLessThan(2);

      Flux<Long> hot = workshop.hotPublisher();
      Thread.sleep(400);
      assertThat(hot.blockFirst()).isGreaterThan(3);
   }
   
   @Test
   public void replay() {
      String reactiveManifesto = "Large systems are composed of smaller ones and therefore " +
                                 "depend on the Reactive properties of their constituents. [...] ";
      Flux<String> timedFlux = workshop.replay(Flux.interval(Duration.ofMillis(100)), reactiveManifesto);

      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");

      ThreadUtils.sleep(600);
      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");
   }
   
   @Test
   public void reactorContext() {
      Mono<String> withContext = workshop.reactorContext()
          .contextWrite(context -> context.put("username", "John"));

      Duration duration = StepVerifier.create(withContext)
          .expectNext("Hello John")
          .verifyComplete();
      assertThat(duration).isGreaterThan(Duration.ofMillis(900));
   }

   private String captureThread(String workType) {
      return workType + ":" + Thread.currentThread().getName();
   }
   @Test
   public void threadHopping() {
      List<String> steps =new ArrayList<>();

      Runnable readTask = () -> steps.add(captureThread("READ"));
      Runnable cpuTask = () -> steps.add(captureThread("CPU"));
      Runnable writeTask = () -> steps.add(captureThread("WRITE"));

      Mono<Void> mono = workshop.threadHopping(readTask, cpuTask, writeTask);

      StepVerifier.create(mono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }

   @Test
   public void threadHoppingHard() {
      List<String> steps =new ArrayList<>();

      Runnable readTask = () -> steps.add(captureThread("READ"));
      Runnable cpuTask = () -> steps.add(captureThread("CPU"));
      Runnable writeTask = () -> steps.add(captureThread("WRITE"));

      Mono<Void> sourceMono = Mono.fromRunnable(readTask);
      Mono<Void> mono = workshop.threadHoppingHard(sourceMono, cpuTask);
      Mono<Void> finalMono = mono.then(Mono.fromRunnable(writeTask));

      StepVerifier.create(finalMono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }

}
