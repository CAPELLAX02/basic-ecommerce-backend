package com.capellax.ecommerce.api.security;

import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration
        implements WebSocketMessageBrokerConfigurer {

    private final ApplicationContext context;
    private final JWTRequestFilter jwtRequestFilter;
    private final UserService userService;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("**")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    private AuthorizationManager<Message<?>> makeMessageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
                new MessageMatcherDelegatingAuthorizationManager.Builder();

        return messages
                .simpDestMatchers("/topic/users/**")
                .authenticated()
                .anyMessage()
                .permitAll()
                .build();
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        AuthorizationManager<Message<?>> authorizationManager =
                makeMessageAuthorizationManager();

        AuthorizationChannelInterceptor authInterceptor =
                new AuthorizationChannelInterceptor(authorizationManager);

        AuthorizationEventPublisher publisher =
                new SpringAuthorizationEventPublisher(context);

        authInterceptor.setAuthorizationEventPublisher(publisher);

        registration.interceptors(authInterceptor);
    }

    /**
     * Interceptor for rejecting client messages on specific channels.
     */
    private class RejectClientMessagesOnChannelsChannelInterceptor
            implements ChannelInterceptor {

        /** Paths that do not allow client messages. */
        private String[] paths = new String[] {
                "/topic/user/*/address"
        };

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.MESSAGE)) {
                String destination = (String) message.getHeaders().get(
                        "simpDestination");
                for (String path: paths) {
                    if (MATCHER.match(path, destination))
                        message = null;
                }
            }
            return message;
        }

    }

    /**
     * Interceptor to apply authorization and permissions onto specific
     * channels and path variables.
     */
    private class DestinationLevelAuthorizationChannelInterceptor
            implements ChannelInterceptor {

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.SUBSCRIBE)) {
                String destination = (String) message.getHeaders().get(
                        "simpDestination");
                String userTopicMatcher = "/topic/user/{userId}/**";
                if (MATCHER.match(userTopicMatcher, destination)) {
                    Map<String, String> params = MATCHER.extractUriTemplateVariables(
                            userTopicMatcher, destination);
                    try {
                        Long userId = Long.valueOf(params.get("userId"));
                        Authentication authentication =
                                SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null) {
                            LocalUser user = (LocalUser) authentication.getPrincipal();
                            if (!userService.userHasPermissionToUser(user, userId)) {
                                message = null;
                            }
                        } else {
                            message = null;
                        }
                    } catch (NumberFormatException ex) {
                        message = null;
                    }
                }
            }
            return message;
        }
    }

}
