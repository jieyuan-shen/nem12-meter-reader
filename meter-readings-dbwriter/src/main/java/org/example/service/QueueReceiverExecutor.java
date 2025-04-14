package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class QueueReceiverExecutor implements ApplicationRunner {

    IQueueReceiver queueReceiver;

    QueueReceiverExecutor(IQueueReceiver queueReceiver) {
        this.queueReceiver = queueReceiver;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        queueReceiver.listen();
    }
}
