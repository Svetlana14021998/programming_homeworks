package helpers;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Вспомогательный класс для валидации корректности url
 */
public class UrlValidator {

    /**
     * Проверяет, является ли строка валидным HTTP/HTTPS URL.
     *
     * @param urlString проверяемая строка
     * @return true, если строка является валидным http/https URL, иначе false
     */
    public static boolean isValidHttpUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(urlString);
            String scheme = uri.getScheme();

            if (scheme == null) {
                return false;
            }

            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null;

        } catch (URISyntaxException e) {
            return false;
        }
    }
}