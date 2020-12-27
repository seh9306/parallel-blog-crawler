package team.dev.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
public class AppConfig {

    @Bean
    public ConcurrentLinkedQueue<String> concurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

}
