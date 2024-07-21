package com.sample.dgs_otel.aspect;

import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.sample.dgs_otel.telemetry.CustomSpan;
import com.sample.dgs_otel.telemetry.Tracing;
import graphql.schema.DataFetchingEnvironment;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Aspect
@Component
public class TracingAspect {

    private final Tracing tracing;

    @Autowired
    public TracingAspect(Tracing tracing) {
        this.tracing = tracing;
    }

    @Around("@annotation(com.netflix.graphql.dgs.DgsQuery) && args(dfe,..)")
    public Object traceQuery(ProceedingJoinPoint pjp, DataFetchingEnvironment dfe) throws Throwable {
        DgsQuery annotation = ((MethodSignature) pjp.getSignature()).getMethod().getDeclaredAnnotation(DgsQuery.class);
        String field = annotation.field();
        Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();

        if (CompletableFuture.class.isAssignableFrom(returnType) ||
                CompletionStage.class.isAssignableFrom(returnType)) {
           return traceAsyncField(pjp, dfe, "Query", field);
        } else {
            return traceField(pjp, dfe, "Query", field);
        }
    }

    @Around("@annotation(com.netflix.graphql.dgs.DgsData) && args(dfe,..)")
    public Object traceData(ProceedingJoinPoint pjp, DataFetchingEnvironment dfe) throws Throwable {
        DgsData annotation = ((MethodSignature) pjp.getSignature()).getMethod().getDeclaredAnnotation(DgsData.class);
        Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();
        String field = annotation.parentType() + "::" + annotation.field();
        if (CompletableFuture.class.isAssignableFrom(returnType) ||
                CompletionStage.class.isAssignableFrom(returnType)) {
            return traceAsyncField(pjp, dfe, "Field", field);
        } else {
            return traceField(pjp, dfe, "Field", field);
        }
    }

    private Object traceAsyncField(ProceedingJoinPoint pjp, DataFetchingEnvironment dfe, String fieldType, String fieldName) throws Throwable {
        String correlationId = dfe.getExecutionId().toString();
        CustomSpan span = tracing.traceFieldResolver(fieldType, fieldName, correlationId);
        try (Scope ignore = span.makeCurrent()) {
            Object object = pjp.proceed();
            if(object instanceof CompletableFuture) {
                CompletableFuture<?> future = (CompletableFuture<?>) object;
                future.whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        span.setException(throwable);
                    }
                    span.end();
                });
            }
            return object;
        } catch (Throwable t) {
            span.setException(t);
            throw t;
        }
    }

    private Object traceField(ProceedingJoinPoint pjp, DataFetchingEnvironment dfe, String fieldType, String fieldName)
            throws Throwable {
        String correlationId = dfe.getExecutionId().toString();
        CustomSpan span = tracing.traceFieldResolver(fieldType, fieldName, correlationId);
        try (Scope ignore = span.makeCurrent()) {
            return pjp.proceed();
        } catch (Throwable t) {
            span.setException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
