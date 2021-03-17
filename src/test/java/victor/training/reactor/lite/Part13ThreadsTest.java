package victor.training.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactivespring.start.ThreadUtils;
import victor.training.reactor.lite.Part13Threads.BlockingService;
import victor.training.reactor.lite.Part13Threads.RxService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Slf4j
public class Part13ThreadsTest {

   private Part13Threads workshop = new Part13Threads();

   private String captureThread(String workType) {
      return workType + ":" + Thread.currentThread().getName();
   }
   @Test
   public void subscribe() {
      List<String> steps =new ArrayList<>();

      Supplier<String> readTask = () -> {
         steps.add(captureThread("READ"));
         return "text";
      };

      Mono<String> mono = workshop.subscribe(readTask);

      StepVerifier.create(mono)
          .expectNext("TEXT")
          .verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic");
   }


   @Test
   public void ioVsCpu() {
      List<String> steps =new ArrayList<>();

      Runnable ioTask = () -> steps.add(captureThread("IO"));
      Runnable cpuTask = () -> steps.add(captureThread("CPU"));

      Mono<Void> mono = workshop.ioVsCpu(ioTask, cpuTask);

      StepVerifier.create(mono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("IO:boundedElastic")
          .contains("CPU:parallel")
      ;
   }

   @Test
   public void ioVsCpuParallel() {
      List<String> steps =new ArrayList<>();

      Runnable ioTask = () -> {
         steps.add(captureThread("IO"));
         log.debug("Start IO");
         ThreadUtils.sleep(500);
         log.debug("End IO");
      };

      Runnable cpuTask = () -> {
         steps.add(captureThread("CPU"));
         log.debug("Start CPU");
         ThreadUtils.sleep(500);
         log.debug("End CPU");
      };

      long t0 = System.currentTimeMillis();
      Mono<Void> mono = workshop.ioVsCpuParallel(ioTask, cpuTask);

      StepVerifier.create(mono).verifyComplete();
      long t1 = System.currentTimeMillis();

      Assertions.assertThat(steps.toString())
          .contains("IO:boundedElastic")
          .contains("CPU:parallel")
      ;
      assertThat(t1-t0).isLessThan(900);
   }

   @Test
   public void threadHopping() {
      List<String> steps =new ArrayList<>();

      RxService service = Mockito.mock(RxService.class);
      when(service.readData()).thenAnswer(a -> Flux.defer(() -> {
         steps.add(captureThread("READ"));
         return Flux.just("1","2","3");
      }));
      when(service.cpuTask(anyString())).thenAnswer(s -> Mono.defer(() -> {
         steps.add(captureThread("CPU"));
         return Mono.just(parseInt(s.getArgument(0)));
      }));
      when(service.writeData(anyInt())).thenAnswer(s -> Mono.defer(() -> {
         steps.add(captureThread("WRITE"));
         return Mono.empty();
      }));
      Mono<?> mono = workshop.threadHopping(service);

      StepVerifier.create(mono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }

   @Test
   public void threadHoppingNonMonoApi() {
      List<String> steps =new ArrayList<>();

      BlockingService service = Mockito.mock(BlockingService.class);
      when(service.readData()).thenAnswer(a -> {
         steps.add(captureThread("READ"));
         return "1";
      });
      when(service.cpuTask(anyString())).thenAnswer(s -> {
         steps.add(captureThread("CPU"));
         return parseInt(s.getArgument(0));
      });
      doAnswer(s -> {
         steps.add(captureThread("WRITE"));
         return null;
      }).when(service).writeData(1);

      Mono<?> mono = workshop.threadHoppingNonMonoApi(service);

      StepVerifier.create(mono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }

   @Test
   public void threadHoppingHard() {
      List<String> steps =new ArrayList<>();

      Supplier<String> readTask = () -> {
          steps.add(captureThread("READ"));
          return "1";
      };
      Function<String, Mono<Integer>> cpuTask = s -> {
          steps.add(captureThread("CPU"));
          return Mono.just(parseInt(s));
      };
      Runnable writeTask = () -> steps.add(captureThread("WRITE"));

      Mono<String> sourceMono = Mono.fromSupplier(readTask); // READ

      Mono<Integer> mono = workshop.threadHoppingHard(sourceMono, cpuTask); // prod

      Mono<Void> finalMono = mono.then(Mono.fromRunnable(writeTask)); // WRITE

      StepVerifier.create(finalMono).verifyComplete();

      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }

}
