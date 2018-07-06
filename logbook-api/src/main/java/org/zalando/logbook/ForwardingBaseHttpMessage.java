package org.zalando.logbook;

import org.apiguardian.api.API;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingBaseHttpMessage extends BaseHttpMessage {

    BaseHttpMessage delegate();

    @Override
    default String getProtocolVersion() {
        return delegate().getProtocolVersion();
    }

    @Override
    default Origin getOrigin() {
        return delegate().getOrigin();
    }

    @Override
    default Map<String, List<String>> getHeaders() {
        return delegate().getHeaders();
    }

    @Override
    default String getContentType() {
        return delegate().getContentType();
    }

    @Override
    default Charset getCharset() {
        return delegate().getCharset();
    }

}
