package za.co.int216d.carwash.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import za.co.int216d.carwash.booking.notification.domain.Notification;
import za.co.int216d.carwash.booking.notification.repository.NotificationRepository;
import za.co.int216d.carwash.booking.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
            .id(1L)
            .clientId(100L)
            .type(Notification.NotificationType.MEMBERSHIP_SUBSCRIBED)
            .title("Welcome!")
            .message("You have subscribed to Premium plan")
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void testCreateNotification_Success() {
        when(notificationRepository.save(any())).thenReturn(testNotification);

        var result = notificationService.createNotification(
            100L,
            Notification.NotificationType.MEMBERSHIP_SUBSCRIBED,
            "Welcome!",
            "You have subscribed to Premium plan",
            "Plan ID: 1"
        );

        assertNotNull(result);
        assertEquals("Welcome!", result.getTitle());
        assertEquals(false, result.getIsRead());

        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void testGetUnreadNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(Arrays.asList(testNotification));

        when(notificationRepository.findUnreadByClientId(100L, pageable)).thenReturn(page);

        var result = notificationService.getUnreadNotifications(100L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Welcome!", result.getContent().get(0).getTitle());

        verify(notificationRepository, times(1)).findUnreadByClientId(100L, pageable);
    }

    @Test
    void testGetAllNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(Arrays.asList(testNotification));

        when(notificationRepository.findByClientIdOrderByCreatedAtDesc(100L, pageable)).thenReturn(page);

        var result = notificationService.getAllNotifications(100L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(notificationRepository, times(1)).findByClientIdOrderByCreatedAtDesc(100L, pageable);
    }

    @Test
    void testGetUnreadCount_Success() {
        when(notificationRepository.countUnreadByClientId(100L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(100L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countUnreadByClientId(100L);
    }

    @Test
    void testMarkAsRead_Success() {
        testNotification.setIsRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenReturn(testNotification);

        notificationService.markAsRead(1L);

        assertTrue(testNotification.getIsRead());
        assertNotNull(testNotification.getReadAt());

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void testMarkAllAsRead_Success() {
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);

        when(notificationRepository.findUnreadByClientId(eq(100L), any(Pageable.class))).thenReturn(page);

        notificationService.markAllAsRead(100L);

        assertTrue(testNotification.getIsRead());
        verify(notificationRepository, times(1)).findUnreadByClientId(eq(100L), any(Pageable.class));
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void testDeleteNotification_Success() {
        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }
}
