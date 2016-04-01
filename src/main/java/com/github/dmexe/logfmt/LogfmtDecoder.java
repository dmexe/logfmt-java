package com.github.dmexe.logfmt;

import java.util.HashMap;
import java.util.Map;

/**
 * port of of https://github.com/csquared/node-logfmt/blob/master/lib/logfmt_parser.js
 */
public class LogfmtDecoder {

    public static Map<String, String> decode(String s) {
        Map<String,String> obj = new HashMap<>();

        char[] line = s.trim().toCharArray();

        String key       = "";
        String value     = "";
        boolean inKey    = false;
        boolean inValue  = false;
        boolean inQuote  = false;
        boolean hadQuote = false;

        for(int i = 0; i <= line.length ; i++) {
            if (i == line.length || (line[i] == ' ' && !inQuote)) {
                if (inKey && key.length() > 0) {
                    obj.put(key, "true");
                } else if (inValue) {
                    if (value.equals("true")) {
                        obj.put(key, "true");
                    } else if (value.equals("false")) {
                        obj.put(key, "false");
                    } else if (value.equals("") && !hadQuote) {
                        obj.put(key, "");
                    } else {
                        obj.put(key, value);
                    }
                    value = "";
                }

                inKey = false;
                inValue = false;
                inQuote = false;
                hadQuote = false;
            }

            if (i < line.length) {
                if (line[i] == '=' && !inQuote) {
                    //println("split")
                    inKey = false;
                    inValue = true;
                } else if (line[i] == '\\') {
                    i++;
                    value = value + line[i];
                    //println("escape: " + line(i))
                } else if (line[i] == '"') {
                    hadQuote = true;
                    inQuote = !inQuote;
                    //println("in quote: " + inQuote)
                } else if (line[i] != ' ' && !inValue && !inKey) {
                    //println("start key with: " + line(i))
                    inKey = true;
                    key = "" + line[i];
                } else if (inKey) {
                    //println("add to key: " + line(i))
                    key = key + line[i];
                } else if (inValue) {
                    //println("add to value: " + line(i))
                    value = value + line[i];
                }
            }
        }
        return obj;
    }
}
