package com.nivixx.ndatabase.tests.repository.entity;

public class EmbeddedObject {

    private long field = 1L;
    private boolean isEnabled = true;

    public EmbeddedObject() {
    }

    public long getField() {
        return field;
    }

    public void setField(long field) {
        this.field = field;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
