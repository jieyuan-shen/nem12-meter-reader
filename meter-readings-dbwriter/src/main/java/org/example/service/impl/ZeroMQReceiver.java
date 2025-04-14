package org.example.service.impl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.common.MeterReadingMsg;
import org.example.service.DBWriter;
import org.example.service.IQueueReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Implemenation of IQueueReceiver with ZeroMQ
 */
@Slf4j
@Service
public class ZeroMQReceiver implements IQueueReceiver {

    @Value("${zeroMQ.address}")
    private String bindAddr;

    @Autowired
    DBWriter dbWriter;

    @Override
    public void listen() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            Preconditions.checkArgument(socket.bind(bindAddr), "failed to bind address: " + bindAddr);
            log.info("bound to queue: " + bindAddr);

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                log.debug("Received: " + message);

                dbWriter.write(new Gson().fromJson(message, MeterReadingMsg.class));
            }
        }
        catch (Throwable t) {
            log.error("Failed to receive and handle message from queue", t);
        }
    }
}
