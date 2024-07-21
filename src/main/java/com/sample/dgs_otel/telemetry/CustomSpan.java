package com.sample.dgs_otel.telemetry;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;

import java.util.Map;

public class CustomSpan {
    private final Span span;
    private final SpanBuilder spanBuilder;
    private final String name;

    public CustomSpan(Span span) {
        this.span = span;
        this.spanBuilder = null;
        this.name = null;
    }

    public CustomSpan(String name, Map<String, Object> attributes, Tracer tracer) {
        this.name = name;
        this.spanBuilder = tracer.spanBuilder(this.name);
        this.span = start();
        this.setAttributes(attributes);
    }

    private Span start() {
        spanBuilder.setSpanKind(SpanKind.INTERNAL);
        return spanBuilder.startSpan();
    }

    public void setAttributes(Map<String, Object> attributes) {
        attributes.forEach(this::setAttribute);
    }

    private void setAttribute(String name, Object value) {
        span.setAttribute(name, value.toString());
    }

    public void setException(Throwable throwable) {
        span.setStatus(StatusCode.ERROR, throwable.getMessage());
    }

    public void end() {
        span.end();
    }

    public void setError(String error) {
        span.setStatus(StatusCode.ERROR, error);
    }

    public <T> void endWhenFutureCompleted(T ignored, Throwable throwable) {
        if(throwable != null) {
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
        }
        end();
    }

    public Scope makeCurrent() {
        return span.makeCurrent();
    }

    public String getId() {
        return span.getSpanContext().getSpanId();
    }
}
