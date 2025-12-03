package edu.dosw.domain.model.ValueObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationStatusTest {

    @Test
    void enum_ShouldHavePendingStatus() {
        // When
        NotificationStatus status = NotificationStatus.PENDING;

        // Then
        assertNotNull(status);
        assertEquals("PENDING", status.name());
    }

    @Test
    void enum_ShouldHaveSentStatus() {
        // When
        NotificationStatus status = NotificationStatus.SENT;

        // Then
        assertNotNull(status);
        assertEquals("SENT", status.name());
    }

    @Test
    void enum_ShouldHaveReadStatus() {
        // When
        NotificationStatus status = NotificationStatus.READ;

        // Then
        assertNotNull(status);
        assertEquals("READ", status.name());
    }

    @Test
    void enum_ShouldHaveFailedStatus() {
        // When
        NotificationStatus status = NotificationStatus.FAILED;

        // Then
        assertNotNull(status);
        assertEquals("FAILED", status.name());
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When
        NotificationStatus status = NotificationStatus.valueOf("SENT");

        // Then
        assertEquals(NotificationStatus.SENT, status);
    }

    @Test
    void values_ShouldReturnAllStatuses() {
        // When
        NotificationStatus[] statuses = NotificationStatus.values();

        // Then
        assertEquals(4, statuses.length);
        assertArrayEquals(new NotificationStatus[]{
                NotificationStatus.PENDING,
                NotificationStatus.SENT,
                NotificationStatus.READ,
                NotificationStatus.FAILED
        }, statuses);
    }

    @Test
    void valueOf_ShouldThrowExceptionForInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> NotificationStatus.valueOf("INVALID"));
    }

    @Test
    void enum_StatusesShouldNotBeEqual() {
        // Given
        NotificationStatus pending = NotificationStatus.PENDING;
        NotificationStatus sent = NotificationStatus.SENT;

        // Then
        assertNotEquals(pending, sent);
    }

    @Test
    void enum_SameStatusShouldBeEqual() {
        // Given
        NotificationStatus status1 = NotificationStatus.SENT;
        NotificationStatus status2 = NotificationStatus.SENT;

        // Then
        assertEquals(status1, status2);
    }
}

