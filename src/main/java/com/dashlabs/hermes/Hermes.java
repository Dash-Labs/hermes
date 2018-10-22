package com.dashlabs.hermes;

import com.dashlabs.hermes.firebase.MessageWrapper;
import com.google.firebase.messaging.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User: blangel
 * Date: 8/1/13
 * Time: 9:31 AM
 *
 * Abstracted interface for APNS and GCM.
 */
public final class Hermes<T> {

    public static FirebaseBuilder androidFirebase(Transport<MessageWrapper> transport) {
        return new FirebaseBuilder(Type.AndroidFirebase, transport);
    }

    public static FirebaseBuilder iOSFirebase(Transport<MessageWrapper> transport, String title) {
        FirebaseBuilder firebaseBuilder = new FirebaseBuilder(Type.iOSFirebase, transport);
        firebaseBuilder.title(title);
        return firebaseBuilder;
    }

    private static abstract class Builder<T extends Builder<T, H>, H> {

        protected final Map<String, String> data;

        protected String body;

        protected Builder() {
            this.data = new HashMap<String, String>();
        }

        public T withData(String key, String value) {
            this.data.put(key, value);
            return getThis();
        }

        public T body(String body) {
            this.body = body;
            return getThis();
        }

        protected abstract T getThis();

        /**
         * @param retries number of attempts to send on failure
         * @return the payload of the generate message
         */
        public abstract String send(int retries);

    }

    public static final class FirebaseBuilder extends Builder<FirebaseBuilder, MessageWrapper> {

        private final Type type;

        private final Transport<MessageWrapper> transport;

        /**
         * @see {@literal https://developer.android.com/google/gcm/gcm.html}
         */
        private Integer timeToLiveSeconds;

        /**
         * @see {@literal https://developer.android.com/google/gcm/gcm.html}
         */
        private String restrictedPackageName;

        /**
         * @see {@literal https://developer.android.com/google/gcm/gcm.html}
         */
        private String collapseKey;

        /**
         * @see {@literal https://developer.android.com/google/gcm/gcm.html}
         */
        private Boolean dryRun;

        private String title;

        private FirebaseBuilder(Type type, Transport<MessageWrapper> transport) {
            this.type = type;
            this.transport = transport;
        }

        public FirebaseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public FirebaseBuilder timeToLiveSeconds(int timeToLiveSeconds) {
            this.timeToLiveSeconds = timeToLiveSeconds;
            return this;
        }

        public FirebaseBuilder restrictedPackageName(String restrictedPackageName) {
            this.restrictedPackageName = restrictedPackageName;
            return this;
        }

        public FirebaseBuilder collapseKey(String collapseKey) {
            this.collapseKey = collapseKey;
            return this;
        }

        public FirebaseBuilder forDryRun() {
            this.dryRun = true;
            return this;
        }

        @Override public String send(int retries) {
            Hermes<MessageWrapper> hermes = new Hermes<MessageWrapper>(type, transport, body, data, timeToLiveSeconds, restrictedPackageName, collapseKey,
                    dryRun, title);
            return hermes.send(retries);
        }

        @Override protected FirebaseBuilder getThis() {
            return this;
        }

    }

    private final Type type;

    private final Transport<T> transport;

    private final String body;

    private final Map<String, String> data;

    private final Integer timeToLiveSeconds;

    private final String restrictedPackageName;

    private final String collapseKey;

    private final Boolean dryRun;

    private final String title;

    private Hermes(Type type, Transport<T> transport, String body, Map<String, String> data, Integer timeToLiveSeconds,
                   String restrictedPackageName, String collapseKey, Boolean dryRun, String title) {
        this.type = type;
        this.transport = transport;
        this.body = body;
        this.data = data;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.restrictedPackageName = restrictedPackageName;
        this.collapseKey = collapseKey;
        this.dryRun = dryRun;
        this.title = title;
    }

    public String send(int retries) {
        T message = build();
        return transport.send(message, retries);
    }

    @SuppressWarnings("unchecked")
    private T build() {
        switch (type) {
            case AndroidFirebase:
                return (T) buildAndroidFirebase();
            case iOSFirebase:
                return (T) buildIOSFirebase(title);
            default:
                throw new AssertionError(String.format("Unknown type %s", (type == null ? "<null>" : type.name())));
        }
    }

    private MessageWrapper buildAndroidFirebase() {
        com.google.firebase.messaging.Message.Builder builder = com.google.firebase.messaging.Message.builder();
        AndroidConfig.Builder androidConfig = AndroidConfig.builder();
        if (timeToLiveSeconds != null) {
            // firebase uses milliseconds
            androidConfig.setTtl(timeToLiveSeconds.longValue() * 1000L);
        }
        androidConfig.setRestrictedPackageName(restrictedPackageName);
        androidConfig.setCollapseKey(collapseKey);
        builder.setAndroidConfig(androidConfig.build());
        if (body != null) {
            builder.putData("body", body);
        }
        for (String key : data.keySet()) {
            String value = data.get(key);
            builder.putData(key, value);
        }
        return new MessageWrapper(builder, (dryRun != null ? dryRun : false));
    }

    private MessageWrapper buildIOSFirebase(String title) {
        com.google.firebase.messaging.Message.Builder builder = com.google.firebase.messaging.Message.builder();
        ApnsConfig.Builder apnsConfig = ApnsConfig.builder();
        apnsConfig.putHeader("apns-priority", "10");
        apnsConfig.setAps(Aps.builder()
                .setAlert(ApsAlert.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build());
        builder.setApnsConfig(apnsConfig.build());
        for (String key : data.keySet()) {
            String value = data.get(key);
            builder.putData(key, value);
        }
        return new MessageWrapper(builder, (dryRun != null ? dryRun : false));
    }

}
