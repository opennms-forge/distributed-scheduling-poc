package org.opennms.poc.testdriver.config;

import org.opennms.poc.testdriver.view.ViewRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViewConfig {

  @Bean
  ViewRegistry viewRegistry() {
    return new ViewRegistry();
  }

}
