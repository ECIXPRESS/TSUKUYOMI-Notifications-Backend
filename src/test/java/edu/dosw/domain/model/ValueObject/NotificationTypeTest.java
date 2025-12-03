package edu.dosw.domain.model.ValueObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTypeTest {

    @Test
    void enum_ShouldHaveSecurityLoginType() {
        // When
        NotificationType type = NotificationType.SECURITY_LOGIN;

        // Then
        assertNotNull(type);
        assertEquals("SECURITY_LOGIN", type.name());
    }

    @Test
    void enum_ShouldHaveOrderConfirmedType() {
        // When
        NotificationType type = NotificationType.ORDER_CONFIRMED;

        // Then
        assertNotNull(type);
        assertEquals("ORDER_CONFIRMED", type.name());
    }

    @Test
    void enum_ShouldHaveSecurityPasswordResetType() {
        // When
        NotificationType type = NotificationType.SECURITY_PASSWORD_RESET;

        // Then
        assertNotNull(type);
        assertEquals("SECURITY_PASSWORD_RESET", type.name());
    }

    @Test
    void enum_ShouldHaveOrderInPreparationType() {
        // When
        NotificationType type = NotificationType.ORDER_IN_PREPARATION;

        // Then
        assertNotNull(type);
        assertEquals("ORDER_IN_PREPARATION", type.name());
    }

    @Test
    void enum_ShouldHavePaymentCompletedType() {
        // When
        NotificationType type = NotificationType.PAYMENT_COMPLETED;

        // Then
        assertNotNull(type);
        assertEquals("PAYMENT_COMPLETED", type.name());
    }

    @Test
    void enum_ShouldHavePaymentProcessedType() {
        // When
        NotificationType type = NotificationType.PAYMENT_PROCESSED;

        // Then
        assertNotNull(type);
        assertEquals("PAYMENT_PROCESSED", type.name());
    }

    @Test
    void enum_ShouldHavePaymentFailedType() {
        // When
        NotificationType type = NotificationType.PAYMENT_FAILED;

        // Then
        assertNotNull(type);
        assertEquals("PAYMENT_FAILED", type.name());
    }

    @Test
    void enum_ShouldHavePaymentCreatedType() {
        // When
        NotificationType type = NotificationType.PAYMENT_CREATED;

        // Then
        assertNotNull(type);
        assertEquals("PAYMENT_CREATED", type.name());
    }

    @Test
    void enum_ShouldHaveOrderReadyType() {
        // When
        NotificationType type = NotificationType.ORDER_READY;

        // Then
        assertNotNull(type);
        assertEquals("ORDER_READY", type.name());
    }

    @Test
    void enum_ShouldHaveOrderDeliveredType() {
        // When
        NotificationType type = NotificationType.ORDER_DELIVERED;

        // Then
        assertNotNull(type);
        assertEquals("ORDER_DELIVERED", type.name());
    }

    @Test
    void enum_ShouldHaveOrderRefundedType() {
        // When
        NotificationType type = NotificationType.ORDER_REFUNDED;

        // Then
        assertNotNull(type);
        assertEquals("ORDER_REFUNDED", type.name());
    }

    @Test
    void enum_ShouldHaveSellerNewOrderType() {
        // When
        NotificationType type = NotificationType.SELLER_NEW_ORDER;

        // Then
        assertNotNull(type);
        assertEquals("SELLER_NEW_ORDER", type.name());
    }

    @Test
    void enum_ShouldHaveSystemType() {
        // When
        NotificationType type = NotificationType.SYSTEM;

        // Then
        assertNotNull(type);
        assertEquals("SYSTEM", type.name());
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When
        NotificationType type = NotificationType.valueOf("PAYMENT_COMPLETED");

        // Then
        assertEquals(NotificationType.PAYMENT_COMPLETED, type);
    }

    @Test
    void values_ShouldReturnAllTypes() {
        // When
        NotificationType[] types = NotificationType.values();

        // Then
        assertEquals(13, types.length);
    }

    @Test
    void valueOf_ShouldThrowExceptionForInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> NotificationType.valueOf("INVALID_TYPE"));
    }

    @Test
    void enum_DifferentTypesShouldNotBeEqual() {
        // Given
        NotificationType type1 = NotificationType.ORDER_CONFIRMED;
        NotificationType type2 = NotificationType.PAYMENT_COMPLETED;

        // Then
        assertNotEquals(type1, type2);
    }

    @Test
    void enum_SameTypeShouldBeEqual() {
        // Given
        NotificationType type1 = NotificationType.SECURITY_LOGIN;
        NotificationType type2 = NotificationType.SECURITY_LOGIN;

        // Then
        assertEquals(type1, type2);
    }
}

