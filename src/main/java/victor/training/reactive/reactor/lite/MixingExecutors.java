package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import victor.training.reactive.intro.ThreadUtils;

@Slf4j
public class MixingExecutors {
   public static void main(String[] args) {

      // TODO run ioRead and ioWrite on boundedElastic scheduler
      // TODO run cpu on parallel scheduler

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
