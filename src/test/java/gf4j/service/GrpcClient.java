package gf4j.service;

import gf4j.examples.helloworld.GreeterGrpc;
import gf4j.examples.helloworld.HelloReply;
import gf4j.examples.helloworld.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public GrpcClient(Addr addr) {
        this(ManagedChannelBuilder.forAddress(addr.getHost(), addr.getPort()).usePlaintext().build());
    }

    public GrpcClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void greet(String name) {
        log.info("Will try to greet " + name + " ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus());
            return;
        }
        log.info("Greeting: " + response.getMessage());
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        GrpcClient client = new GrpcClient(new Addr("0.0.0.0", 9100));
        client.greet("soi");

    }
}
