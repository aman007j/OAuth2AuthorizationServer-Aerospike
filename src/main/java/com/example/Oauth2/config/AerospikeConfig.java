package com.example.Oauth2.config;

import com.aerospike.client.AerospikeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AerospikeConfig {
    @Bean
    public AerospikeClient aerospikeClient() {
        return new AerospikeClient("localhost", 3000);
    }
}
