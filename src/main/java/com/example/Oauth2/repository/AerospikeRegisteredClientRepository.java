package com.example.Oauth2.repository;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.SerializationUtils;

public class AerospikeRegisteredClientRepository implements RegisteredClientRepository {

    private static final String NS = "test";
    private static final String SET = "oauth2_clients";
    private final AerospikeClient aerospikeClient;

    public AerospikeRegisteredClientRepository(AerospikeClient client) {
        this.aerospikeClient = client;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Key key = new Key(NS, SET, registeredClient.getId());
        Bin data = new Bin("data", SerializationUtils.serialize(registeredClient));
        Bin clientIdBin = new Bin("clientId", registeredClient.getClientId());
        aerospikeClient.put(null, key, data, clientIdBin);

        // Create a secondary index by clientId
        aerospikeClient.put(null, new Key(NS, "client_id_index", registeredClient.getClientId()),
                new Bin("id", registeredClient.getId()));
    }

    @Override
    public RegisteredClient findById(String id) {
        Record record = aerospikeClient.get(null, new Key(NS, SET, id));
        if (record == null) return null;
        return (RegisteredClient) SerializationUtils.deserialize((byte[]) record.bins.get("data"));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Record indexRecord = aerospikeClient.get(null, new Key(NS, "client_id_index", clientId));
        if (indexRecord == null) return null;
        String id = indexRecord.getString("id");
        return findById(id);
    }

    public void deleteById(String id) {
        aerospikeClient.delete(null, new Key(NS, SET, id));
    }

    public void deleteByClientId(String clientId) {
        Record indexRecord = aerospikeClient.get(null, new Key(NS, "client_id_index", clientId));
        if (indexRecord != null) {
            String id = indexRecord.getString("id");
            deleteById(id);
            aerospikeClient.delete(null, new Key(NS, "client_id_index", clientId));
        }
    }
}
