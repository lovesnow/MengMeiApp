package com.evilbeast.meizi.network.Api;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Author: sumary
 */
public interface ZxFuliApi {
    String BASE_URL = "http://www.15yc.com/";

    @GET("type/20/{page}.html")
    Observable<ResponseBody> getImageData(@Path("page") int page);
}
