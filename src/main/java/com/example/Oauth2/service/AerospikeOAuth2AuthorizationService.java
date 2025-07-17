package com.example.Oauth2.service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import com.aerospike.client.Record;

import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

public class AerospikeOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final AerospikeClient client;
    private final RegisteredClientRepository clientRepository;
    private static final String NS = "test", SET = "oauth2_auth";

    public AerospikeOAuth2AuthorizationService(AerospikeClient client, RegisteredClientRepository clientRepository) {
        this.client = client;
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(OAuth2Authorization auth) {
        Key key = new Key(NS, SET, auth.getId());
        OAuth2AccessToken token = auth.getAccessToken() != null
                ? auth.getAccessToken().getToken()
                : null;

        if(token == null) {
            return;
        }

        Bin data = new Bin("data", serialize(auth));
        Bin tokenBin = new Bin("token", token.getTokenValue());
        Bin issuedAt = new Bin("issuedAt", token.getIssuedAt().toString());
        Bin expiresAt = new Bin("expiresAt", token.getExpiresAt().toString());
        Bin scopes = new Bin("scopes", String.join(" ", token.getScopes()));

        client.put(null, key, data, tokenBin, issuedAt, expiresAt, scopes);
        client.put(null, new Key(NS, "token_index", token.getTokenValue()), new Bin("auth_id", auth.getId()));
    }

    @Override
    public void remove(OAuth2Authorization auth) {
        Key key = new Key(NS, SET, auth.getId());
        client.delete(null, key);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Record rec = client.get(null, new Key(NS, SET, id));
        return (rec == null) ? null : (OAuth2Authorization) deserialize((byte[]) rec.bins.get("data"));
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType type) {
        Record idx = client.get(null, new Key(NS, "token_index", token));
        if (idx == null) return null;
        return findById(idx.getString("auth_id"));
    }
}
