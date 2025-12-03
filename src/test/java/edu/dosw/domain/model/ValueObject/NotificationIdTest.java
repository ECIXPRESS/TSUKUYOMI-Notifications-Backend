package edu.dosw.domain.model.ValueObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationIdTest {

    @Test
    void constructor_ShouldCreateNotificationIdWithValue() {
        // Given
        String value = "notif-12345";

        // When
        NotificationId notificationId = new NotificationId(value);

        // Then
        assertNotNull(notificationId);
        assertEquals("notif-12345", notificationId.getValue());
    }

    @Test
    void constructor_ShouldHandleUUIDFormat() {
        // Given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        NotificationId notificationId = new NotificationId(uuid);

        // Then
        assertEquals(uuid, notificationId.getValue());
    }

    @Test
    void setValue_ShouldUpdateValue() {
        // Given
        NotificationId notificationId = new NotificationId("old-id");

        // When
        notificationId.setValue("new-id");

        // Then
        assertEquals("new-id", notificationId.getValue());
    }

    @Test
    void equals_ShouldReturnTrueForSameValue() {
        // Given
        NotificationId id1 = new NotificationId("notif-123");
        NotificationId id2 = new NotificationId("notif-123");

        // Then
        assertEquals(id1, id2);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        // Given
        NotificationId id1 = new NotificationId("notif-123");
        NotificationId id2 = new NotificationId("notif-456");

        // Then
        assertNotEquals(id1, id2);
    }

    @Test
    void hashCode_ShouldBeSameForEqualObjects() {
        // Given
        NotificationId id1 = new NotificationId("notif-123");
        NotificationId id2 = new NotificationId("notif-123");

        // Then
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void toString_ShouldContainValue() {
        // Given
        NotificationId notificationId = new NotificationId("notif-123");

        // When
        String toString = notificationId.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("notif-123"));
    }

    @Test
    void constructor_ShouldHandleNullValue() {
        // When
        NotificationId notificationId = new NotificationId(null);

        // Then
        assertNull(notificationId.getValue());
    }

    @Test
    void constructor_ShouldHandleEmptyString() {
        // When
        NotificationId notificationId = new NotificationId("");

        // Then
        assertEquals("", notificationId.getValue());
    }
}

