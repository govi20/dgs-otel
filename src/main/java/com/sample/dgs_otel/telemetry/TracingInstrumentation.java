package com.sample.dgs_otel.telemetry;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TracingInstrumentation extends SimpleInstrumentation {

    private final Tracing tracing;

    @Autowired
    public TracingInstrumentation(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters env) {
        final String query = normalize(env.getQuery());
        String correlationId = env.getExecutionInput().getExecutionId().toString();
        MDC.put(Tracing.CORRELATION_ID, correlationId);
        CustomSpan customSpan = tracing.traceExecution(correlationId, query, env.getVariables());

        return SimpleInstrumentationContext.whenCompleted((result, excp) -> {
           if(excp == null && !result.getErrors().isEmpty()) {
               customSpan.setError(result.getErrors().stream().map(GraphQLError::getMessage).collect(Collectors.joining()));
           }
           customSpan.endWhenFutureCompleted(result, excp);
        });
    }

    private String normalize(String query) {
        return query.trim().replaceAll(System.getProperty("line.separator"), " ");
    }
}
