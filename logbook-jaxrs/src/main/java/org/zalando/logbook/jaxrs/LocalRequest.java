package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

final class LocalRequest implements HttpRequest, RawHttpRequest {

    private final ClientRequestContext context;

    private final TeeOutputStream stream;
    private byte[] body;

    public LocalRequest(final ClientRequestContext context) {
        // TODO now we need to buffer needlessly if we never actually need the body
        this.stream = new TeeOutputStream(context.getEntityStream());
        context.setEntityStream(stream);
        this.context = context;
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getUri().getScheme();
    }

    @Override
    public String getHost() {
        return context.getUri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return HttpMessages.getPort(context.getUri());
    }

    @Override
    public String getPath() {
        return context.getUri().getPath();
    }

    @Override
    public String getQuery() {
        return ofNullable(context.getUri().getQuery()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getStringHeaders();
    }

    @Nullable
    @Override
    public String getContentType() {
        return context.getStringHeaders().getFirst("Content-Type");
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
    }

    @Override
    public HttpRequest withBody() {
        return this;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            this.body = stream.toByteArray();
        }

        return body;
    }

}
