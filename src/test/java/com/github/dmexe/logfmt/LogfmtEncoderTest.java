package demo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.github.dmexe.logfmt.LogfmtEncoder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static com.github.dmexe.logfmt.StructuredArgument.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LogfmtEncoderTest extends TestCase
{

    private LogfmtEncoder encoder = null;

    public LogfmtEncoderTest(String testName)
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( LogfmtEncoderTest.class );
    }

    public void testAppend()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);

        log.info("info msg\n new line\r {}", "return");

        String output = stream.toString();

        assertThat(output, containsString("level=info"));
        assertThat(output, containsString("msg=\"info msg  new line return\""));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("thread=main\n"));
    }

    public void testMDC()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);

        try {
            MDC.put("mdcKey", "mdcValue");
            log.info("test");
        } finally {
            MDC.clear();
        }

        String output = stream.toString();

        assertThat(output, containsString("level=info"));
        assertThat(output, containsString("msg=test"));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("mdcKey=mdcValue"));
        assertThat(output, containsString("thread=main\n"));
    }

    public void testMDCIncluded()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);
        this.encoder.setIncludeMdcKeyName("incKey");

        try {
            MDC.put("mdcKey", "mdcValue");
            MDC.put("incKey", "incValue");
            log.info("test");
        } finally {
            MDC.clear();
        }

        String output = stream.toString();

        assertThat(output, containsString("level=info"));
        assertThat(output, containsString("msg=test"));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("incKey=incValue"));
        assertThat(output, not(containsString("mdcKey")));
        assertThat(output, containsString("thread=main\n"));
    }

    public void testMDCExcluded()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);
        this.encoder.setExcludeMdcKeyName("mdcKey");

        try {
            MDC.put("mdcKey", "mdcValue");
            MDC.put("incKey", "incValue");
            log.info("test");
        } finally {
            MDC.clear();
        }

        String output = stream.toString();

        assertThat(output, containsString("level=info"));
        assertThat(output, containsString("msg=test"));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("incKey=incValue"));
        assertThat(output, not(containsString("mdcKey")));
        assertThat(output, containsString("thread=main\n"));
    }

    public void testStructuredArgument()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);

        Map<String,Object> items = new HashMap<>();
        items.put("first", "first value");
        items.put("int", 1);
        items.put("bool", true);

        log.info("test {} {}", value("argKey", "arg value"), entries(items), value("hidden", "hide me"));

        String output = stream.toString();

        assertThat(output, containsString("level=info"));
        assertThat(output, containsString("msg=\"test argKey=\\\"arg value\\\" bool=true first=\\\"first"));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("argKey=\"arg value\""));
        assertThat(output, containsString("first=\"first value\""));
        assertThat(output, containsString("int=1"));
        assertThat(output, containsString("hidden=\"hide me\""));
        assertThat(output, containsString("thread=main\n"));
    }

    public void testStackTrace()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger log = getLogger(stream);

        try {
            throw new RuntimeException("boom!", new RuntimeException("test"));
        } catch (Exception e){
            log.error("catch", e);
        }

        String output = stream.toString();

        assertThat(output, containsString("level=error"));
        assertThat(output, containsString("msg=\"catch: boom!\""));
        assertThat(output, containsString("logger=demo.LogfmtEncoderTest"));
        assertThat(output, containsString("err=java.lang.RuntimeException"));
        assertThat(output, containsString("stacktrace=\"[LogfmtEncoderTest.java:150:demo.LogfmtEncoderTest#testStackTrace]["));
        assertThat(output, containsString("[Caused by: java.lang.RuntimeException: test]"));
        assertThat(output, containsString("]\" thread=main\n"));
    }

    private Logger getLogger(ByteArrayOutputStream stream) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        LogfmtEncoder encoder = new LogfmtEncoder();
        encoder.setContext(context);
        encoder.start();

        OutputStreamAppender<ILoggingEvent> appender= new OutputStreamAppender<>();
        appender.setName("OutputStream Appender");
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.setOutputStream(stream);
        appender.start();

        Logger log = context.getLogger(this.getClass());
        log.addAppender(appender);

        this.encoder = encoder;
        return log;
    }
}
