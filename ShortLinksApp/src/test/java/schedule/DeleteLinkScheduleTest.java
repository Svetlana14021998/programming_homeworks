package schedule;

import config.AppConfig;
import model.Link;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.LinkRepository;
import repository.impl.LinkInMemoryRepository;
import storage.impl.LinkFileStorageImpl;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Интеграционный тест шедулера очистки")
class DeleteLinkScheduleTest {
    @TempDir
    Path tempDir;

    private String originalFilePath;
    private long originalInterval;
    private TimeUnit originalTimeUnit;

    //изоляция реальных пользовательских данных и ускорения шедулера
    @BeforeEach
    void setUp() throws Exception {
        AppConfig config = AppConfig.getInstance();

        Field linksFileField = AppConfig.class.getDeclaredField("linksFile");
        linksFileField.setAccessible(true);
        originalFilePath = (String) linksFileField.get(config);
        linksFileField.set(config, tempDir.resolve("test_links.json").toString());

        Field intervalField = AppConfig.class.getDeclaredField("cleanupInterval");
        intervalField.setAccessible(true);
        originalInterval = (long) intervalField.get(config);
        intervalField.set(config, 100L);

        Field timeUnitField = AppConfig.class.getDeclaredField("cleanupTimeUnit");
        timeUnitField.setAccessible(true);
        originalTimeUnit = (TimeUnit) timeUnitField.get(config);
        timeUnitField.set(config, TimeUnit.MILLISECONDS);
    }

    //восстановление оригинальных данных
    @AfterEach
    void tearDown() throws Exception {
        AppConfig config = AppConfig.getInstance();

        Field linksFileField = AppConfig.class.getDeclaredField("linksFile");
        linksFileField.setAccessible(true);
        linksFileField.set(config, originalFilePath);

        Field intervalField = AppConfig.class.getDeclaredField("cleanupInterval");
        intervalField.setAccessible(true);
        intervalField.set(config, originalInterval);

        Field timeUnitField = AppConfig.class.getDeclaredField("cleanupTimeUnit");
        timeUnitField.setAccessible(true);
        timeUnitField.set(config, originalTimeUnit);
    }

    @Test
    @DisplayName("Проверка удаления ссылкок по шедулеру")
    void checkDeleteLinksByScheduleTest() throws InterruptedException {
        // given
        LinkFileStorageImpl storage = LinkFileStorageImpl.getInstance();
        LinkRepository repository = new LinkInMemoryRepository(storage);

        Link expired = new Link("http://expired.com", "exp",
                Instant.now().minus(1, ChronoUnit.DAYS),
                5, 5, UUID.randomUUID());
        Link active = new Link("http://active.com", "act",
                Instant.now().plus(1, ChronoUnit.DAYS),
                5, 5, UUID.randomUUID());

        repository.save(expired);
        repository.save(active);

        // when
        DeleteLinkSchedule schedule = new DeleteLinkSchedule(repository);
        schedule.start();

        //чтобы шедулер успел отработать
        Thread.sleep(300);

        // then
        assertTrue(repository.findByCode("exp").isEmpty(),
                "Протухшая ссылка должна быть удалена");
        assertTrue(repository.findByCode("act").isPresent(),
                "Активная ссылка должна остаться");
        schedule.stop();
    }
}