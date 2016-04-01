package com.github.dmexe.logfmt;

import java.util.*;

public class StructuredArgument {
    public static BaseArgument value(String key, Object value) {
        return new KeyValueArgument(key, value);
    }

    public static BaseArgument entries(Map<String,Object> args) {
        return new MapArgument(args);
    }
}
