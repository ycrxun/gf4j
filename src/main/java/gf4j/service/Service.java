package gf4j.service;

import gf4j.registry.Registry;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.opentracing.Tracer;
import io.opentracing.contrib.ServerTracingInterceptor;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Slf4j
public class Service {

    private String id;
    private String name;
    private Addr serviceAddr;
    private Addr prometheusAddr;

    // private for inner
    private Server server;
    private HTTPServer httpServer;

    private Registry registry;
    private List<ServerInterceptor> interceptors = new ArrayList<>();
    private Tracer tracer;
    private BindableService bindableService;

    public Service(String name) {
        this.id = generateID(name);
        this.name = name;
        this.serviceAddr = new Addr("0.0.0.0", 9100);
        this.prometheusAddr = new Addr("0.0.0.0", 9000);
    }

    private String generateID(String name) {
        return UUID.randomUUID().toString() + "-" + name;
    }

    public void setGRPCImpl(BindableService bindableService) {
        this.bindableService = bindableService;
    }

    public void setServiceAddr(Addr serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public void setPrometheusAddr(Addr prometheusAddr) {
        this.prometheusAddr = prometheusAddr;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void addInterceptors(ServerInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void run() throws IOException {

        // trace
        if (this.tracer != null) {
            // to do sth.
            ServerTracingInterceptor tracingInterceptor = new
                    ServerTracingInterceptor(this.tracer);
            this.interceptors.add(tracingInterceptor);
            log.info("init tracer");
        }

        // prometheus collector registry
        CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        MonitoringServerInterceptor monitoringInterceptor = MonitoringServerInterceptor
                .create(Configuration.cheapMetricsOnly().withCollectorRegistry(collectorRegistry));
        this.interceptors.add(monitoringInterceptor);

        // grpc
        server = ServerBuilder
                .forPort(this.getServiceAddr().getPort())
                .addService(ServerInterceptors.intercept(this.bindableService, interceptors)).build()
                .start();

        log.info("gRPC server started, listening on: {}", this.getServiceAddr().toString());

        // prometheus metrics
        this.httpServer = new HTTPServer(new InetSocketAddress(this.prometheusAddr.getHost(),
                this.prometheusAddr.getPort()), collectorRegistry);
        log.info("Prometheus server started, listening on: {}", this.getPrometheusAddr().toString());

        // registry
        if (registry != null) {
            try {
                registry.register(this.id, this.name,
                        this.getServiceAddr().getPort
                                (), "grpc-endpoint");
                registry.register(this.id + "-metrics", this.name + "-metrics", this
                        .getPrometheusAddr()
                        .getPort(), "prometheus-target");
            } catch (Exception e) {
                log.error("registry failed.", e);
            }
        }
        // hook
        Runtime.getRuntime().addShutdownHook(
                new Thread(Service.this::stop)
        );
    }

    private void stop() {
        // stop gRPC
        if (server != null) {
            log.warn("*** shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            log.warn("*** gRPC server shut down");
        }

        // stop prometheus
        if (this.httpServer != null) {
            log.warn("*** shutting down prometheus server since JVM is shutting down");
            this.httpServer.stop();
            log.warn("*** prometheus server shut down");
        }

        // de registry
        if (this.registry != null) {
            this.registry.deregister(this.id);
            this.registry.deregister(this.id + "-metrics");
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
