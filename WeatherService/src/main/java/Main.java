import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Scanner;

import static java.lang.String.format;

public class Main {

    public static void main(String[] args) {
        //ключ добавлен в environment variables
        String apiKey = System.getenv("YANDEX_WEATHER_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Ошибка: не задана переменная окружения YANDEX_WEATHER_KEY");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        double lat = readLat(scanner);
        double lon = readLon(scanner);
        int limit = readLimit(scanner);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .header("X-Yandex-Weather-Key", apiKey)
                .uri(URI.create(format(Locale.US, "https://api.weather.yandex.ru/v2/forecast?lat=%.4f&lon=%.4f&limit=%d", lat, lon, limit)))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            processAnswer(response);
        } catch (IOException e) {
            System.out.println("Ошибка сети: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Запрос был прерван");
        }
    }

    /**
     * Обработка полученного от сервиса ответа. Если статус-код != 200, то выводится сообщение об ошибке,
     * иначе выводятся:
     * 1.Тело ответа
     * 2.Текущая температура
     * 3.Рассчитанная средняя температура за заданное количество дней
     *
     * @param response полученный ответ
     */
    private static void processAnswer(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            processStatusCode(statusCode);
        } else {
            String body = response.body();
            System.out.printf("Полученный ответ:%n%s%n", body);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            int temp = jsonObject.getAsJsonObject("fact").get("temp").getAsInt();
            System.out.printf("Текущая температура: %d%n", temp);
            JsonArray forecasts = jsonObject.getAsJsonArray("forecasts");

            int sum = 0;
            int count = 0;
            for (JsonElement o : forecasts) {
                JsonObject partsDay = o.getAsJsonObject()
                        .getAsJsonObject("parts")
                        .getAsJsonObject("day");

                if (partsDay == null) {
                    continue;
                }

                JsonElement tempAvg = partsDay.get("temp_avg");
                if (tempAvg == null) {
                    continue;
                }

                sum += tempAvg.getAsInt();
                count++;
            }
            if (count == 0) {
                System.out.println("Не удалось рассчитать среднюю температуру, не достаточно данных");
            } else {
                long value = Math.round((double) sum / count);
                System.out.printf("Средняя температура на ближайшие %d %s = %d%n", count, getDayWord(count), value);
            }
        }
    }

    /**
     * Метод для склонения слова 'день' в зависимости от переданного количества дней
     *
     * @param count количество дней
     * @return слово 'день' в нужном падеже
     */
    private static String getDayWord(int count) {
        int lastDigit = count % 10;
        int lastTwoDigits = count % 100;

        if (lastDigit == 1 && lastTwoDigits != 11) {
            return "день";
        }
        if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 10 || lastTwoDigits >= 20)) {
            return "дня";
        }
        return "дней";
    }

    /**
     * Вывод сообщения о некорректном статус-коде (только самые распространённые)
     *
     * @param statusCode статус-код ответа
     */
    private static void processStatusCode(int statusCode) {
        switch (statusCode) {
            case 400 -> System.out.println("Неверный запрос");
            case 403 -> System.out.println("Доступ запрещён");
            case 404 -> System.out.println("Населённый пункт не найден (некорректные координаты)");
            case 500 -> System.out.println("Внутренняя ошибка сервера Яндекс.Погоды");
            default -> System.out.println("Неизвестный статус-код: " + statusCode);
        }
    }

    /**
     * Чтение долготы точки. Необходимо учитывать, что долгота может принимать значение от -180 до 180
     *
     * @param scanner для чтения из консоли
     * @return долгота
     */
    private static double readLon(Scanner scanner) {
        while (true) {
            double lon = readDouble(scanner, "Введите долготу (диапазон [-180;180]): ");
            if (lon >= -180 && lon <= 180) {
                return lon;
            }
            System.out.println("Ошибка: долгота должна быть в диапазоне [-180; 180]");
        }
    }

    /**
     * Чтение широты точки. Необходимо учитывать, что широта может принимать значение от -90 до 90
     *
     * @param scanner для чтения из консоли
     * @return широта
     */
    private static double readLat(Scanner scanner) {
        while (true) {
            double lat = readDouble(scanner, "Введите широту (диапазон [-90;90]): ");
            if (lat >= -90 && lat <= 90) {
                return lat;
            }
            System.out.println("Ошибка: широта должна быть в диапазоне [-90; 90]");
        }
    }

    /**
     * Чтение чисел из консоли с предотвращением некорректного ввода.
     *
     * @param scanner для чтения из консоли
     * @param message отображение сообщения
     * @return введенное пользователем значение
     */
    private static double readDouble(Scanner scanner, String message) {
        System.out.print(message);
        while (!scanner.hasNextDouble()) {
            System.out.print(message);
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    /**
     * Чтение из консоли количества дней прогноза
     *
     * @param scanner для чтения из консоли
     * @return введенное пользователем значение
     */
    private static int readLimit(Scanner scanner) {
        while (true) {
            int value = readInt(scanner, "Введите количество дней:");
            if (value > 0) {
                return value;
            }
            System.out.println("Ошибка: число должно быть положительным");
        }
    }

    /**
     * Чтение целых чисел из консоли с предотвращением некорректного ввода.
     *
     * @param scanner для чтения из консоли
     * @param message отображение сообщения
     * @return введенное пользователем значение
     */
    private static int readInt(Scanner scanner, String message) {
        System.out.print(message);
        while (!scanner.hasNextInt()) {
            System.out.print(message);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

}
