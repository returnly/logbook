package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.BodyFilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestPropertySource(properties = "logbook.write.max-body-size = 20")
public final class WriteBodyMaxSizeTest extends AbstractTest {

    @Autowired
    private BodyFilter bodyFilter;

    @Test
    void shouldUseBodyMaxSizeFilter() {
        assertThat(bodyFilter.filter("application/json", "{\"foo\":\"someLongMessage\"}"),
                is("{\"foo\":\"someLongMess..."));
    }

    @Test
    void shouldUseBodyMaxSizeOverDefaultFilter() {
        assertThat(bodyFilter.filter("application/json", "{\"open_id\":\"someLongSecret\",\"foo\":\"bar\"}"),
                is("{\"open_id\":\"XXX\",\"fo..."));
    }
}
