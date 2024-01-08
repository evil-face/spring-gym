package epam.xstack.config;

import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SqsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsConfig.class);

    @Bean
    @Primary
    public AmazonSQSAsync amazonSQSAsync() {
        String region = new DefaultAwsRegionProviderChain().getRegion();
        LOGGER.info("Detected region: {}", region);

        return AmazonSQSAsyncClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate() {
        return new QueueMessagingTemplate(amazonSQSAsync());
    }
}
