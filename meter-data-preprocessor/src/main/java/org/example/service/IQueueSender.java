package org.example.service;

import org.example.common.MeterInfo;
import org.example.common.MeterReading;

import java.util.List;

/**
 * Interface for queue sender
 */
public interface IQueueSender {

    /**
     * Send meter readings to queue
     * @param meterInfo meter info (including NMI)
     * @param readings meter readings (including value and timestamp)
     */
    public void send(MeterInfo meterInfo, List<MeterReading> readings);
}
