package ui;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@AllArgsConstructor
@Getter
/**
 * Чтение информации из консоли
 */
public class ConsoleUI {

    private final Scanner scanner;

    /**
     * Чтение чисел  из консоли с предотвращением некорректного ввода.
     *
     * @param message отображение сообщения
     * @return введенное пользователем значение
     */

    public int readPositiveInt(String message) {
        System.out.println(message);
        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.println("Необходимо ввести целое число: ");
                scanner.next();
                continue;
            }
            int value = scanner.nextInt();
            scanner.nextLine();

            if (value <= 0) {
                System.out.println("Число должно быть больше 0. Попробуйте еще раз");
                continue;
            }
            return value;
        }
    }

    /**
     * Считывает сообщение из консоли
     *
     * @param message сообщение для вывода пользователю
     * @return строка из консоли
     */
    public String readString(String message) {
        System.out.println(message);
        String str = scanner.nextLine().trim();
        while (str.isEmpty()) {
            System.out.println(message);
            str = scanner.nextLine().trim();
        }
        return str;
    }

    /**
     * Считывает команду из консоли до тех пор, пока не будет введена команда из списка разрешенных
     * команд
     *
     * @param message          сообщение пользователю
     * @param availableCommand список доступных команд
     * @return команда от пользователя
     */
    public String readCommand(String message, List<String> availableCommand) {
        System.out.println(message);
        String str = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        while (!availableCommand.contains(str)) {
            System.out.println(message);
            str = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        }
        return str;
    }
}
