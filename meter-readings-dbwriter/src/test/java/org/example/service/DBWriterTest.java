package org.example.service;


import org.example.common.MeterReading;
import org.example.common.MeterReadingMsg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DBWriterTest {


    @InjectMocks
    private DBWriter dbWriter;

    @Test
    public void write_EmptyMeterReadingMsg_ShouldLogEmptyStatement() {
        MeterReadingMsg msg = new MeterReadingMsg("nmi1", Collections.emptyList());

        dbWriter.write(msg);
    }

    @Test
    public void write_SingleMeterReading_ShouldLogSingleInsertStatement() {
        MeterReading reading = new MeterReading(100.0, "2023-01-01T00:00:00Z");
        MeterReadingMsg msg = new MeterReadingMsg("nmi1", Arrays.asList(reading));

        dbWriter.write(msg);
    }

    @Test
    public void write_MultipleMeterReadings_ShouldLogMultipleInsertStatements() {
        MeterReading reading1 = new MeterReading(100.0, "2023-01-01T00:00:00Z");
        MeterReading reading2 = new MeterReading(200.0, "2023-01-02T00:00:00Z");
        MeterReadingMsg msg = new MeterReadingMsg("nmi1", Arrays.asList(reading1, reading2));

        dbWriter.write(msg);
    }

    @Test
    public void write_DifferentNmiAndTimestamp_ShouldLogCorrectStatements() {
        MeterReading reading1 = new MeterReading(100.0, "2023-01-01T00:00:00Z");
        MeterReading reading2 = new MeterReading(200.0, "2023-01-02T00:00:00Z");
        MeterReadingMsg msg = new MeterReadingMsg("nmi2", Arrays.asList(reading1, reading2));

        dbWriter.write(msg);
    }
}