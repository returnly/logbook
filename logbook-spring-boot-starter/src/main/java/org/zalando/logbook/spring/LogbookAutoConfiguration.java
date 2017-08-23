package org.zalando.logbook.spring;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.servlet.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.*;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.Strategy;

import static java.util.stream.Collectors.toList;
import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.REQUEST;

@Configuration
@ConditionalOnClass(Logbook.class)
@EnableConfigurationProperties(LogbookProperties.class)
@AutoConfigureAfter({
        JacksonAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
})
public class LogbookAutoConfiguration {

    private final LogbookProperties properties;

    @Autowired
    public LogbookAutoConfiguration(final LogbookProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(Logbook.class)
    public Logbook logbook(
            final Predicate<RawHttpRequest> condition,
            final List<RawRequestFilter> rawRequestFilters,
            final List<RawResponseFilter> rawResponseFilters,
            final List<HeaderFilter> headerFilters,
            final List<QueryFilter> queryFilters,
            final List<BodyFilter> bodyFilters,
            final List<RequestFilter> requestFilters,
            final List<ResponseFilter> responseFilters,
            @SuppressWarnings("SpringJavaAutowiringInspection") final HttpLogFormatter formatter,
            final HttpLogWriter writer,
            final CorrelationIdProvider correlationIdProvider) {
        return Logbook.builder()
                .condition(mergeWithExcludes(condition))
                .rawRequestFilters(rawRequestFilters)
                .rawResponseFilters(rawResponseFilters)
                .headerFilters(headerFilters)
                .queryFilters(queryFilters)
                .bodyFilters(bodyFilters)
                .requestFilters(requestFilters)
                .responseFilters(responseFilters)
                .formatter(formatter)
                .writer(writer)
                .correlationIdProvider(correlationIdProvider)
                .build();
    }

    private Predicate<RawHttpRequest> mergeWithExcludes(final Predicate<RawHttpRequest> predicate) {
        return properties.getExclude().stream()
                .map(Conditions::<RawHttpRequest>requestTo)
                .map(Predicate::negate)
                .reduce(predicate, Predicate::and);
    }

    @Bean
    @ConditionalOnMissingBean(name = "requestCondition")
    public Predicate<RawHttpRequest> requestCondition() {
        return $ -> true;
    }

    @Bean
    @ConditionalOnMissingBean(RawRequestFilter.class)
    public RawRequestFilter rawRequestFilter() {
        return RawRequestFilters.defaultValue();
    }

    @Bean
    @ConditionalOnMissingBean(RawResponseFilter.class)
    public RawResponseFilter rawResponseFilter() {
        return RawResponseFilters.defaultValue();
    }

    @Bean
    @ConditionalOnMissingBean(QueryFilter.class)
    public QueryFilter queryFilter() {
        final List<String> parameters = properties.getObfuscate().getParameters();
        return parameters.isEmpty() ?
                QueryFilters.defaultValue() :
                parameters.stream()
                        .map(parameter -> QueryFilters.replaceQuery(parameter, "XXX"))
                        .collect(toList()).stream()
                        .reduce(QueryFilter::merge)
                        .orElseGet(QueryFilter::none);
    }

    @Bean
    @ConditionalOnMissingBean(HeaderFilter.class)
    public HeaderFilter headerFilter() {
        final List<String> headers = properties.getObfuscate().getHeaders();
        return headers.isEmpty() ?
                HeaderFilters.defaultValue() :
                headers.stream()
                        .map(header -> HeaderFilters.replaceHeaders(header::equalsIgnoreCase, "XXX"))
                        .collect(toList()).stream()
                        .reduce(HeaderFilter::merge)
                        .orElseGet(HeaderFilter::none);
    }

    @Bean
    @ConditionalOnMissingBean(BodyFilter.class)
    public BodyFilter bodyFilter() {
        return BodyFilters.defaultValue();
    }

    @Bean
    @ConditionalOnMissingBean(RequestFilter.class)
    public RequestFilter requestFilter() {
        return RequestFilter.none();
    }

    @Bean
    @ConditionalOnMissingBean(ResponseFilter.class)
    public ResponseFilter responseFilter() {
        return ResponseFilter.none();
    }

    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "http")
    public HttpLogFormatter httpFormatter() {
        return new DefaultHttpLogFormatter();
    }

    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "curl")
    public HttpLogFormatter curlFormatter() {
        return new CurlHttpLogFormatter();
    }

    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            @SuppressWarnings("SpringJavaAutowiringInspection") final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer(final Logger httpLogger) {
        final LogbookProperties.Write write = properties.getWrite();
        final Level level = write.getLevel();
        final int size = write.getChunkSize();

        final HttpLogWriter writer = new DefaultHttpLogWriter(httpLogger, level);
        return size > 0 ? new ChunkingHttpLogWriter(size, writer) : writer;
    }

    @Bean
    @ConditionalOnMissingBean(name = "httpLogger")
    public Logger httpLogger() {
        return LoggerFactory.getLogger(properties.getWrite().getCategory());
    }

    @Bean
    @ConditionalOnMissingBean(CorrelationIdProvider.class)
    public CorrelationIdProvider correlationIdProvider() {
        return CorrelationIdProvider.DEFAULT;
    }


    @Configuration
    @ConditionalOnClass({
            HttpClient.class,
            LogbookHttpRequestInterceptor.class,
            LogbookHttpResponseInterceptor.class
    })
    static class HttpClientAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(LogbookHttpRequestInterceptor.class)
        public LogbookHttpRequestInterceptor logbookHttpRequestInterceptor(final Logbook logbook) {
            return new LogbookHttpRequestInterceptor(logbook);
        }

        @Bean
        @ConditionalOnMissingBean(LogbookHttpResponseInterceptor.class)
        public LogbookHttpResponseInterceptor logbookHttpResponseInterceptor() {
            return new LogbookHttpResponseInterceptor();
        }

    }

    @Configuration
    @ConditionalOnClass(Filter.class)
    @ConditionalOnWebApplication
    static class ServletFilterConfiguration {

        private static final String FILTER_NAME = "authorizedLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean authorizedLogbookFilter(final Logbook logbook) {
            final Filter filter = new LogbookFilter(logbook);
            final FilterRegistrationBean<?> registration = new FilterRegistrationBean<>(filter);
            registration.setName(FILTER_NAME);
            registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
            registration.setOrder(Ordered.LOWEST_PRECEDENCE);
            return registration;
        }

    }

    @Configuration
    @ConditionalOnClass(SecurityFilterChain.class)
    @ConditionalOnWebApplication
    @AutoConfigureAfter(SecurityFilterAutoConfiguration.class)
    static class SecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "unauthorizedLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean unauthorizedLogbookFilter(final Logbook logbook) {
            final Filter filter = new LogbookFilter(logbook, Strategy.SECURITY);
            final FilterRegistrationBean<?> registration = new FilterRegistrationBean<>(filter);
            registration.setName(FILTER_NAME);
            registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return registration;
        }

    }


}
