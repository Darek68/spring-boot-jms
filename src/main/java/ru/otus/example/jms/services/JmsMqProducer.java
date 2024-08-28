package ru.otus.example.jms.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ObjectMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.otus.example.jms.config.ActiveMqConfig;
import ru.otus.example.jms.config.JsmConst;
import ru.otus.example.jms.config.RabbitMqConfig;
import ru.otus.example.jms.dto.OrderDto;
import ru.otus.example.jms.dto.StudentDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class JmsMqProducer {

    private static final int TEXT = 0;
    private static final int SERIALIZABLE = 1;
    private static final int MESSAGE_CREATOR = 2;

    private final JmsTemplate rabbitMqTemplate;
    private final JmsTemplate activeMqTemplate;
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

    public JmsMqProducer(@Qualifier(RabbitMqConfig.JMS_TEMPLATE) JmsTemplate rabbitMqTemplate,
                         @Qualifier(ActiveMqConfig.JMS_TEMPLATE) JmsTemplate activeMqTemplate,
                         ObjectMapper objectMapper) {
        this.rabbitMqTemplate = rabbitMqTemplate;
        this.activeMqTemplate = activeMqTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 5000)
    public void convertAndSend() {
        int rnd = random.nextInt(3);

        if (rnd == TEXT)
            convertAndSend(createTextMessage());
        else if (rnd == SERIALIZABLE)
            convertAndSend(createSerializableMessage());
        else    // MESSAGE_CREATOR
            send(createMessageCreatorMessage());
    }

    private void convertAndSend(String message) {
        rabbitMqTemplate.convertAndSend(RabbitMqConfig.DESTINATION_NAME, message);
        activeMqTemplate.convertAndSend(ActiveMqConfig.DESTINATION_NAME, message);
    }

    private void convertAndSend(Serializable message) {
        rabbitMqTemplate.convertAndSend(RabbitMqConfig.DESTINATION_NAME, message);
        activeMqTemplate.convertAndSend(ActiveMqConfig.DESTINATION_NAME, message);
    }

    private void send(MessageCreator messageCreator) {
        rabbitMqTemplate.send(RabbitMqConfig.DESTINATION_NAME, messageCreator);
        activeMqTemplate.send(ActiveMqConfig.DESTINATION_NAME, messageCreator);
    }

    private String createTextMessage() {
        int id = random.nextInt(10);
        return "text-message:" + id;
    }

    private Serializable createSerializableMessage() {
        int id = random.nextInt(10);
        String firstName = "oleg_" + id;
        String lastName = "pavlov_" + id;

        return StudentDto.builder()
                         .firstName(firstName)
                         .lastName(lastName)
                         .email(firstName + '.' + lastName + "@yandex.ru")
                         .counts(List.of(1, 2, 3))
                         .dateOfBirth(LocalDate.now())
                         .build();
    }

    private MessageCreator createMessageCreatorMessage() {
        int id = random.nextInt(10);
        OrderDto order = OrderDto.builder()
                                 .title("auto_" + id)
                                 .value(id * id)
                                 .build();

        return session -> {
            try {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setStringProperty(JsmConst.CLASS_NAME, OrderDto.class.getName());
                objectMessage.setObject(objectMapper.writeValueAsString(order));
                return objectMessage;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
