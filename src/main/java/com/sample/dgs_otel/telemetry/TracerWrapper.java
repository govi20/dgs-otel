package com.sample.dgs_otel.telemetry;

import io.opentelemetry.api.trace.Tracer;

public class TracerWrapper {

    private final Tracer tracer;

    public TracerWrapper(Tracer tracer) {
        this.tracer = tracer;
    }

    public CustomSpanBuilder customSpanBuilder(String spanName) {
        return new CustomSpanBuilder(spanName, tracer);
    }
}
