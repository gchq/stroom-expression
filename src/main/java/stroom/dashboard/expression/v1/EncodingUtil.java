package stroom.dashboard.expression.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class EncodingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtil.class);

    static String encodeUrl(final String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    static String decodeUrl(final String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    static String encodeUri(final String string) {
        try {
            return new URI(string).getRawPath();
        } catch (final URISyntaxException e) {
            LOGGER.debug(e.getMessage(), e);
            return string;
        }
    }
}
