package com.sample.dgs_otel.telemetry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Tracing {
    public static final String CORRELATION_ID = "correlationId";
    private final TracerWrapper tracerWrapper;

    @Autowired
    public Tracing(TracerWrapper tracerWrapper) {
        this.tracerWrapper = tracerWrapper;
    }

    public CustomSpan traceExecution(String correlationId,
                                     String query,
                                     Map<String, Object> variables) {
        return tracerWrapper.customSpanBuilder("Execution")
                .withAttribute(CORRELATION_ID, correlationId)
                .withAttribute("query", query)
                .withAttribute("variables", variables)
                .start();
    }

    public CustomSpan traceFieldResolver(String fieldType, String fieldName, String correlationId) {
        return tracerWrapper.customSpanBuilder(fieldType + "::" + fieldName)
                .withAttribute(CORRELATION_ID, correlationId)
                .start();
    }
}
