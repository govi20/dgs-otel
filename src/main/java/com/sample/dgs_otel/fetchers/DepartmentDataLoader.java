package com.sample.dgs_otel.fetchers;

import com.netflix.graphql.dgs.DgsDataLoader;
import com.sample.dgs_otel.generated.types.Department;
import io.opentelemetry.context.Context;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@DgsDataLoader(name = "departmentDataLoader")
public class DepartmentDataLoader implements BatchLoader<Parameter, Department> {

    private final ExecutorService dgsAsyncTaskExecutor;

    @Autowired
    public DepartmentDataLoader(@Qualifier("dgsAsyncTaskExecutor") ExecutorService dgsAsyncTaskExecutor) {
        this.dgsAsyncTaskExecutor = dgsAsyncTaskExecutor;
    }

    @Override
    public CompletionStage<List<Department>> load(List<Parameter> parameters) {
        //TODO: If I call parameters.get(0).getContext().makeCurrent() then context gets propagated correctly.
        System.out.println("### context lost here, even before offloading a task on executor: " + Context.current());
        return CompletableFuture.supplyAsync(() -> fetchDepartments(parameters), dgsAsyncTaskExecutor);
    }

    private List<Department> fetchDepartments(List<Parameter> parameters) {
        try {
            TimeUnit.SECONDS.sleep(3); // simulate IO call
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return parameters.stream()
                .map(Parameter::getDepartmentId)
                .map(id -> Department.newBuilder().id(id).name("Name:" + id).build())
                .toList();
    }
}
