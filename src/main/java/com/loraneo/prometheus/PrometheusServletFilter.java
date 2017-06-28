package com.loraneo.prometheus;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.prometheus.client.Histogram;

/**
 * The MetricsFilter class exists to provide a high-level filter that enables tunable collection of metrics for Servlet
 * performance.
 *
 * The Histogram name itself is required, and configured with a {@code metric-name} init parameter.
 *
 * The help parameter, configured with the {@code help} init parameter, is not required but strongly recommended.
 *
 * By default, this filter will provide metrics that distinguish only 1 level deep for the request path
 * (including servlet context path), but can be configured with the {@code path-components} init parameter. Any number
 * provided that is less than 1 will provide the full path granularity (warning, this may affect performance).
 *
 * The Histogram buckets can be configured with a {@code buckets} init parameter whose value is a comma-separated list
 * of valid {@code double} values.
 *
 * {@code
 * <filter>
 *   <filter-name>prometheusFilter</filter-name>
 *   <filter-class>net.cccnext.ssp.portal.spring.filter.PrometheusMetricsFilter</filter-class>
 *   <init-param>
 *      <param-name>metric-name</param-name>
 *      <param-value>webapp_metrics_filter</param-value>
 *   </init-param>
 *    <init-param>
 *      <param-name>help</param-name>
 *      <param-value>The time taken fulfilling servlet requests</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>buckets</param-name>
 *      <param-value>0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>path-components</param-name>
 *      <param-value>0</param-value>
 *   </init-param>
 * </filter>
 * }
 *
 * @author Andrew Stuart &lt;andrew.stuart2@gmail.com&gt;
 */
public class PrometheusServletFilter implements Filter {

    private static final double[] DEFAULT_BUCKETS = new double[] {.005,
                                                                  .01,
                                                                  .025,
                                                                  .05,
                                                                  .075,
                                                                  .1,
                                                                  .25,
                                                                  .5,
                                                                  .75,
                                                                  1,
                                                                  2.5,
                                                                  5,
                                                                  7.5,
                                                                  10 };

    private Histogram histogram = null;
    private int pathComponents = 1;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.histogram = Optional.ofNullable(filterConfig)
                .map(this::buildHistogram)
                .orElse(null);

        this.pathComponents = Optional.ofNullable(filterConfig)
                .map(p -> filterConfig.getInitParameter("help"))
                .map(Integer::valueOf)
                .orElse(1);
    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest,
                    servletResponse);
            return;
        }

        final Histogram.Timer timer = startTimer((HttpServletRequest) servletRequest);
        try {
            filterChain.doFilter(servletRequest,
                    servletResponse);
        } finally {
            timer.observeDuration();
        }
    }

    private Histogram.Timer startTimer(final HttpServletRequest request) {
        return histogram.labels(getComponents(request.getRequestURI(),
                pathComponents),
                request.getMethod())
                .startTimer();
    }

    @Override
    public void destroy() {
    }

    private Histogram buildHistogram(final FilterConfig filterConfig) {
        return Histogram.build()
                .labelNames("path",
                        "method")
                .name(Optional.ofNullable(filterConfig.getInitParameter("metric-name"))
                        .orElse("http_metrics"))
                .help(Optional.ofNullable(filterConfig.getInitParameter("help"))
                        .orElse("The time taken fulfilling servlet requests"))
                .buckets(Optional.ofNullable(filterConfig.getInitParameter("buckets"))
                        .map(p -> parseBuckets(p))
                        .orElse(DEFAULT_BUCKETS))
                .register();
    }

    private double[] parseBuckets(final String buckets) {
        return Arrays.stream(buckets.split(","))
                .mapToDouble(Double::valueOf)
                .toArray();
    }

    private String getComponents(final String str,
                                 final int noOfComponents) {
        return Optional.of(str)
                .map(p -> Paths.get("/",
                        p))
                .filter(p -> (p.getNameCount() > noOfComponents))
                .map(p -> p.subpath(0,
                        noOfComponents))
                .map(p -> p.toString())
                .orElse(str);

    }
}
