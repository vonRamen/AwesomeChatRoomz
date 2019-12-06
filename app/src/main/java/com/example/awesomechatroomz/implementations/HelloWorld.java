package com.example.awesomechatroomz.implementations;

import android.util.Log;

import com.example.awesomechatroomz.interfaces.IHelloWorld;

import javax.inject.Inject;


public class HelloWorld implements IHelloWorld {
    private static final String TAG = "HelloWorld";
    @Inject
    public HelloWorld() {

    }

    public void say() {
        Log.d(TAG, "say: hello world");;
    }
}
