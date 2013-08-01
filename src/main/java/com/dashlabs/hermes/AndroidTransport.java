package com.dashlabs.hermes;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: blangel
 * Date: 8/1/13
 * Time: 11:16 AM
 */
public class AndroidTransport implements Transport<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidTransport.class);

    private final String registrationId;

    private final Sender sender;

    public AndroidTransport(String registrationId, Sender sender) {
        this.registrationId = registrationId;
        this.sender = sender;
    }

    @Override public String getId() {
        return registrationId;
    }

    @Override public void send(Message message, int retries) {
        try {
            sender.send(message, registrationId, retries);
        } catch (IOException ioe) {
            LOG.error("Could not send {}", message.toString(), ioe);
        }
    }
}
