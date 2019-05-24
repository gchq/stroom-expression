package stroom.dashboard.expression.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

class EncodingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtil.class);
    private static final String CHARSET = "UTF-8";

    static String encodeUrl(final String string) {
        try {
            return URLEncoder.encode(string, CHARSET);
        } catch (final UnsupportedEncodingException e) {
            LOGGER.debug(e.getMessage(), e);
            return string;
        }
    }

    static String decodeUrl(final String string) {
        try {
            return URLEncoder.encode(string, CHARSET);
        } catch (final UnsupportedEncodingException e) {
            LOGGER.debug(e.getMessage(), e);
            return string;
        }
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
