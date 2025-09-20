package com.ml.OpusML.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import opusml.OpusMLServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel grpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext() //remove in prod with TLS
                .build();
    }

    @Bean
    public OpusMLServiceGrpc.OpusMLServiceBlockingStub opusMlBlockingStub(ManagedChannel channel) {
        return OpusMLServiceGrpc.newBlockingStub(channel);
    }
}
