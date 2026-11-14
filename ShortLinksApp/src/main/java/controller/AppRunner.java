package controller;

import config.AppConfig;
import helpers.UrlValidator;
import model.Link;
import model.User;
import service.LinkService;
import service.UserService;
import ui.ConsoleUI;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static helpers.LinksHelper.printUserLinkInfo;
import static helpers.LinksHelper.printUserLinksInfo;

public class AppRunner {

    private final LinkService linkService;
    private final UserService userService;
    private final ConsoleUI ui;
    private User currentUser;

    private static final String WELCOME_MESSAGE = """
            ============================================
            Добро пожаловать в приложение коротких ссылок!
            Если у вас уже есть учетная запись, используйте команду /login для входа.
            Если учетной записи нет, то для начала использования воспользуйтесь командой /create-link для создания короткой ссылки и передайте ссылку на ресурс, а также
            количество допустимых переходов по ссылке (пример: /create-link https://www.me-link.ru 10).
            Для выхода из учетной записи используйте команду /logout
            Для завершения работы программы используйте команду /exit
            Для получения справочной информации воспользуйтесь командой /help.
            =============================================
            """;
    private static final String BYE_MESSAGE = "Приложение завершает свою работу. До свидания!";
    private static final String NOT_AUTH = "Вы не авторизованы. Необходимо войти в систему или создать новую ссылку для создания аккаунта";

    private static final String EXIT = "/exit";
    private static final String HELP = "/help";
    private static final String LOGIN = "/login";
    private static final String LOGOUT = "/logout";
    private static final String WHO_AMI = "/who-ami";
    private static final String CREATE_LINK = "/create-link";
    private static final String GO = "/go";
    private static final String MY_LINKS = "/my-links";
    private static final String LINK_INFO = "/link-info";
    private static final String DELETE_LINK = "/delete-link";
    private static final String CHANGE_LINK = "/change-link";

    private static final List<String> AVAILABLE_COMMANDS = List.of(EXIT, HELP, LOGIN, LOGOUT, WHO_AMI, CREATE_LINK, GO,
            MY_LINKS, LINK_INFO, DELETE_LINK, CHANGE_LINK);

    public AppRunner(
            UserService userService,
            LinkService linkService,
            ConsoleUI ui) {
        this.userService = userService;
        this.linkService = linkService;
        this.ui = ui;
        this.currentUser = null;
    }

    /**
     * Основной метод
     */
    public void run() {
        System.out.println(WELCOME_MESSAGE);
        while (true) {
            String command = ui.readCommand("Введите команду. Список доступных команд можно посмотреть через команду /help", AVAILABLE_COMMANDS);
            switch (command) {
                case EXIT:
                    System.out.println(BYE_MESSAGE);
                    return;
                case HELP:
                    printHelpMessage();
                    break;
                case LOGIN:
                    login();
                    break;
                case LOGOUT:
                    logout();
                    break;
                case WHO_AMI:
                    whoAmi();
                    break;
                case CREATE_LINK:
                    createLink();
                    break;
                case GO:
                    clickLink();
                    break;
                case MY_LINKS:
                    showMyLinks();
                    break;
                case LINK_INFO:
                    showLinkInfo();
                    break;
                case DELETE_LINK:
                    deleteLink();
                    break;
                case CHANGE_LINK:
                    changeLink();
                    break;
            }
        }
    }

    /**
     * Изменение лимита для ссылки
     */
    private void changeLink() {
        if (currentUser == null) {
            System.out.println(NOT_AUTH);
            return;
        }
        String link = ui.readString("Введите короткую ссылку, для которой Вы хотите изменить лимит переходов");
        System.out.println("Внимание: после изменения лимита оставшийся лимит будет установлен на новое значение.");
        int newLimit = ui.readPositiveInt("Введите новый лимит переходов (положительное число)");
        linkService.updateLinkLimit(link, newLimit, currentUser.getUuid());
    }

    /**
     * Удаление ссылки
     */
    private void deleteLink() {
        if (currentUser == null) {
            System.out.println(NOT_AUTH);
            return;
        }
        String link = ui.readString("Введите короткую ссылку, которую Вы хотите удалить");
        linkService.deleteLink(link, currentUser.getUuid());
    }

    /**
     * Вывод информации о ссылке
     */
    private void showLinkInfo() {
        if (currentUser == null) {
            System.out.println(NOT_AUTH);
            return;
        }
        String shortLink = ui.readString("Введите короткую ссылку, о которой хотите получить информацию");
        Link link = linkService.getLink(shortLink, currentUser.getUuid());
        printUserLinkInfo(link);
    }

    /**
     * Информация обо всех ссылках пользователя
     */
    private void showMyLinks() {
        if (currentUser == null) {
            System.out.println(NOT_AUTH);
            return;
        }
        List<Link> userLinks = linkService.getUserLinks(currentUser.getUuid());
        printUserLinksInfo(userLinks);
    }

    /**
     * Информация о текущем пользователе
     */
    private void whoAmi() {
        if (currentUser != null) {
            System.out.printf("Текущий пользователь: %s%n", currentUser.getUuid());
        } else {
            System.out.println(NOT_AUTH);
        }
    }

    /**
     * Переход по ссылке
     */
    private void clickLink() {
        if (currentUser == null) {
            System.out.println(NOT_AUTH);
            return;
        }
        String shortLink = ui.readString("Введите короткую ссылку, по которой Вы хотите перейти");
        linkService.openLink(currentUser.getUuid(), shortLink);
    }

    /**
     * Создание ссылки
     */
    private void createLink() {
        String url = ui.readString("Введите url");
        while (!UrlValidator.isValidHttpUrl(url)) {
            url = ui.readString("Введите корректный url");
        }
        int limit = ui.readPositiveInt("Введите лимит переходов по ссылке (положительное число)");
        if (currentUser == null) {
            User newUser = userService.createNewUser();
            currentUser = newUser;
            System.out.printf("Вам был создан аккаунт с UUID = %s. В дальнейшем Вы можете использовать его для входа в приложение%n", currentUser.getUuid());
        }
        Link link = linkService.createLink(currentUser.getUuid(), url, limit);
        String prefix = AppConfig.getInstance().getLinkPrefix();
        System.out.printf("Короткая ссылка %s%n", prefix + link.getCode());
    }

    /**
     * Выход из аккаунта
     */
    private void logout() {
        if (currentUser == null) {
            System.out.println("Вы не авторизованы, поэтому не можете выйти из аккаунта");
            return;
        }
        currentUser = null;
        System.out.println("Вы успешно вышли из аккаунта");
    }

    /**
     * Вход в аккаунт
     */
    private void login() {
        if (currentUser != null) {
            System.out.println("Вы уже вошли в систему. Для смены пользователя сначала необходимо выйти из текущего аккаунта с помощью команды /logout");
            return;
        }
        String uuid = ui.readString("Введите ваш UUID (не может быть пустой строкой)");
        try {
            Optional<User> user = userService.findByUuid(UUID.fromString(uuid));
            if (user.isEmpty()) {
                System.out.println("Пользователь с данным UUID не найден");
            } else {
                currentUser = user.get();
                System.out.printf("Успешно выполнен вход для пользователя %s%n", uuid);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Введен некорректный UUID. Попробуйте еще раз");
        }
    }

    /**
     * Вывод вспомогательной информации
     */
    private void printHelpMessage() {
        String msg = """
                Если у Вас нет аккаунта, используйте команду /create-link для регистрации и создания первой ссылки
                Если у Вас есть аккаунт, для входа используется команда /login для входа
                Узнать Ваш uuid - команда /who-ami
                Выход из аккаунта - /logout
                Завершение работы программы - /exit
                Для перехода по ссылке - /go
                Для просмотра всех ссылок - /my-links
                Для просмотра информации о ссылке - /link-info
                Для удаления ссылки - /delete-link
                Для изменения лимита переходов по ссылке - /change-link
                После ввода команды могут потребоваться дополнительные данные. Просто следуйте инструкциям в терминале
                """;
        System.out.println(msg);
    }
}
