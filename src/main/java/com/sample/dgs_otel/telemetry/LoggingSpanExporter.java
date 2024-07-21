package com.sample.dgs_otel.telemetry;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class LoggingSpanExporter implements SpanExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

    private static final String SPAN_ID = "spanId";
    private static final String TRACE_ID = "traceId";
    private static final String CONTEXT = "context";
    private static final String STATUS = "status";
    private static final String ATTRIBUTES = "attributes";

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            Map<String, String> attributes = Map.of("key", "value");
            Map<String, Object> context = Map.of("key", "value");

            if(span.getStatus().getStatusCode() != StatusCode.UNSET) {
                context.put(STATUS, String.valueOf(span.getStatus().getStatusCode()));
            }
            context.put(ATTRIBUTES, attributes);
            LOGGER.info(span.getName(), kv(CONTEXT, context), kv(SPAN_ID, span.getSpanId()), kv(TRACE_ID, span.getTraceId()));
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return flush();
    }
}
