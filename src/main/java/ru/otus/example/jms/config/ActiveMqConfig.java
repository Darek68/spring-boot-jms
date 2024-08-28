package ru.otus.example.jms.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class ActiveMqConfig {
    public static final String JMS_TEMPLATE = "activeMqJmsTemplate";
    public static final String JMS_LISTENER_CONTAINER_FACTORY = "activeMqJmsListenerContainerFactory";

    private static final String CONNECTION_FACTORY = "activeMqConnectionFactory";

    public static final String DESTINATION_NAME = "foo";
    public static final String CLASS_NAME = "className";

    @Bean(CONNECTION_FACTORY)
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("tcp://localhost:61616");
        connectionFactory.setPassword("admin");
        connectionFactory.setUserName("password");
        return connectionFactory;
    }

    @Bean(JMS_TEMPLATE)
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(10));
        return jmsTemplate;
    }

    @Bean(JMS_LISTENER_CONTAINER_FACTORY)
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("1-1");
        return factory;
    }

}
