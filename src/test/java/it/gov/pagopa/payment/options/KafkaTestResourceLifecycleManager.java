package it.gov.pagopa.payment.options;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import java.util.HashMap;
import java.util.Map;

public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchIncomingChannelsToInMemory("nodo-dei-pagamenti-cache");
        Map<String, String> props2 = InMemoryConnector.switchOutgoingChannelsToInMemory("nodo-dei-pagamenti-verify-ko");
        Map<String, String> props3 = InMemoryConnector.switchOutgoingChannelsToInMemory("opzioni-di-pagamento-re");
        env.putAll(props1);
        env.putAll(props2);
        env.putAll(props3);
        return env;  
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }

}
