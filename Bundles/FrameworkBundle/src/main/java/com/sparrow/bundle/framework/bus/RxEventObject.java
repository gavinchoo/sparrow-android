package com.sparrow.bundle.framework.bus;

public class RxEventObject {

    private String event;
    private Object data;

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public <T> T getData() {
        return (T) data;
    }
}
