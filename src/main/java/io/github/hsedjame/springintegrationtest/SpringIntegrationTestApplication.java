package io.github.hsedjame.springintegrationtest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.Expression;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@SpringBootApplication
@RestController
public class SpringIntegrationTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationTestApplication.class, args);
    }

    @Bean
    Publisher<Message<String>> httpReactiveSource() {
        return IntegrationFlows
                .from(
                        Http.inboundChannelAdapter("/message/{id}")
                            .requestMapping(r -> r.methods(HttpMethod.POST))
                            .payloadExpression("#pathVariables.id")

                )
                .channel(MessageChannels.direct())
                .toReactivePublisher();
   }

   @GetMapping(value = "/events", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
   public Flux<Event> eventMessages() {
        return Flux.interval(Duration.of(2, ChronoUnit.SECONDS))
                .map(String::valueOf)
                .map(this::getEvent);
   }

    @GetMapping(value = "/sources", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Event> eventMessagesFromSource() {
        return Flux.from(httpReactiveSource())
                .map(Message::getPayload)
                .map(this::getEvent);
    }


   @Data
   @AllArgsConstructor
    public class Event {
        private String msg;
        private LocalDate date;
    }

    public Event getEvent(String msg) {
        return new Event(msg, LocalDate.now());
    }

}

