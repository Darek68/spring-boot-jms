package ru.otus.example.jms.config;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RabbitMqConfig {

    public static final String JMS_TEMPLATE = "rabbitMqJmsTemplate";
    public static final String JMS_LISTENER_CONTAINER_FACTORY = "rabbitMqJmsListenerContainerFactory";

    public static final String DESTINATION_NAME = "foo";

    @Bean
    public ConnectionFactory connectionFactory() {
        RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    @Bean(JMS_TEMPLATE)
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(10));
        return jmsTemplate;
    }

    @Bean(JMS_LISTENER_CONTAINER_FACTORY)
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory());
        return factory;
    }

}
