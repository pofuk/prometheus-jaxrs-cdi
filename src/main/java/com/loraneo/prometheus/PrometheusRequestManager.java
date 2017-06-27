package com.loraneo.prometheus;


import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import io.prometheus.client.Histogram;

@ApplicationScoped
public class PrometheusRequestManager {

    private Histogram histogram;

    @PostConstruct
    public void init() {
        histogram = Histogram.build()
                .labelNames("path",
                        "method")
                .name("spot_backend_rest")
                .help("Call timer")
                .register();
    }

    public Histogram getHistogram() {
        return histogram;
    }

}
