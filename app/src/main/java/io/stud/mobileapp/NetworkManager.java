package io.stud.mobileapp;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public class NetworkManager
{
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance = null;
    private static String prefixURL = null;

    public RequestQueue requestQueue;

    private NetworkManager(Context context, String surl)
    {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        prefixURL = surl;
    }

    public static synchronized NetworkManager getInstance(Context context, String prefixURL)
    {
        if (null == instance)
            instance = new NetworkManager(context, prefixURL);
        return instance;
    }

    // So you don't need to pass context each time
    public static synchronized NetworkManager getInstance()
    {
        if (null == instance)
        {
            throw new IllegalStateException(NetworkManager.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    public void sendGETRequest(String path, final NetworkManagerIdListener<JSONObject> listener)
    {
        String url = prefixURL + path;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest

                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG + ": ", "GetRequest Response : " + response.toString());
                        try {
                            listener.getResult(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (null != error.networkResponse)
                        {
                            Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);

                        }
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }


    public void sendPOSTRequest(String path, JSONObject postparams, final NetworkManagerIdListener<JSONObject> listener)
    {
        String url = prefixURL + path;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest

                (Request.Method.POST, url, postparams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG + ": ", "PostRequest Response : " + response.toString());
                        try {
                            listener.getResult(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (null != error.networkResponse)
                        {
                            Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);

                        }
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
