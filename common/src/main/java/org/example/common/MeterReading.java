package org.example.common;

import lombok.Value;

@Value
public class MeterReading {
    double value;
    String timestamp; // format:  yyyy-MM-dd hh:mm:ss
}
