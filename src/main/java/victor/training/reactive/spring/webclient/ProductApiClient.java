package victor.training.reactive.spring.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class ProductApiClient implements CommandLineRunner {
//   private final RestTemplate rest;
//
//   @Override
//   public void run(String... args) throws Exception {
//      String url = "http://localhost:9999/api/product/1";
//      log.info("Sending request to {}...", url);
////      String string = rest.getForObject(url, String.class);
////      log.info("Got response: " + string);
//
//      WebClient.create().get().uri(url).retrieve().bodyToMono(String.class)
//          .subscribe(s -> log.info("Got response: " + s));
//   }
//}
