package victor.training.reactivespring.start;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class MixingExecutors {

   public static void main(String[] args) {

//      int i = ioRead();
//      int i2 = cpu(i);
//      ioWrite(i2);

      ExecutorService ioPool = Executors.newFixedThreadPool(1);
      ExecutorService cpuPool = Executors.newFixedThreadPool(1);

      CompletableFuture.supplyAsync(MixingExecutors::ioRead, ioPool)
          .thenApplyAsync(MixingExecutors::cpu, cpuPool)
          .thenAcceptAsync(MixingExecutors::ioWrite, ioPool);

      ThreadUtils.sleep(400); // threadurile din ForkJoinPool.commonPool sunt daemon. App se inchide daca au ramas doar astea in viata
   }

   public static int ioRead() {
      log.info("read");
      ThreadUtils.sleep(100);
      return 1;
   }
   public static int cpu(int i) {
      log.info("CPU");
      return i * 2;
   }
   public static void ioWrite(int i) {
      log.info("write " + i );
      ThreadUtils.sleep(100);
      log.info("Done");
   }
}
