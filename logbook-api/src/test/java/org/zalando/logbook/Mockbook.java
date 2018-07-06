package org.zalando.logbook;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;


final class Mockbook implements Logbook {

    private final Predicate<RawHttpRequest> predicate;
    private final RawRequestFilter rawRequestFilter;
    private final RawResponseFilter rawResponseFilter;
    private final QueryFilter queryFilter;
    private final HeaderFilter headerFilter;
    private final BodyFilter bodyFilter;
    private final RequestFilter requestFilter;
    private final ResponseFilter responseFilter;
    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;
    private final CorrelationIdProvider correlationIdProvider;

    public Mockbook(
            @Nullable final Predicate<RawHttpRequest> predicate,
            @Nullable final RawRequestFilter rawRequestFilter,
            @Nullable final RawResponseFilter rawResponseFilter,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer,
            @Nullable final CorrelationIdProvider correlationIdProvider) {
        this.predicate = predicate;
        this.rawRequestFilter = rawRequestFilter;
        this.rawResponseFilter = rawResponseFilter;
        this.queryFilter = queryFilter;
        this.headerFilter = headerFilter;
        this.bodyFilter = bodyFilter;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.formatter = formatter;
        this.writer = writer;
        this.correlationIdProvider = correlationIdProvider;
    }

    @Override
    public Optional<Correlator> write(final RawHttpRequest request) {
        throw new UnsupportedOperationException();
    }

    public Predicate<RawHttpRequest> getPredicate() {
        return predicate;
    }

    public RawRequestFilter getRawRequestFilter() {
        return rawRequestFilter;
    }

    public RawResponseFilter getRawResponseFilter() {
        return rawResponseFilter;
    }

    public BodyFilter getBodyFilter() {
        return bodyFilter;
    }

    public HeaderFilter getHeaderFilter() {
        return headerFilter;
    }

    public QueryFilter getQueryFilter() {
        return queryFilter;
    }

    public RequestFilter getRequestFilter() {
        return requestFilter;
    }

    public ResponseFilter getResponseFilter() {
        return responseFilter;
    }

    public HttpLogFormatter getFormatter() {
        return formatter;
    }

    public HttpLogWriter getWriter() {
        return writer;
    }

    public CorrelationIdProvider getCorrelationIdProvider() {
        return correlationIdProvider;
    }

}
