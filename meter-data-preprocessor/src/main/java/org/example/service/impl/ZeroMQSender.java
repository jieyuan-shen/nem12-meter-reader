package org.example.service.impl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.common.MeterInfo;
import org.example.common.MeterReading;
import org.example.common.MeterReadingMsg;
import org.example.service.IQueueSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;

/**
 * A sample implementation of queue service with https://github.com/OpenHFT/Chronicle-Queue
 */
@Slf4j
@Service
public class ZeroMQSender implements IQueueSender {

    @Value("${zeroMQ.address}")
    private String connectAddr;

    ZContext context;
    ZMQ.Socket pubSocket;

    private ZMQ.Socket aquireSocket() {
        if (pubSocket == null) {
            context = new ZContext();
            pubSocket = context.createSocket(SocketType.PUSH);
            Preconditions.checkArgument(pubSocket.connect(connectAddr),
                    "failed to connect zeroMQ: " + connectAddr);
            log.info("connected to queue: " + connectAddr);
        }
        return pubSocket;
    }

    /**
     * Send meter readings to the queue
     * @param meterInfo meter info (including NMI)
     * @param readings meter readings (including value and timestamp)
     */
    @Override
    public void send(MeterInfo meterInfo, List<MeterReading> readings) {
        MeterReadingMsg msg = new MeterReadingMsg(meterInfo.getNmi(), readings);
        aquireSocket().send(new Gson().toJson(msg));
    }

    /**
     * Close the socket and context at the end of the application
     */
    @PreDestroy
    public void onDestroy() {
        if (pubSocket != null) {
            pubSocket.close();
        }
        if (context != null) {
            context.close();
        }
    }
}
