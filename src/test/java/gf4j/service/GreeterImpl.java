package gf4j.service;

import gf4j.examples.helloworld.GreeterGrpc;
import gf4j.examples.helloworld.HelloReply;
import gf4j.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
