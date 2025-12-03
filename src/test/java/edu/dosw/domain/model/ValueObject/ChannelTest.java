package edu.dosw.domain.model.ValueObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTest {

    @Test
    void enum_ShouldHaveEmailChannel() {
        // When
        Channel channel = Channel.EMAIL;

        // Then
        assertNotNull(channel);
        assertEquals("EMAIL", channel.name());
    }

    @Test
    void enum_ShouldHavePushChannel() {
        // When
        Channel channel = Channel.PUSH;

        // Then
        assertNotNull(channel);
        assertEquals("PUSH", channel.name());
    }

    @Test
    void enum_ShouldHaveInAppChannel() {
        // When
        Channel channel = Channel.IN_APP;

        // Then
        assertNotNull(channel);
        assertEquals("IN_APP", channel.name());
    }

    @Test
    void enum_ShouldHaveSMSChannel() {
        // When
        Channel channel = Channel.SMS;

        // Then
        assertNotNull(channel);
        assertEquals("SMS", channel.name());
    }

    @Test
    void enum_ShouldHaveWebSocketChannel() {
        // When
        Channel channel = Channel.WEB_SOCKET;

        // Then
        assertNotNull(channel);
        assertEquals("WEB_SOCKET", channel.name());
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When
        Channel channel = Channel.valueOf("EMAIL");

        // Then
        assertEquals(Channel.EMAIL, channel);
    }

    @Test
    void values_ShouldReturnAllChannels() {
        // When
        Channel[] channels = Channel.values();

        // Then
        assertEquals(5, channels.length);
        assertArrayEquals(new Channel[]{
                Channel.EMAIL,
                Channel.PUSH,
                Channel.IN_APP,
                Channel.SMS,
                Channel.WEB_SOCKET
        }, channels);
    }

    @Test
    void valueOf_ShouldThrowExceptionForInvalidValue() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Channel.valueOf("INVALID"));
    }

    @Test
    void enum_ShouldBeComparable() {
        // Given
        Channel email = Channel.EMAIL;
        Channel push = Channel.PUSH;

        // Then
        assertNotEquals(email, push);
    }
}

