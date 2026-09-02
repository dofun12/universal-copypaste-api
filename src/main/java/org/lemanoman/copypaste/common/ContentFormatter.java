package org.lemanoman.copypaste.common;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns plain-text message content into a safe HTML fragment: the raw text is
 * HTML-escaped first (to prevent XSS), then bare URLs are turned into
 * clickable links, and URLs that look like images are embedded as
 * {@code <img>} tags instead.
 */
@Component
public class ContentFormatter {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(
            "\\.(png|jpe?g|gif|webp|svg|bmp)(\\?.*)?$",
            Pattern.CASE_INSENSITIVE);

    public String render(String rawContent) {
        String escaped = HtmlUtils.htmlEscape(rawContent, "UTF-8");
        Matcher matcher = URL_PATTERN.matcher(escaped);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String url = matcher.group(1);
            String replacement = isImageUrl(url)
                    ? "<img src=\"" + url + "\" alt=\"shared image\" class=\"img-fluid rounded shared-image\">"
                    : "<a href=\"" + url + "\" target=\"_blank\" rel=\"noopener noreferrer\">" + url + "</a>";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private boolean isImageUrl(String url) {
        // The URL has already been HTML-escaped, so strip a possible trailing "&amp;..." query
        // safely by matching the extension right before an optional query string.
        return IMAGE_EXTENSION_PATTERN.matcher(url).find();
    }
}
