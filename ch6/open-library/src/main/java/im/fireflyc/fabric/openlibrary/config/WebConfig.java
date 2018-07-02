package im.fireflyc.fabric.openlibrary.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan(basePackages = "im.fireflyc.blockchainlibrary.web")
public class WebConfig implements WebMvcConfigurer {
    @Bean
    LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
