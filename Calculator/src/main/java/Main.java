import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String WELCOME_MESSAGE = "Добро пожаловать в приложение калькулятор!" +
            "\nДоступные операции: + сложение, - вычитание, / деление, * умножение." +
            "\nДля выхода из калькулятора вместо выбора действия введите S" +
            "\nДля сброса результата введите C";

    private static final List<Character> AVAILABLE_OPERATIONS = List.of('-', '+', '/', '*');

    private static final String INCORRECT_OPERATION = "Выберите доступную операцию:" +
            "+ сложение, - вычитание, / деление, * умножение";

    private static final char EXIT_COMMAND = 'S';

    private static final char RESET_COMMAND = 'C';

    public static void main(String[] args) {
        System.out.println(WELCOME_MESSAGE);

        Scanner scanner = new Scanner(System.in);
        double firstOperand = readDouble(scanner, "Первое число:");
        while (true) {
            char operation = readOperation(scanner);

            if (operation == EXIT_COMMAND) {
                System.out.println("Завершение работы калькулятора");
                break;
            }
            if (operation == RESET_COMMAND) {
                firstOperand = readDouble(scanner, "Первое число:");
                continue;
            }
            double secondOperand = readDouble(scanner, "Второе число:");
            try {
                double result = calculate(firstOperand, secondOperand, operation);
                printResult(result);
                firstOperand = result;
            } catch (ArithmeticException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Запрашивает операцию до тех пор, пока не будет введена корректная операция,
     * команда выхода или команда сброса.
     */
    private static char readOperation(Scanner scanner) {
        while (true) {
            System.out.print("Операция: ");
            char operation = Character.toUpperCase(scanner.next().charAt(0));

            if (operation == EXIT_COMMAND || operation == RESET_COMMAND
                    || AVAILABLE_OPERATIONS.contains(operation)) {
                return operation;
            }
            System.out.println(INCORRECT_OPERATION);
        }
    }

    /**
     * Вычисляет значение выражения
     * Метод всегда вызывается с операцией из {@link #AVAILABLE_OPERATIONS},
     * так как {@link #readOperation(Scanner)} отбрасывает все остальные значения.
     * Ветка default недостижима при корректном использовании,
     * но обязательна для switch-выражения по типу char.
     *
     * @param firstOperand  первый операнд
     * @param secondOperand второй операнд
     * @param operation     выполняемое действие
     * @return результат вычисления
     * @throws ArithmeticException      если выполняется деление на ноль
     * @throws IllegalArgumentException если введена неподдерживаемая операция
     */
    private static double calculate(double firstOperand, double secondOperand, char operation) {
        return switch (operation) {
            case '+' -> firstOperand + secondOperand;
            case '-' -> firstOperand - secondOperand;
            case '/' -> {
                if (secondOperand == 0) {
                    throw new ArithmeticException("Деление на ноль невозможно");
                }
                yield firstOperand / secondOperand;
            }
            case '*' -> firstOperand * secondOperand;
            default -> throw new IllegalArgumentException("Неподдерживаемый тип операции: " + operation);
        };
    }

    /**
     * Отображает результат без дробной части для целых чисел.
     *
     * @param result результат вычисления
     */
    private static void printResult(double result) {
        String value = (result == (long) result)
                ? String.valueOf((long) result)
                : String.valueOf(result);
        String message = "Результат: " + value;
        System.out.println(message);
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
}
