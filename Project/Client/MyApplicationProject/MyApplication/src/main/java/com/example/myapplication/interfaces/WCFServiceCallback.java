package com.example.myapplication.interfaces;

import com.example.myapplication.domain_objects.ServiceResponse;

/**
 * Created by Michal on 05/01/14.
 */
public interface WCFServiceCallback<T, U> {
    void onServiceCallCompleted(ServiceResponse<T> serviceResponse, U parameter);
}
