package org.zalando.logbook;


import java.util.function.Predicate;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import static java.util.ServiceLoader.load;

import static org.apiguardian.api.API.Status.STABLE;


@API(status = STABLE)
interface LogbookFactory {

    LogbookFactory INSTANCE = load(LogbookFactory.class).iterator().next();

    Logbook create(
            @Nullable final Predicate<RawHttpRequest> condition,
            @Nullable final RawRequestFilter rawRequestFilter,
            @Nullable final RawResponseFilter rawResponseFilter,
            @Nullable final QueryFilter queryFilter,
            @Nullable final HeaderFilter headerFilter,
            @Nullable final BodyFilter bodyFilter,
            @Nullable final RequestFilter requestFilter,
            @Nullable final ResponseFilter responseFilter,
            @Nullable final HttpLogFormatter formatter,
            @Nullable final HttpLogWriter writer,
            @Nullable final CorrelationIdProvider correlationIdProvider);

}
