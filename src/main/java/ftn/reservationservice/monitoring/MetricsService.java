package ftn.reservationservice.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MetricsService {

    private final Counter httpRequestsCounter;
    private final Counter successfulHttpRequestsCounter;
    private final Counter failedHttpRequestsCounter;
    private final Set<String> uniqueVisitors;
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.httpRequestsCounter = Counter.builder("http.requests.total").description("Total number of HTTP requests").register(meterRegistry);
        this.successfulHttpRequestsCounter = Counter.builder("http.requests.success").description("Number of successful HTTP requests").register(meterRegistry);
        this.failedHttpRequestsCounter = Counter.builder("http.requests.failed").description("Number of failed HTTP requests").register(meterRegistry);
        this.uniqueVisitors = new HashSet<>();
    }

    public void trackHttpRequest(HttpServletRequest request, HttpServletResponse response) {
        httpRequestsCounter.increment();

        if (response.getStatus() == HttpServletResponse.SC_OK) {
            successfulHttpRequestsCounter.increment();
        } else if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
            Counter.builder("http.requests.404")
                    .description("Number of HTTP 404 requests")
                    .tags(Tags.of("endpoint", request.getRequestURI()))
                    .register(meterRegistry)
                    .increment();
        } else {
            failedHttpRequestsCounter.increment();
        }

        String visitorKey = request.getRemoteAddr() + "|" + request.getHeader("User-Agent");
        if (uniqueVisitors.add(visitorKey)) {
            meterRegistry.counter("unique.visitors").increment();
        }
    }

}
