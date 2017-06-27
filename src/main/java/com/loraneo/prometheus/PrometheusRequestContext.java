package com.loraneo.prometheus;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import io.prometheus.client.Histogram;

@RequestScoped
public class PrometheusRequestContext {

    private String jaxRsPath;

    private Histogram.Timer timer;

    @Inject
    private PrometheusRequestManager prometheusRequestManager;

    public String getJaxRsPath() {
        return jaxRsPath;
    }

    public void start(final String jaxRsPath,
                      final String method) {
        timer = prometheusRequestManager.getHistogram()
                .labels(jaxRsPath,
                        method)
                .startTimer();

        this.jaxRsPath = jaxRsPath;

    }

    public void stop() {
        timer.observeDuration();
    }
}
