package com.dashlabs.hermes;

import com.dashlabs.hermes.firebase.MessageWrapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import java.util.concurrent.ExecutionException;

/**
 * User: blangel
 * Date: 5/1/18
 * Time: 9:47 AM
 */
public class AndroidFirebaseTransport implements Transport<MessageWrapper> {

    private final String registrationId;

    private final FirebaseMessaging messaging;

    public AndroidFirebaseTransport(String registrationId, FirebaseMessaging messaging) {
        this.registrationId = registrationId;
        this.messaging = messaging;
    }

    @Override public String getId() {
        return registrationId;
    }

    @Override public String send(MessageWrapper wrapper, int retries) {
        wrapper.getBuilder().setToken(registrationId);
        Message message = wrapper.getBuilder().build();
        try {
            return messaging.sendAsync(message, wrapper.isDryRun()).get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee.getCause());
        }
    }
}
