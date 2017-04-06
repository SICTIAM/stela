package com.sictiam.flux_pes.axon;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.TrackingEventStream;
import org.axonframework.eventsourcing.eventstore.TrackingToken;
import org.axonframework.messaging.MessageDispatchInterceptor;
//import org.axonframework.spring.config.EnableAxon;
import org.axonframework.spring.config.annotation.AnnotationCommandHandlerBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by s.vergon on 29/03/2017.
 */
@Configuration
//@EnableAxon
@ConditionalOnClass
public class AxonConfig {
    @Bean
    @ConditionalOnMissingBean
    CommandBus commandBus() {
        CommandBus bus = new SimpleCommandBus();
        return bus;
    }

    @Bean
    @ConditionalOnMissingBean
    CommandGateway commandGateway() {
        CommandGateway gateway = new DefaultCommandGateway(commandBus());
        return gateway;
    }

    @Bean
    @ConditionalOnMissingBean
    EventBus eventBus() {
        EventBus bus =  new SimpleEventBus();
        return bus;
    }
    @Bean
    @ConditionalOnMissingBean
    EventStore eventStore() {

        EventStore store =  new EventStore() {
            @Override
            public DomainEventStream readEvents(String aggregateIdentifier) {
                return null;
            }

            @Override
            public void storeSnapshot(DomainEventMessage<?> snapshot) {

            }

            @Override
            public TrackingEventStream openStream(TrackingToken trackingToken) {
                return null;
            }

            @Override
            public void publish(List<? extends EventMessage<?>> events) {

            }

            @Override
            public Registration registerDispatchInterceptor(MessageDispatchInterceptor<EventMessage<?>> dispatchInterceptor) {
                return null;
            }

            @Override
            public Registration subscribe(Consumer<List<? extends EventMessage<?>>> messageProcessor) {
                return null;
            }
        };
        return store;
    }

    @Bean
    AnnotationCommandHandlerBeanPostProcessor annotationCommandHandlerBeanPostProcessor() {
        AnnotationCommandHandlerBeanPostProcessor p =  new AnnotationCommandHandlerBeanPostProcessor();
        //p.commandBus = commandBus()
        return p;
    }

}