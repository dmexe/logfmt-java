package com.github.dmexe.logfmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class MapArgument implements BaseArgument {
    private Map<String,String> args;

    public MapArgument(Map<String,String> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        if (args.isEmpty()) {
            return "";
        }

        List<String> list = new LinkedList<>();
        for (Map.Entry<String,String> entry : args.entrySet()) {
            String value = String.format("%s=%s", entry.getKey(), LogfmtEncoder.quote(entry.getValue()));
            list.add(value);
        }
        return String.join(" ", list);
    }

    @Override
    public void writeTo(StringBuilder sb) {
        for (Map.Entry<String,String> entry : args.entrySet()) {
            sb.append(entry.getKey()).append("=").append(LogfmtEncoder.quote(entry.getValue())).append(" ");
        }
    }
}


