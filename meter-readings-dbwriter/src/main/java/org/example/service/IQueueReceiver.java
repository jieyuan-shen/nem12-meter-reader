package org.example.service;

import org.example.common.MeterReadingMsg;

/**
 * Interface for queue receiver of MeterReadingMsg
 */
public interface IQueueReceiver {

    /**
     * Listen to queue and process messages
     */
    public void listen();
}
