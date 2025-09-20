package com.ml.OpusML.rest.controller;

import opusml.OpusMLServiceGrpc;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class OpusMLController {

    //Log for incoming REST and gRPC calls
    private static final Logger logger = (Logger) LoggerFactory.getLogger(OpusMLController.class);

    private final OpusMLServiceGrpc.OpusMLServiceBlockingStub grpcStub;


    @Autowired
    public OpusMLController(OpusMLServiceGrpc.OpusMLServiceBlockingStub grpcStub) {
        this.grpcStub = grpcStub;
    }
}
