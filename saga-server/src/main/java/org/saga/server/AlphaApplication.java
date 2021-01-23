

package org.saga.server;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlphaApplication {
  public static void main(String[] args) {
    SpringApplication.run(AlphaApplication.class, args);
  }

  @PreDestroy
  void shutdown() {
    // do nothing here
  }
}
