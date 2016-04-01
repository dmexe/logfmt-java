package com.github.dmexe.logfmt;

import java.util.*;

class StructuredArgument {
    public static BaseArgument value(String key, Object value) {
        return new KeyValueArgument(key, value);
    }

    public static BaseArgument entries(Map<String,String> args) {
        return new MapArgument(args);
    }
}
