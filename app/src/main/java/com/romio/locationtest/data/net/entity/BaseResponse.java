package com.romio.locationtest.data.net.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by roman on 3/7/17
 */

public class BaseResponse<T> implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
