package org.zalando.logbook.spring;


import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.Logbook;

import static org.apiguardian.api.API.Status.INTERNAL;


@API(status = INTERNAL)
@ConfigurationProperties(prefix = "logbook")
public final class LogbookProperties {

    private final List<String> exclude = new ArrayList<>();
    private final Obfuscate obfuscate = new Obfuscate();
    private final Write write = new Write();
    private final Format format = new Format();

    public List<String> getExclude() {
        return exclude;
    }

    public Obfuscate getObfuscate() {
        return obfuscate;
    }

    public Write getWrite() {
        return write;
    }

    public Format getFormat() {
        return format;
    }

    public static class Obfuscate {

        private final List<String> headers = new ArrayList<>();
        private final List<String> parameters = new ArrayList<>();

        public List<String> getHeaders() {
            return headers;
        }

        public List<String> getParameters() {
            return parameters;
        }

    }

    public static class Write {

        private String category = Logbook.class.getName();
        private Level level = Level.TRACE;
        private int chunkSize;
        private int maxBodySize = -1;

        public String getCategory() {
            return category;
        }

        public void setCategory(final String category) {
            this.category = category;
        }

        public Level getLevel() {
            return level;
        }

        public void setLevel(final Level level) {
            this.level = level;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(final int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public int getMaxBodySize() {
            return maxBodySize;
        }

        public void setMaxBodySize(final int maxBodySize) {
            this.maxBodySize = maxBodySize;
        }

    }

    public static class Format {

        public enum Style { http, curl, json }

        private Style style = Style.json;

        public Style getStyle() {
            return style;
        }

        public void setStyle(final Style style) {
            this.style = style;
        }

    }

}
