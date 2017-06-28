package com.loraneo.prometheus;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.prometheus.client.Histogram;

@RequestScoped
public class PrometheusRequestContext {

    private Histogram.Timer timer;

    @Inject
    private PrometheusRequestManager prometheusRequestManager;

    public void start(final String jaxRsPath,
                      final String method) {
        timer = prometheusRequestManager.getHistogram()
                .labels(jaxRsPath,
                        method)
                .startTimer();

    }

    public void stop() {
        Optional.ofNullable(timer)
                .ifPresent(p -> p.observeDuration());
    }
}
