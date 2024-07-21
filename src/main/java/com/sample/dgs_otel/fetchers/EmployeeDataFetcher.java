package com.sample.dgs_otel.fetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.sample.dgs_otel.generated.types.Department;
import com.sample.dgs_otel.generated.types.Employee;
import graphql.schema.DataFetchingEnvironment;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@DgsComponent
public class EmployeeDataFetcher {

    @DgsQuery(field = "employees")
    public List<Employee> employees(DataFetchingEnvironment dfe) {
        Department department = Department.newBuilder().id("1").build();
        return List.of(
                Employee.newBuilder().id("1").name("John").department(department).build(),
                Employee.newBuilder().id("2").name("Adam").department(department).build(),
                Employee.newBuilder().id("3").name("Rick").department(department).build(),
                Employee.newBuilder().id("4").name("Pep").department(department).build(),
                Employee.newBuilder().id("5").name("Jose").department(department).build()
        );
    }

    @DgsData(parentType = "Employee", field = "department")
    public CompletableFuture<Department> department(DataFetchingEnvironment dfe) {
        Employee employee = dfe.getSource();
        Context context = Context.current();
        DataLoader<Parameter, Department> departmentDataLoader = dfe.getDataLoader("departmentDataLoader");

        try (Scope ignored = context.makeCurrent()) {
            System.out.println("### context in data fetcher: " + Context.current());
            return departmentDataLoader.load(new Parameter(employee.getDepartment().getId(), Context.current()));
        }
    }

}
