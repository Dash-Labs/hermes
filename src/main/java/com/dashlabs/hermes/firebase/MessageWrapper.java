package com.dashlabs.hermes.firebase;

import com.google.firebase.messaging.Message;

/**
 * User: blangel
 * Date: 5/1/18
 * Time: 9:51 AM
 */
public class MessageWrapper {

    private final Message.Builder builder;

    private final boolean dryRun;

    public MessageWrapper(Message.Builder builder, boolean dryRun) {
        this.builder = builder;
        this.dryRun = dryRun;
    }

    public Message.Builder getBuilder() {
        return builder;
    }

    public boolean isDryRun() {
        return dryRun;
    }
}
