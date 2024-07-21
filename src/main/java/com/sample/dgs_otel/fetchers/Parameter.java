package com.sample.dgs_otel.fetchers;

import io.opentelemetry.context.Context;

public class Parameter {

    private final String departmentId;
    private final Context context; //added for debugging

    public Parameter(String departmentId, Context context) {
        this.departmentId = departmentId;
        this.context = context;
    }

    // added context field for debugging
    public Context getContext() {
        return context;
    }

    public String getDepartmentId() {
        return departmentId;
    }
}
