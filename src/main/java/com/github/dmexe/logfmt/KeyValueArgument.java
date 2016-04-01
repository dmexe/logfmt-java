package com.github.dmexe.logfmt;

class KeyValueArgument implements BaseArgument {
    private String key;
    private String value;

    public KeyValueArgument(String key, Object value) {
        this.key = key;
        this.value = value.toString();
    }

    @Override
    public String toString() {
        return String.format("%s=%s", key, LogfmtEncoder.quote(value));
    }

    @Override
    public void writeTo(StringBuilder sb) {
        sb.append(key).append("=").append(LogfmtEncoder.quote(value)).append(" ");
    }
}

