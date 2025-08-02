package com.neroforte.goodfiction.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public class Description {
    private String value;

    @JsonCreator
    public Description(Object input) {
        if (input instanceof String) {
            this.value = (String) input;
        } else if (input instanceof Map<?, ?> map) {
            this.value = (String) map.get("value");
        }
    }

    public String getValue() {
        return value;
    }
}
