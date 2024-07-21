package com.sample.dgs_otel.telemetry;

import io.opentelemetry.api.trace.Tracer;

import java.util.HashMap;
import java.util.Map;

public class CustomSpanBuilder {
    private final String name;
    private final Tracer tracer;
    private final Map<String, Object> attributes = new HashMap<>();

    public CustomSpanBuilder(String name, Tracer tracer) {
        this.name = name;
        this.tracer = tracer;
    }

    public CustomSpanBuilder withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public CustomSpan start() {
        return new CustomSpan(name, attributes, tracer);
    }
}
