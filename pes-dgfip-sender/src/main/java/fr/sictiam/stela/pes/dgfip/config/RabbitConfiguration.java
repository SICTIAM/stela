package fr.sictiam.stela.pes.dgfip.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.hostname}")
    private String hostname;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.application.exchange}")
    private String exchangeName;
    @Value("${spring.application.queuePes}")
    private String queuePes;
    @Value("${spring.application.queuePesSend}")
    private String queuePesSend;
    @Value("${spring.application.queuePesAr}")
    private String queuePesAr;

    @Bean
    Queue queuePesAr() {
        return new Queue(queuePesAr, true);
    }
    @Bean
    Queue queuePes() {
        return new Queue(queuePes, true);
    }
    @Bean
    Queue queuePesSend() {
        return new Queue(queuePesSend, true);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("pesAr.exchange", true, false);
    }
    @Bean
    TopicExchange topicExchangeSend() {
        return new TopicExchange("pesSend.exchange", true, false);
    }
    @Bean
    TopicExchange topicExchangePes() {
        return new TopicExchange("pes.exchange", true, false);
    }

    @Bean
    Binding bindingPes() {
        return new Binding("pes.queue", Binding.DestinationType.QUEUE, "pes.exchange", "pes.created", null);
    }
    @Bean
    Binding binding() {
        return new Binding("pesAr.queue", Binding.DestinationType.QUEUE, "pesAr.exchange", "#", null);
    }
    @Bean
    Binding bindingSend() {
        return new Binding("pesSend.queue", Binding.DestinationType.QUEUE, "pesSend.exchange", "#", null);
    }

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(hostname);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    @Bean
    @Required
    RabbitAdmin rabbitAdmin() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
        admin.setAutoStartup(true);
        admin.declareExchange(topicExchange());
        admin.declareExchange(topicExchangeSend());
        admin.declareExchange(topicExchangePes());
        admin.declareQueue(queuePes());
        admin.declareQueue(queuePesSend());
        admin.declareQueue(queuePesAr());
        admin.declareBinding(binding());
        admin.declareBinding(bindingPes());
        admin.declareBinding(bindingSend());
        return admin;
    }
}
