package io.stud.mobileapp;

import org.json.JSONException;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public interface NetworkManagerIdListener<T>
{
    public void getResult(T object) throws JSONException;
}
