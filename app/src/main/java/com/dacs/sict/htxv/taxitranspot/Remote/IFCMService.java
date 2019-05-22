package com.dacs.sict.htxv.taxitranspot.Remote;

import com.dacs.sict.htxv.taxitranspot.Model.FCMResponse;
import com.dacs.sict.htxv.taxitranspot.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers( {
            "Content-Type:application/json",
            "Authorization:key=AAAALyFZM5I:APA91bH4cIOhiLUqlES-BIM0uXIJPy-MGCXsJaXMA0bpXxxDS2u0E_j9SysYx7Gxj8OgDrdmJHo4G4ZIQDa9-kcsTT2NjbxZYcdGSOqXhbYPnrRFyrXvsSDKhFLPW2ZRve8lQD7XJtPg"
    } )
    @POST("fom/send")
        Call<FCMResponse> sendMessage(@Body Sender body );
}
