package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import victor.training.reactive.intro.ThreadUtils;

import java.util.Scanner;

import static victor.training.reactive.intro.ThreadUtils.sleep;
import static victor.training.reactive.intro.ThreadUtils.waitForEnter;

@Slf4j
public class MixingExecutors {
   public static void main(String[] args) {

      // TODO call ioRead and ioWrite on boundedElastic scheduler
      // TODO call cpu on parallel scheduler

      // threads from ForkJoinPool.commonPool are daemon threads. the process dies if main exits
      waitForEnter();
   }

   public static int ioRead() {
      log.info("read");
      sleep(100);
      return 1;
   }

   public static int cpu(int i) {
      log.info("CPU");
      return i * 2;
   }

   public static void ioWrite(int i) {
      log.info("write " + i);
      sleep(100);
      log.info("Done");
   }
}
