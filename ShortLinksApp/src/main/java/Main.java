import controller.AppRunner;
import repository.LinkRepository;
import repository.UserRepository;
import repository.impl.LinkInMemoryRepository;
import repository.impl.UserInMemoryRepository;
import schedule.DeleteLinkSchedule;
import service.LinkService;
import service.UserService;
import service.impl.LinkServiceImpl;
import service.impl.UserServiceImpl;
import storage.LinkStorage;
import storage.UserStorage;
import storage.impl.LinkFileStorageImpl;
import storage.impl.UserFileStorageImpl;
import ui.ConsoleUI;

import java.io.UncheckedIOException;
import java.util.Scanner;

public class Main {

    private static final String CANT_RUN_APP =
            "Приложение не может быть запущено в данный момент. Свяжитесь с поддержкой";

    public static void main(String[] args) {
        DeleteLinkSchedule deleteLinkSchedule = null;
        try (Scanner scanner = new Scanner(System.in)) {
            UserStorage userStorage = UserFileStorageImpl.getInstance();
            LinkStorage linkStorage = LinkFileStorageImpl.getInstance();

            UserRepository userRepository = new UserInMemoryRepository(userStorage);
            LinkRepository linkRepository = new LinkInMemoryRepository(linkStorage);

            deleteLinkSchedule = new DeleteLinkSchedule(linkRepository);
            deleteLinkSchedule.start();

            UserService userService = new UserServiceImpl(userRepository);
            LinkService linkService = new LinkServiceImpl(linkRepository);
            ConsoleUI ui = new ConsoleUI(scanner);
            AppRunner runner = new AppRunner(userService, linkService, ui);
            runner.run();

        } catch (UncheckedIOException | IllegalStateException e) {
            System.err.println(CANT_RUN_APP);
        } finally {
            if (deleteLinkSchedule != null) {
                deleteLinkSchedule.stop();
            }
        }
    }
}
