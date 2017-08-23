package org.zalando.logbook;

import java.util.UUID;


/**
 * Responsible for providing correlation ID.
 * Implementations may generate a new one (e.g. UUID), or delegate to another library (e.g. spring-cloud-sleuth).
 */
@FunctionalInterface
public interface CorrelationIdProvider {

    String getId();

    CorrelationIdProvider DEFAULT = () -> UUID.randomUUID().toString();

}
