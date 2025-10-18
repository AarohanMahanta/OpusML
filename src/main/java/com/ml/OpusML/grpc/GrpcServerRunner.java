package com.ml.OpusML.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class GrpcServerRunner {

    private final OpusMLServiceImpl opusMLService;
    private Server server;

    public GrpcServerRunner(OpusMLServiceImpl opusMLService) {
        this.opusMLService = opusMLService;
    }

    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(9090)
                .addService(opusMLService)
                .build()
                .start();

        System.out.println("OpusML gRPC server started on port 9090");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            stop();
        }));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
