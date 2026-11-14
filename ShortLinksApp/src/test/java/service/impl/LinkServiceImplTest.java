package service.impl;

import model.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.LinkRepository;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса работы со ссылками")
class LinkServiceImplTest {

    @Mock
    private LinkRepository linkRepository;

    private LinkServiceImpl linkService;
    private UUID userId;
    private final String TEST_URL = "http://test.com";
    private final String TEST_CODE = "abc";

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Проверка создания ссылки")
    void checkCreateLinkTest() {
        //given
        when(linkRepository.findByOwnerAndUrl(userId, TEST_URL)).thenReturn(Optional.empty());

        //when
        Link link = linkService.createLink(userId, TEST_URL, 5);

        //then
        assertNotNull(link);
        assertEquals(TEST_URL, link.getOriginalUrl());
        assertEquals(5, link.getLimit());
        assertEquals(5, link.getCurrentLimit());
        assertEquals(userId, link.getOwnerUuid());
        verify(linkRepository, times(1)).save(link);
    }

    @Test
    @DisplayName("Создание короткой ссылки для url, который уже есть у пользователя")
    void checkCreateLinkWhenCurrentUrlAlreadyExistsTest() {
        //given
        Link existing = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 5, userId);
        when(linkRepository.findByOwnerAndUrl(userId, TEST_URL)).thenReturn(Optional.of(existing));

        //when
        Link link = linkService.createLink(userId, TEST_URL, 5);

        //then
        assertEquals(TEST_CODE, link.getCode());
        verify(linkRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка уменьшения доступных переходов по ссылке при переходе по ней")
    void checkCurrentLimitDecreaseTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 2, userId);
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //чтобы не открывать браузер в тесте
        try (MockedStatic<Desktop> mockedDesktop = mockStatic(Desktop.class)) {
            Desktop fakeDesktop = mock(Desktop.class);
            mockedDesktop.when(Desktop::getDesktop).thenReturn(fakeDesktop);

            //when
            linkService.openLink(userId, TEST_CODE);

            //then
            assertEquals(1, link.getCurrentLimit());
            verify(linkRepository, times(1)).save(link);
        }
    }

    @Test
    @DisplayName("Проверка перехода по ссылке, если лимит переходов исчерпан")
    void checkOpenLinkWhenCurrentLimitIsZeroTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 0, userId);
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //when
        linkService.openLink(userId, TEST_CODE);

        //then
        verify(linkRepository, never()).save(link);
    }

    @Test
    @DisplayName("Проверка перехода по ссылке, срок действия которой истек")
    void checkOpenLinkWhenExpiresAtBeforeCurrentTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().minus(1, ChronoUnit.DAYS),
                5, 5, userId);
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //when
        linkService.openLink(userId, TEST_CODE);

        //then
        verify(linkRepository, never()).save(link);
    }

    @Test
    @DisplayName("Проверка перехода по ссылке другого пользователя")
    void checkOpenLinkWhenOwnerUuidIsNotEqualsCurrentUuidTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 5, UUID.randomUUID());
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //when
        linkService.openLink(userId, TEST_CODE);

        //then
        verify(linkRepository, never()).save(link);
    }

    @Test
    @DisplayName("Удаление ссылки")
    void checkDeleteLinkTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 5, userId);
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //when
        linkService.deleteLink(TEST_CODE, userId);

        //then
        verify(linkRepository, times(1)).deleteByCode(TEST_CODE);
    }

    @Test
    @DisplayName("Изменение лимитов у ссылки")
    void checkUpdateLimitTest() {
        //given
        Link link = new Link(TEST_URL, TEST_CODE, Instant.now().plus(1, ChronoUnit.DAYS),
                5, 5, userId);
        when(linkRepository.findByCode(TEST_CODE)).thenReturn(Optional.of(link));

        //when
        linkService.updateLinkLimit(TEST_CODE, 10, userId);

        //then
        assertEquals(10, link.getLimit());
        assertEquals(10, link.getCurrentLimit());
        verify(linkRepository, times(1)).save(link);
    }
}