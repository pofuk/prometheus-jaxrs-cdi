package com.loraneo.prometheus;


import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;

public class PrometheusRequestFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private PrometheusRequestContext monitoringContext;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        monitoringContext.start(getPath(resourceInfo),
                requestContext.getRequest()
                        .getMethod());

    }

    private String getPath(final ResourceInfo resourceInfo) {
        return Paths.get("/",
                getValue(resourceInfo.getResourceMethod()),
                getValue(resourceInfo.getResourceClass()))
                .toString();
    }

    private String getValue(final Method method) {
        return getAnnotationValue(method.getAnnotation(Path.class));
    }

    private String getValue(final Class<?> sourceClass) {
        return getAnnotationValue(sourceClass.getAnnotation(Path.class));
    }

    private String getAnnotationValue(final Path annot) {
        return Optional.ofNullable(annot)
                .map(Path::value)
                .orElse("");
    }

}
