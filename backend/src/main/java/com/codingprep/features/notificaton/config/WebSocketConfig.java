package com.codingprep.features.notificaton.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    //Spring boot manages websocket connections and acts as broker, a message broker is being setup here 
    // the frontend client will subscribe to us, the message broker here , since spring lets us setup 2 simple broker registry.enableSimpleBroker("/topic", "/queue")
    //
    // Frontend typescript stomp subscription to 1 event : 
        //public subscribeToCountdown(matchId: string, onEvent: (event: any) => void): StompSubscription {
        //    return this.subscribe(`/topic/match/${matchId}/countdown`, onEvent);
        //}

    // After indiciating the subscription via stompService.subscribeToCountdown("10", (event: any) => { } )
    

    // What publishes it? At 
    // IN ADDITION TO THIS, we are subscribed to redis topics  , which is used for another functionality, but 
    // and is subcribed to redis topics, then forward it to the users over their websocket connection
    //
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // Message broker is being setup here, a broker is one where they help exchange messages  between 2 endpoints, ensure message go from producer to consumers in decoupled way 
        registry.setApplicationDestinationPrefixes("/app"); // prefix used by Client to send message to server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                 .setAllowedOriginPatterns("*")
                 .withSockJS(); //The frontend will use this url path to connect and also manage messages over sockjs localhost:8080/ws
    }

}
