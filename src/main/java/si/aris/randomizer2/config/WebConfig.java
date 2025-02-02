package si.aris.randomizer2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Omogoči CORS za vse poti
                        .allowedOrigins("http://localhost:5173") // Frontend naslov
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); // Dovoli HTTP metode
            }
        };
    }
}

