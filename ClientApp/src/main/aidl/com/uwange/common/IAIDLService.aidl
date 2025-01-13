// IAIDLService.aidl
package com.uwange.common;
import com.uwange.common.IAIDLCallback;

// Declare any non-default types here with import statements

interface IAIDLService {
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void registerCallback(IAIDLCallback callback);
}