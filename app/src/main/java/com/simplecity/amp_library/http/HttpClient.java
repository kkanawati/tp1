package com.simplecity.amp_library.http;

import com.simplecity.amp_library.http.itunes.ItunesService;
import com.simplecity.amp_library.http.lastfm.LastFmService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {

    public static final String TAG = "HttpClient";

    private static final String URL_LAST_FM = "http://ws.audioscrobbler.com/2.0/";
    private static final String URL_ITUNES = "https://itunes.apple.com/search/";

    private static HttpClient sInstance;

    public OkHttpClient okHttpClient;

    // Initialize lastFmService directly here
    public static final LastFmService lastFmService = createLastFmService();

    public ItunesService itunesService;

    public static final String TAG_ARTWORK = "artwork";

    public static synchronized HttpClient getInstance() {
        if (sInstance == null) {
            sInstance = new HttpClient();
        }
        return sInstance;
    }

    private HttpClient() {
        okHttpClient = new OkHttpClient.Builder()
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.3", 8888)))
            .build();

        Retrofit itunesRestAdapter = new Retrofit.Builder()
            .baseUrl(URL_ITUNES)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        itunesService = itunesRestAdapter.create(ItunesService.class);
    }

    private static LastFmService createLastFmService() {
        Retrofit lastFmRestAdapter = new Retrofit.Builder()
            .baseUrl(URL_LAST_FM)
            .client(new OkHttpClient()) // Creating a new OkHttpClient for lastFmService
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        return lastFmRestAdapter.create(LastFmService.class);
    }
}
