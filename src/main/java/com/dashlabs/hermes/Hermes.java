package com.dashlabs.hermes;

import com.google.android.gcm.server.Message;
import com.notnoop.apns.APNS;
import com.notnoop.apns.PayloadBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * User: blangel
 * Date: 8/1/13
 * Time: 9:31 AM
 *
 * Abstracted interface for APNS and GCM.  To start use the appropriate builder method {@link #iOS(Transport)} or {@link #android(Transport)}
 */
public final class Hermes<T> {

    public static iOSBuilder iOS(Transport<String> transport) {
        return new iOSBuilder(transport);
    }

    public static AndroidBuilder android(Transport<Message> transport) {
        return new AndroidBuilder(transport);
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

    public static final class AndroidBuilder extends Builder<AndroidBuilder, Message> {

        private final Transport<Message> transport;

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
        private Boolean delayWhileIdle;

        /**
         * @see {@literal https://developer.android.com/google/gcm/gcm.html}
         */
        private Boolean dryRun;

        private AndroidBuilder(Transport<Message> transport) {
            this.transport = transport;
        }

        public AndroidBuilder timeToLiveSeconds(int timeToLiveSeconds) {
            this.timeToLiveSeconds = timeToLiveSeconds;
            return this;
        }

        public AndroidBuilder restrictedPackageName(String restrictedPackageName) {
            this.restrictedPackageName = restrictedPackageName;
            return this;
        }

        public AndroidBuilder collapseKey(String collapseKey) {
            this.collapseKey = collapseKey;
            return this;
        }

        public AndroidBuilder delayWhileIdle(boolean delayWhileIdle) {
            this.delayWhileIdle = delayWhileIdle;
            return this;
        }

        public AndroidBuilder forDryRun() {
            this.dryRun = true;
            return this;
        }

        @Override public String send(int retries) {
            Hermes<Message> hermes = new Hermes<Message>(Type.Android, transport, body, data, timeToLiveSeconds, restrictedPackageName, collapseKey, delayWhileIdle,
                    dryRun, null, null, null, null, null);
            return hermes.send(retries);
        }

        @Override protected AndroidBuilder getThis() {
            return this;
        }

    }

    public static final class iOSBuilder extends Builder<iOSBuilder, String> {

        private final Transport<String> transport;

        /**
         * @see com.notnoop.apns.PayloadBuilder#sound(String)
         */
        private String sound;

        /**
         * @see com.notnoop.apns.PayloadBuilder#badge(int)
         */
        private Integer badge;

        /**
         * @see com.notnoop.apns.PayloadBuilder#actionKey(String)
         */
        private String actionKey;

        /**
         * @see com.notnoop.apns.PayloadBuilder#forNewsstand()
         */
        private Boolean newsstand;

        /**
         * @see com.notnoop.apns.PayloadBuilder#launchImage(String)
         */
        private String launchImage;

        private iOSBuilder(Transport<String> transport) {
            this.transport = transport;
        }

        public iOSBuilder sound(String sound) {
            this.sound = sound;
            return this;
        }

        public iOSBuilder badge(int badge) {
            this.badge = badge;
            return this;
        }

        public iOSBuilder actionKey(String actionKey) {
            this.actionKey = actionKey;
            return this;
        }

        public iOSBuilder forNewsstand() {
            this.newsstand = true;
            return this;
        }

        public iOSBuilder launchImage(String launchImage) {
            this.launchImage = launchImage;
            return this;
        }

        @Override public String send(int retries) {
            Hermes<String> hermes = new Hermes<String>(Type.iOS, transport, body, data, null, null, null, null, null, sound, badge, actionKey, newsstand, launchImage);
            return hermes.send(retries);
        }

        @Override protected iOSBuilder getThis() {
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

    private final Boolean delayWhileIdle;

    private final Boolean dryRun;

    private final String sound;

    private final Integer badge;

    private final String actionKey;

    private final Boolean newsstand;

    private final String launchImage;

    private Hermes(Type type, Transport<T> transport, String body, Map<String, String> data, Integer timeToLiveSeconds,
                   String restrictedPackageName, String collapseKey, Boolean delayWhileIdle, Boolean dryRun,
                   String sound, Integer badge, String actionKey, Boolean newsstand, String launchImage) {
        this.type = type;
        this.transport = transport;
        this.body = body;
        this.data = data;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.restrictedPackageName = restrictedPackageName;
        this.collapseKey = collapseKey;
        this.delayWhileIdle = delayWhileIdle;
        this.dryRun = dryRun;
        this.sound = sound;
        this.badge = badge;
        this.actionKey = actionKey;
        this.newsstand = newsstand;
        this.launchImage = launchImage;
    }

    public String send(int retries) {
        T message = build();
        return transport.send(message, retries);
    }

    @SuppressWarnings("unchecked")
    private T build() {
        switch (type) {
            case Android:
                return (T) buildAndroid();
            case iOS:
                return (T) buildIOS();
            default:
                throw new AssertionError(String.format("Unknown type %s", (type == null ? "<null>" : type.name())));
        }
    }

    private Message buildAndroid() {
        Message.Builder builder = new Message.Builder();
        if (timeToLiveSeconds != null) {
            builder.timeToLive(timeToLiveSeconds);
        }
        builder.restrictedPackageName(restrictedPackageName);
        builder.collapseKey(collapseKey);
        if (delayWhileIdle != null) {
            builder.delayWhileIdle(delayWhileIdle);
        }
        if (dryRun != null) {
            builder.dryRun(dryRun);
        }
        if (body != null) {
            builder.addData("body", body);
        }
        for (String key : data.keySet()) {
            String value = data.get(key);
            builder.addData(key, value);
        }
        return builder.build();
    }

    private String buildIOS() {
        PayloadBuilder builder = APNS.newPayload();
        builder.sound(sound);
        if (badge != null) {
            builder.badge(badge);
        }
        builder.actionKey(actionKey);
        if ((newsstand != null) && newsstand) {
            builder.forNewsstand();
        }
        builder.launchImage(launchImage);
        if (body != null) {
            builder.alertBody(body);
        }
        builder.customFields(data);
        return builder.build();
    }

}
