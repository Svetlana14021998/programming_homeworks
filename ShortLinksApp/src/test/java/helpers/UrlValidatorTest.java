package helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Проверка валидатора URL")
class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com",
            "https://www.baeldung.com/java-9-http-client",
            "http://localhost:8080/path",
            "HTTP://EXAMPLE.COM",
            "HtTpS://example.com"
    })
    @DisplayName("Валидные HTTP/HTTPS URL возвращают true")
    void checkValidDataTest(String url) {
        assertTrue(UrlValidator.isValidHttpUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://example.com",
            "file:///path/to/file",
            "mailto:test@example.com",
            "javascript:alert(1)"
    })
    @DisplayName("Не http/https возвращают false")
    void checkNotHttpOrHttpsTest(String url) {
        assertFalse(UrlValidator.isValidHttpUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://",
            "https://",
            "http:///path",
            "https:///path"
    })
    @DisplayName("HTTP/HTTPS без хоста возвращают false")
    void checkHttpAndHttpsWithoutHostTest(String url) {
        assertFalse(UrlValidator.isValidHttpUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "just_a_string",
            "example.com",
            "//example.com",
            "/path/to/page"
    })
    @DisplayName("Строки без схемы возвращают false")
    void checkUrlWithoutSchemeTest(String url) {
        assertFalse(UrlValidator.isValidHttpUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com/hello world",
            "http://example.com/^",
            "http://example.com/|"
    })
    @DisplayName("Синтаксически невалидные URL возвращают false")
    void checkNotValidUrlTest(String url) {
        assertFalse(UrlValidator.isValidHttpUrl(url));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("null, пустая строка и пробелы возвращают false")
    void checkEmptyDataTest(String url) {
        assertFalse(UrlValidator.isValidHttpUrl(url));
    }
}