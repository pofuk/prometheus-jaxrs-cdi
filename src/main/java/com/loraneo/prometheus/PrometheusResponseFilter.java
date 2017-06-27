package com.loraneo.prometheus;


import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class PrometheusResponseFilter implements ContainerResponseFilter {

    @Inject
    PrometheusRequestContext monitoringContext;

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext)
            throws IOException {
        monitoringContext.stop();

    }

}
