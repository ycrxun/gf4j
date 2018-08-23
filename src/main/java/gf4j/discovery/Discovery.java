package gf4j.discovery;

import io.grpc.ManagedChannel;

public interface Discovery {
    ManagedChannel dial(String serviceName);
}
