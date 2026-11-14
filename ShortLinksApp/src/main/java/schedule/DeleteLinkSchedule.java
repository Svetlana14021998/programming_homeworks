package schedule;

import config.AppConfig;
import model.Link;
import repository.LinkRepository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Шедулер для удаления ссылок с истекшим сроком
 */
public class DeleteLinkSchedule {
    private final LinkRepository linkRepository;
    private final ScheduledExecutorService scheduler;
    private final AppConfig config;

    public DeleteLinkSchedule(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.config = AppConfig.getInstance();
    }

    /**
     * Запуск шедулера
     */
    public void start() {
        long interval = config.getCleanupInterval();
        TimeUnit unit = config.getCleanupTimeUnit();
        scheduler.scheduleAtFixedRate(this::deleteExpiredLinks, 0, interval, unit);
    }

    /**
     * Остановка шедулера
     */
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * Удаление ссылок с истекшим сроком (срок задается через конфигурацию)
     */
    private void deleteExpiredLinks() {
        Instant now = Instant.now();
        List<Link> allLinks = linkRepository.findAll();

        for (Link link : allLinks) {
            if (now.isAfter(link.getExpiresAt())) {
                linkRepository.deleteByCode(link.getCode());
            }
        }
    }
}
