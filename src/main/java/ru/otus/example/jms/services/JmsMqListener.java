package ru.otus.example.jms.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import ru.otus.example.jms.config.ActiveMqConfig;
import ru.otus.example.jms.config.JsmConst;
import ru.otus.example.jms.config.RabbitMqConfig;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class JmsMqListener {

    private static final String ID_RABBIT_MQ = "rabbit-mq";
    private static final String ID_ACTIVE_MQ = "active-mq";

    private final ObjectMapper objectMapper;

    @JmsListener(destination = RabbitMqConfig.DESTINATION_NAME,
            containerFactory = RabbitMqConfig.JMS_LISTENER_CONTAINER_FACTORY)
    public void onRabbitMqMessage(Message message) {
        onMessage(ID_RABBIT_MQ, message);
    }

    @JmsListener(destination = ActiveMqConfig.DESTINATION_NAME,
            containerFactory = ActiveMqConfig.JMS_LISTENER_CONTAINER_FACTORY)
    public void onActiveMqMessage(Message message) {
        onMessage(ID_ACTIVE_MQ, message);
    }

    private void onMessage(String id, Message message) {
        try {
            if (message instanceof TextMessage)
                onTextMessage(id, (TextMessage) message);
            else if (message instanceof ObjectMessage)
                onObjectMessage(id, (ObjectMessage) message);
            else
                throw new IllegalArgumentException("Message Error");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void onTextMessage(String id, TextMessage message) throws JMSException {
        String msg = message.getText();
        System.out.format("[%s: text] : %s\n", id, msg);
    }

    private void onObjectMessage(String id, ObjectMessage message)
            throws JMSException, ClassNotFoundException, JsonProcessingException {
        String className = message.getStringProperty(JsmConst.CLASS_NAME);

        if (className == null)
            onSerializableObjectMessage(id, message);
        else
            onCustomObjectMessage(id, Class.forName(className), message);
    }

    private static void onSerializableObjectMessage(String id, ObjectMessage message) throws JMSException {
        Serializable obj = message.getObject();
        System.out.format("[%s: serializable] : %s\n", id, obj);
    }

    private void onCustomObjectMessage(String id, Class<?> cls, ObjectMessage message)
            throws JMSException, JsonProcessingException {
        String json = String.valueOf(message.getObject());
        Object obj = objectMapper.readValue(json, cls);
        System.out.format("[%s: %s] : %s\n", id, cls.getSimpleName(), obj);
    }
}
