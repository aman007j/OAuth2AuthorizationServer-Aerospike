package com.example.Oauth2.config;


import com.aerospike.client.AerospikeClient;
import com.example.Oauth2.repository.AerospikeRegisteredClientRepository;
import com.example.Oauth2.service.AerospikeOAuth2AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;
import java.util.UUID;

import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

@Configuration
public class AuthorizationServerConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .with(authorizationServerConfigurer, config -> config.oidc(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(AerospikeClient aerospikeClient) {
        AerospikeRegisteredClientRepository repository = new AerospikeRegisteredClientRepository(aerospikeClient);

        // Initialize with one default client if not present
        String clientId = "my-client";
        if (repository.findByClientId(clientId) == null) {
            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode("secret"))
                    .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://127.0.0.1:8080/authorized")
                    .scope("read")
                    .scope("write")
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                            .accessTokenTimeToLive(Duration.ofMinutes(30))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .build())
                    .build();
            repository.save(registeredClient);
        }

        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(AerospikeClient aerospikeClient,
                                                           RegisteredClientRepository clientRepo) {
        return new AerospikeOAuth2AuthorizationService(aerospikeClient, clientRepo);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
















//    @Bean
//    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository clients) {
//        // Persists issued tokens in DB
//        return new JdbcOAuth2AuthorizationService(jdbcTemplate, clients);
//    }


//    @Bean
//    public CommandLineRunner initClient(RegisteredClientRepository repository) {
//        return args -> {
//            if (repository.findByClientId("my-client") == null) {
//                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
//                        .clientId("my-client")
//                        .clientSecret(passwordEncoder.encode("secret"))
//                        .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
//                        .authorizationGrantType(CLIENT_CREDENTIALS)
//                        .scope("read")
//                        .scope("write")
//                        .tokenSettings(TokenSettings.builder()
//                                .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
//                                .accessTokenTimeToLive(Duration.ofMinutes(30))
//                                .build())
//                        .build();
//                repository.save(client);
//            }
//        };
//    }




















































































/*
 for jwt

 @Configuration
public class AuthorizationServerConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .with(authorizationServerConfigurer, config -> config.oidc(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build(); // uses default paths
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("my-client2")
                .clientSecret(passwordEncoder.encode("secret"))
                .redirectUri("http://localhost:9000/callback")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .reuseRefreshTokens(false)
                        .build()
                )
                .build();
        return new InMemoryRegisteredClientRepository(client);
    }
}
*/

