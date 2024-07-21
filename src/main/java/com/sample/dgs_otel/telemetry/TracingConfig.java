package com.sample.dgs_otel.telemetry;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TracingConfig {

    private final LoggingSpanExporter exporter;

    public TracingConfig(LoggingSpanExporter exporter) {
        this.exporter = exporter;
    }

    @Bean
    public TracerWrapper tracer() {
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .setSampler(Sampler.alwaysOn()).build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        return new TracerWrapper(sdk.getTracer("custom-tracer", "1"));
    }
}
