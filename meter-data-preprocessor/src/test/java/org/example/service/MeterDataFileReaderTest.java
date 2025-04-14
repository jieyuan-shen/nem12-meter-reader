package org.example.service;


import org.example.common.MeterInfo;
import org.example.common.MeterReading;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeterDataFileReaderTest {

    @Mock
    private IQueueSender queueSender;

    @InjectMocks
    private MeterDataFileReader meterDataFileReader;

    @Before
    public void setUp() {
        // 如果需要，可以在此处进行任何设置
    }

    @Test
    public void decodeMeterInfo_ValidInput_ReturnsMeterInfo() {
        String line = "200,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,15,1234567890,1234567890";
        MeterInfo meterInfo = meterDataFileReader.decodeMeterInfo(line);
        assertNotNull(meterInfo);
        assertEquals("1234567890", meterInfo.getNmi());
        assertEquals(15, meterInfo.getIntervalLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeMeterInfo_InsufficientFields_ThrowsException() {
        String line = "200,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,15";
        meterDataFileReader.decodeMeterInfo(line);
    }

    @Test(expected = NumberFormatException.class)
    public void decodeMeterInfo_InvalidIntervalLength_ThrowsException() {
        String line = "200,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,1234567890,invalid,1234567890,1234567890";
        meterDataFileReader.decodeMeterInfo(line);
    }

    @Test
    public void processReading_ValidInput_SendsReadings() {
        String line = "300,20230101,10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,90.0,100.0,110.0,120.0,130.0,140.0,150.0,160.0,170.0,180.0,190.0,200.0,210.0,220.0,230.0,240.0";
        MeterInfo meterInfo = new MeterInfo("1234567890", 96);
        meterDataFileReader.processReading(line, meterInfo);

        ArgumentCaptor<MeterInfo> meterInfoCaptor = ArgumentCaptor.forClass(MeterInfo.class);
        ArgumentCaptor<List<MeterReading>> readingsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(queueSender, times(1)).send(meterInfoCaptor.capture(), readingsCaptor.capture());

        assertEquals("1234567890", meterInfoCaptor.getValue().getNmi());
        assertEquals(96, meterInfoCaptor.getValue().getIntervalLength());

        List<MeterReading> readings = readingsCaptor.getValue();
        assertNotNull(readings);
        assertEquals(15, readings.size());

        DateTimeFormatter timestampFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime expectedDateTime = new DateTime(2023, 1, 1, 0, 0);
        for (int i = 0; i < readings.size(); i++) {
            MeterReading reading = readings.get(i);
            assertEquals((i + 1) * 10.0, reading.getValue(), 0.01);
            assertEquals(timestampFormatter.print(expectedDateTime), reading.getTimestamp());
            expectedDateTime = expectedDateTime.plusMinutes(96);
        }
    }

    @Test
    public void processReading_InsufficientFields_ThrowsException() {
        String line = "300,20230101,10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,90.0,100.0,110.0,120.0,130.0,140.0,150.0,160.0,170.0,180.0,190.0,200.0,210.0,220.0,230.0";
        MeterInfo meterInfo = new MeterInfo("1234567890", 15);
        meterDataFileReader.processReading(line, meterInfo);
    }

    @Test
    public void processReading_InvalidReadingValue_ThrowsException() {
        String line = "300,20230101,10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,90.0,100.0,110.0,120.0,130.0,140.0,150.0,160.0,170.0,180.0,190.0,200.0,210.0,220.0,230.0,-1.0";
        MeterInfo meterInfo = new MeterInfo("1234567890", 15);
        meterDataFileReader.processReading(line, meterInfo);
    }

    @Test
    public void processReading_InvalidDate_ThrowsException() {
        String line = "300,invalid,10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,90.0,100.0,110.0,120.0,130.0,140.0,150.0,160.0,170.0,180.0,190.0,200.0,210.0,220.0,230.0,240.0";
        MeterInfo meterInfo = new MeterInfo("1234567890", 15);
        meterDataFileReader.processReading(line, meterInfo);
    }
}