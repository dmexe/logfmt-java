package com.github.dmexe.logfmt;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LogfmtEncoder extends EncoderBase<ILoggingEvent> {

    private boolean           includeTime        = true;
    private ArrayList<String> includeMdcKeyNames = new ArrayList<>();
    private ArrayList<String> excludeMdcKeyNames = new ArrayList<>();

    public void setIncludeTime(boolean flag) {
        includeTime = flag;
    }

    public void setIncludeMdcKeyName(String keyName) {
        this.includeMdcKeyNames.add(keyName);
    }

    public void setExcludeMdcKeyName(String keyName) {
        this.excludeMdcKeyNames.add(keyName);
    }

    @Override
    public void close() {}

    @Override
    public void doEncode(ILoggingEvent event) throws java.io.IOException {
        StringBuilder sb = new StringBuilder(128);

        String level = event.getLevel().toString().toLowerCase();
        String time = java.time.Instant.ofEpochMilli(event.getTimeStamp()).toString();
        String thread = event.getThreadName();
        String logger = event.getLoggerName();
        IThrowableProxy err = event.getThrowableProxy();
        String msg = event.getFormattedMessage();

        if (err != null) {
            msg += ": ";
            msg += err.getMessage();
        }

        append(sb, "level", level);
        if (this.includeTime) {
            append(sb, "time", time);
        }
        append(sb, "msg", msg);
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

        if (!this.includeMdcKeyNames.isEmpty()) {
            mdc = new HashMap<>(mdc);
            mdc.keySet().retainAll(this.includeMdcKeyNames);
        }

        if (!this.excludeMdcKeyNames.isEmpty()) {
            mdc = new HashMap<>(mdc);
            mdc.keySet().removeAll(this.excludeMdcKeyNames);
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
        StringBuilder stBuilder = new StringBuilder(128);

        for (StackTraceElementProxy it : err.getStackTraceElementProxyArray()) {
            addStackTraceElement(stBuilder, it.getStackTraceElement());
        }
        addErrorCause(stBuilder, err);
        if (stBuilder.length() > 0) {
            append(sb, "stacktrace", stBuilder.toString());
        }
    }

    private void addErrorCause(StringBuilder stringBuilder, IThrowableProxy err) {
        if (err == null) {
            return;
        }

        IThrowableProxy cause = err.getCause();
        if (cause == null) {
            return;
        }

        addStackTraceElement(stringBuilder, cause);
        for (StackTraceElementProxy it : cause.getStackTraceElementProxyArray()) {
            addStackTraceElement(stringBuilder, it.getStackTraceElement());
        }

        addErrorCause(stringBuilder, cause.getCause());
    }

    private void addStackTraceElement(StringBuilder stringBuilder, IThrowableProxy err) {
        if (err != null) {
            stringBuilder
                    .append("[")
                    .append("Caused by: ")
                    .append(err.getClassName())
                    .append(": ")
                    .append(err.getMessage())
                    .append("]");
        }
    }

    private void addStackTraceElement(StringBuilder stringBuilder, StackTraceElement elem) {
        if (elem != null) {
            String file  = elem.getFileName();
            String clazz = elem.getClassName();
            String func  = elem.getMethodName();
            int line     = elem.getLineNumber();
            stringBuilder
                    .append("[")
                    .append(file).append(":")
                    .append(line).append(":")
                    .append(clazz).append("#")
                    .append(func)
                    .append("]");
        }
    }

    private void append(StringBuilder sb, String key, String value) {
        sb.append(key);
        sb.append("=");
        sb.append(quote(value));
        sb.append(" ");
    }

    private static boolean needsQuoting(String string) {
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
