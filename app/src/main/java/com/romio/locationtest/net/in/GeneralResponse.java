package com.romio.locationtest.net.in;

/**
 * Created by roman on 3/7/17.
 */

public class GeneralResponse<T> {
    private boolean status;
    private T data;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
