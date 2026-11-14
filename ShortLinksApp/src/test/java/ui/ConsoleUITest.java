package ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тестирование консольного ввода (ConsoleUI)")
class ConsoleUITest {

    @Test
    @DisplayName("Чтение положительного числа с первой попытки")
    void checkReadPositiveIntTest() {
        //given
        ConsoleUI ui = createUiWithInput("42\n");

        //when
        int result = ui.readPositiveInt("Введите:");

        //then
        assertEquals(42, result);
    }

    @Test
    @DisplayName("Чтение числа после ввода текста (нечисла)")
    void checkReadPositiveIntAfterNonIntegerTest() {
        //given
        ConsoleUI ui = createUiWithInput("abc\n15\n");

        //when
        int result = ui.readPositiveInt("Введите:");

        //then
        assertEquals(15, result);
    }

    @Test
    @DisplayName("Чтение числа после ввода отрицательного значения и нуля")
    void checkReadPositiveIntAfterNegativeAndZeroTest() {
        //given
        ConsoleUI ui = createUiWithInput("-5\n0\n10\n");

        //when
        int result = ui.readPositiveInt("Введите:");

        //then
        assertEquals(10, result);
    }

    @Test
    @DisplayName("Чтение непустой строки")
    void checkReadNotEmptyStringTest() {
        //given
        ConsoleUI ui = createUiWithInput("Hello World\n");

        //when
        String result = ui.readString("Введите:");

        //then
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("Обрезка пробелов по краям строки")
    void checkReadStringAndTrimSpacesTest() {
        //given
        ConsoleUI ui = createUiWithInput("   Hello   \n");

        //when
        String result = ui.readString("Введите:");

        //then
        assertEquals("Hello", result);
    }

    @Test
    @DisplayName("Пропуск пустых строк и строк из пробелов")
    void checkReadStringWithSkipEmptyAndBlankLinesTest() {
        //given
        ConsoleUI ui = createUiWithInput("\n   \nRealText\n");

        //when
        String result = ui.readString("Введите:");

        //then
        assertEquals("RealText", result);
    }

    @Test
    @DisplayName("Чтение разрешенной команды")
    void checkReadValidCommandTest() {
        //given
        ConsoleUI ui = createUiWithInput("/help\n");
        List<String> commands = List.of("/help", "/exit");

        //when
        String result = ui.readCommand("Введите команду:", commands);

        //then
        assertEquals("/help", result);
    }

    @Test
    @DisplayName("Приведение команды к нижнему регистру (если разрешены только строчные)")
    void checkReadCommandWithUpperCaseTest() {
        //given
        ConsoleUI ui = createUiWithInput("/HELP\n");
        List<String> commands = List.of("/help", "/exit");

        //when
        String result = ui.readCommand("Введите команду:", commands);

        //then
        assertEquals("/help", result);
    }

    @Test
    @DisplayName("Пропуск неизвестной команды и чтение следующей")
    void checkReadCommandWithSkipUnknownCommandTest() {
        //given
        ConsoleUI ui = createUiWithInput("/unknown\n/exit\n");
        List<String> commands = List.of("/help", "/exit");

        //when
        String result = ui.readCommand("Введите команду:", commands);

        //then
        assertEquals("/exit", result);
    }

    @Test
    @DisplayName("Пропуск пустых строк при вводе команды")
    void checkReadCommandWithSkipEmptyLinesTest() {
        //given
        ConsoleUI ui = createUiWithInput("\n\n/exit\n");
        List<String> commands = List.of("/help", "/exit");

        //when
        String result = ui.readCommand("Введите команду:", commands);

        //then
        assertEquals("/exit", result);
    }

    @Test
    @DisplayName("Обрезка пробелов по краям команды")
    void checkReadCommandAndTrimSpacesTest() {
        //given
        ConsoleUI ui = createUiWithInput("   /exit   \n");
        List<String> commands = List.of("/help", "/exit");

        //when
        String result = ui.readCommand("Введите команду:", commands);

        //then
        assertEquals("/exit", result);
    }

    private ConsoleUI createUiWithInput(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        return new ConsoleUI(scanner);
    }
}