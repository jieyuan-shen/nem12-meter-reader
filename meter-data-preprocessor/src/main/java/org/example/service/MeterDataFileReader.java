package org.example.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.data.MeterInfo;
import org.example.data.MeterReading;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class MeterDataFileReader implements ApplicationRunner {

    // In this implementation, meter data file is located in local storage
    // This can be expanded to other meter data file sources according to practical needs
    @Value("${filePath}")
    private String filePath;

    // Only 200 record and 300 record are in interest in this project
    private final String PREFIX_200_RECORD = "200";
    private final String PREFIX_300_RECORD = "300";

    private final int REQ_SIZE_IN_200_RECORD = 10;
    private final int IDX_NMI_IN_200_RECORD = 1;
    private final int IDX_INTERVAL_LENGTH_IN_200_RECORD = 8;

    private final int MINUTES_PER_DAY = 1440;

    /**
     * Read file and process meter data
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Check file patch exists
        Preconditions.checkArgument(StringUtils.isNotEmpty(filePath),
                "file path not specified in configuration");

        // read the file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            MeterInfo currentMeterInfo = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(PREFIX_200_RECORD)) {
                    // a new meter started with 200 record.
                    // update meter info
                    currentMeterInfo = decodeMeterInfo(line);
                }
                else if (line.startsWith(PREFIX_300_RECORD)) {
                    // decode and process reading with current meter info
                    processReading(line, currentMeterInfo);
                }
                // skip other record
            }
        }
        catch (Throwable t) {
            log.error("Error reading or decoding file: " + filePath, t);
        }
    }

    /**
     * decode a 200 record to get NMI and interval length.
     * The interval length will be useful in decoding 300 record to determine how many readings are in one record
     *
     * @param line  one 200 record
     * @return meter info, or null if the line is not a 200 record
     */
    private MeterInfo decodeMeterInfo(@NonNull String line) {
        log.debug("processing meter info: {}", line);
        List<String> splittedLine = Splitter.on(',').splitToList(line);

        Preconditions.checkArgument(splittedLine.size() >= REQ_SIZE_IN_200_RECORD,
                "unable to decode 200 record, no enough fields: " + line);

        String nmi = splittedLine.get(IDX_NMI_IN_200_RECORD);
        int intervalLength = Integer.parseInt(splittedLine.get(IDX_INTERVAL_LENGTH_IN_200_RECORD));

        return new MeterInfo(nmi, intervalLength);
    }

    /**
     * process a 300 record, send out the reading to queue
     * @param line  one 300 record
     * @param meterInfo  current meter info
     */
    private void processReading(@NonNull String line, @NonNull MeterInfo meterInfo) {
        log.debug("processing reading: {}", line);
        try {
            Iterator<String> it = Splitter.on(',').split(line).iterator();
            // skip record indicator
            it.next();

            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
            DateTime dateTime = formatter.parseDateTime(it.next()/* date str */);

            int numReadings = MINUTES_PER_DAY / meterInfo.getIntervalLength();
            List<MeterReading> readings = new ArrayList<>(numReadings);
            while (numReadings-- > 0 ) {
                Preconditions.checkArgument(it.hasNext(), "no enough interval value" );

                String readingStr = it.next();
                double value = Double.parseDouble(readingStr);
                // according NEM12 spec, no negative reading value is allowed.
                Preconditions.checkArgument(value >= 0, "invalid reading value: " + readingStr);

                readings.add(new MeterReading(value, dateTime.getMillis()));
                // plus interval length in minutes
                dateTime = dateTime.plusMinutes(meterInfo.getIntervalLength());
            }

            // TODO: send out the reading to queue
            log.info("sending {} {}", meterInfo, readings);
        }
        catch (Throwable t) {
            log.error("Error processing reading: " + line, t);
        }
    }

}
