package com.github.dmexe.logfmt;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;

public class LogfmtEncoder extends EncoderBase<ILoggingEvent> {

    private boolean includeTime = true;

    private boolean isIncludeTime() {
        return includeTime;
    }

    public void setIncludeTime(boolean flag) {
        includeTime = flag;
    }

    @Override
    public void close() {}

    @Override
    public void doEncode(ILoggingEvent event) throws java.io.IOException {
        StringBuilder sb = new StringBuilder(128);

        String level        = event.getLevel().toString().toLowerCase();
        String time         = java.time.Instant.ofEpochMilli(event.getTimeStamp()).toString();
        String thread       = event.getThreadName();
        String logger       = event.getLoggerName();
        IThrowableProxy err = event.getThrowableProxy();
        String msg          = event.getFormattedMessage();

        if (err != null) {
            msg += ": ";
            msg += err.getMessage();
        }

        append(sb, "level",  level);
        if (isIncludeTime()) {
            append(sb, "time",   time);
        }
        append(sb, "msg",    msg);
        append(sb, "logger", logger);

        addMDC(sb, event.getMDCPropertyMap());
        addStructuredArguments(sb, event.getArgumentArray());
        addError(sb, err);

        sb.append("thread=").append(quote(thread));
        sb.append(CoreConstants.LINE_SEPARATOR);

        outputStream.write(sb.toString().getBytes());
        outputStream.flush();
    }

    private void addStructuredArguments(StringBuilder sb, Object[] args) {

        if (args == null || args.length == 0) {
            return;
        }

        for (Object obj : args) {
            if (obj instanceof BaseArgument) {
                BaseArgument arg = (BaseArgument) obj;
                arg.writeTo(sb);
            }
        }
    }

    private void addMDC(StringBuilder sb, Map<String, String> mdc) {

        if (mdc == null || mdc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : mdc.entrySet()) {
            append(sb, entry.getKey(), entry.getValue());
        }
    }

    private void addError(StringBuilder sb, IThrowableProxy err) {
        if (err == null) {
            return;
        }

        append(sb, "err", err.getClassName());
        StackTraceElementProxy[] st = err.getStackTraceElementProxyArray();
        StringBuilder stBuilder = new StringBuilder(128);

        for (StackTraceElementProxy it : st) {
            StackTraceElement elem = it.getStackTraceElement();
            if (elem != null) {
                String file  = it.getStackTraceElement().getFileName();
                String klass = it.getStackTraceElement().getClassName();
                String func  = it.getStackTraceElement().getMethodName();
                int line     = it.getStackTraceElement().getLineNumber();
                stBuilder
                        .append("[")
                        .append(file).append(":")
                        .append(line).append(":")
                        .append(klass).append("#")
                        .append(func)
                        .append("]");
            }
        }
        append(sb, "stacktrace", stBuilder.toString());
    }

    private void append(StringBuilder sb, String key, String value) {
        sb.append(key);
        sb.append("=");
        sb.append(quote(value));
        sb.append(" ");
    }

    public static boolean needsQuoting(String string) {
        int  i;
        char ch;
        for (i = 0 ; i < string.length(); i ++) {
            ch = string.charAt(i);

            if (!(
                    (ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (ch >= '0' && ch <= '9') ||
                    (ch == '-' || ch == '.')
                )) {
                return true;
            }
        }
        return false;
    }

    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        if (!needsQuoting(string)) {
            return string;
        }

        char         c;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\r':
                    break;
                case '\n':
                    sb.append(" ");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
