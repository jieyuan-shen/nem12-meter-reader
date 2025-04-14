package org.example.service;

import jakarta.annotation.PostConstruct;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.common.MeterReadingMsg;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;

import java.util.List;

@Slf4j
@Service
public class DBWriter {

    // define an ST for batch insert statement
    private final String templateString =
            "INSERT INTO meter_readings (\"nmi\", \"timestamp\", \"consumption\") VALUES\n" +
            "<entries:{e | ('<e.nmi>', '<e.timestamp>', <e.consumption>)}; separator=\",\\n\">;\n";

    @Value
    static class MeterReadingEntry {
        public String nmi;
        public String timestamp;
        public double consumption;
    }

    /**
     * Implementation of DB writer.
     * Print INSERT string for MySQL
     *
     * @param meterReadingMsg
     */
    public void write(MeterReadingMsg meterReadingMsg) {

        List<MeterReadingEntry> entries = meterReadingMsg.getReadings().stream()
                .map(reading -> new MeterReadingEntry(
                        meterReadingMsg.getNmi(), reading.getTimestamp(), reading.getValue()))
                .toList();

        ST st = new ST(templateString);
        st.add("entries", entries);

        // print the batch insert statement
        log.info("\n" + st.render());
    }
}
