package com.github.dmexe.logfmt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Map;
import java.util.HashMap;

public class LogfmtDecoderTest
    extends TestCase
{
    public LogfmtDecoderTest(String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( LogfmtDecoderTest.class );
    }

    public void testSimpleFlag()
    {
        Map<String, String> fmt = LogfmtDecoder.decode("hello");
        Map<String, String> exp = new HashMap<>();
        exp.put("hello", "true");
        assertEquals(fmt, exp);
    }

    public void testSimpleKeyValue()
    {
        Map<String, String> fmt = LogfmtDecoder.decode("hello=kitty");
        Map<String, String> exp = new HashMap<>();
        exp.put("hello", "kitty");
        assertEquals(fmt, exp);
    }

    public void testSimpleBoolean()
    {
        Map<String, String> fmt = LogfmtDecoder.decode("foo=true bar=false");
        Map<String, String> exp = new HashMap<>();
        exp.put("foo", "true");
        exp.put("bar", "false");
        assertEquals(fmt, exp);
    }

    public void testEscapesInValue()
    {
        Map<String, String> fmt = LogfmtDecoder.decode("hello=\"'kitty'\" second='kitty' last=\"one \\\"two\\\"\"");
        Map<String, String> exp = new HashMap<>();
        exp.put("hello", "'kitty'");
        exp.put("second", "'kitty'");
        exp.put("last", "one \"two\"");
        assertEquals(fmt, exp);
    }

    public void testStringWithEq()
    {
        Map<String, String> fmt = LogfmtDecoder.decode("foo=\"hello=kitty\"");
        Map<String, String> exp = new HashMap<>();
        exp.put("foo", "hello=kitty");
        assertEquals(fmt, exp);
    }

    public void testComplexString()
    {
        String input = "foo=bar a=14 baz=\"hello kitty\" cool%story=bro f %^asdf code=H12 path=/hello/user@foo.com/close";
        Map<String,String> fmt  = LogfmtDecoder.decode(input);
        Map<String, String> exp = new HashMap<>();
        exp.put("foo",        "bar");
        exp.put("a",          "14");
        exp.put("baz",        "hello kitty");
        exp.put("cool%story", "bro");
        exp.put("f",          "true");
        exp.put("%^asdf",     "true");
        exp.put("code",       "H12");
        exp.put("path",       "/hello/user@foo.com/close");
        assertEquals(fmt, exp);
    }
}
