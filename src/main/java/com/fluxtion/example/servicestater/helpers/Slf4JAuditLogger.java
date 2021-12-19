package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.runtim.audit.LogRecord;
import com.fluxtion.runtim.audit.LogRecordListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends Fluxtion audit records to slf4j registered logger
 */
@Slf4j(topic = "fluxtion.eventLog")
public class Slf4JAuditLogger implements LogRecordListener {

    @Override
    public void processLogRecord(LogRecord logRecord) {
        log.info(logRecord.toString());
        log.info("\n---\n");
    }
}

