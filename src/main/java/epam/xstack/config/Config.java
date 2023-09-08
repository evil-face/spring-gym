package epam.xstack.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("epam.xstack")
@PropertySource("classpath:application.properties")
public class Config {

}
