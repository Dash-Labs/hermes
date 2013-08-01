package com.dashlabs.hermes;

/**
 * User: blangel
 * Date: 8/1/13
 * Time: 11:04 AM
 */
public interface Transport<T> {

    /**
     * @return the device or registration id
     */
    String getId();

    /**
     * @param message to send on the underlying transport
     * @param retries number of times to attempt retry on failure
     * @return the payload of the sent message
     */
    String send(T message, int retries);

}
