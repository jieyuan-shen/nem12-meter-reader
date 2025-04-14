package org.example.service.impl;


import org.example.common.MeterInfo;
import org.example.common.MeterReading;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ZeroMQSenderTest {

    @Mock
    private ZContext zContext;

    @Mock
    private ZMQ.Socket socket;

    @InjectMocks
    private ZeroMQSender zeroMQSender;

    @Before
    public void setUp() {
        zeroMQSender.connectAddr = "tcp://localhost:5555";
    }

    @Test
    public void send_WhenSocketNotInitialized_ShouldInitializeAndSend() {
        MeterInfo meterInfo = new MeterInfo("nmi1", 1);
        MeterReading reading = new MeterReading(10.0, "2023-01-01T00:00:00Z");
        List<MeterReading> readings = Arrays.asList(reading);

        zeroMQSender.send(meterInfo, readings);

        verify(socket, times(1)).send(any(String.class));
    }

    @Test
    public void send_WhenSocketInitialized_ShouldSendWithoutReinitializing() {
        zeroMQSender.pubSocket = socket;
        when(socket.send(any(String.class))).thenReturn(true);

        MeterInfo meterInfo = new MeterInfo("nmi1", 1);
        MeterReading reading = new MeterReading(10.0, "2023-01-01T00:00:00Z");
        List<MeterReading> readings = Arrays.asList(reading);

        zeroMQSender.send(meterInfo, readings);

        verify(socket, times(1)).send(any(String.class));
    }
}