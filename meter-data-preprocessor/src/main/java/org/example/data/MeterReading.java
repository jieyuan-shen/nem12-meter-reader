package org.example.data;

import lombok.Value;

@Value
public class MeterReading {
    double value;
    long timestamp;
}
