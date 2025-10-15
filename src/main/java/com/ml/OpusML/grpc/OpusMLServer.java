package com.ml.OpusML.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class OpusMLServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(9090)
                .addService(new OpusMLServiceImpl())
                .build()
                .start();

        System.out.println("OpusML gRPC Server started on port 50051");
        server.awaitTermination();
    }
}
