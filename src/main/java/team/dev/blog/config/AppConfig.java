package team.dev.blog.config;

import com.formdev.flatlaf.FlatLightLaf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig implements InitializingBean {

    @Bean
    public ConcurrentLinkedQueue<String> concurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    public void afterPropertiesSet() {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            log.error("Failed to initialize LaF");
        }
    }
}
