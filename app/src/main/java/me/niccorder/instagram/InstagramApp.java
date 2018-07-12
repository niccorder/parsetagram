package me.niccorder.instagram;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.parse.Parse;
import com.parse.ParseObject;
import me.niccorder.instagram.internal.SimpleNetworkLogger;
import me.niccorder.instagram.model.ImagePost;
import okhttp3.OkHttpClient;

public class InstagramApp extends Application {

  private static final String SERVER_URL = "https://fbu-instagram-niccorder.herokuapp.com/parse";
  private static final String CLIENT_KEY = "fdaojbfnrbureb473fb834fbh38nkdfnskfn3";
  private static final String APPLICATION_ID = "johnny-hopkins";

  @Override
  public void onCreate() {
    super.onCreate();

    Stetho.initializeWithDefaults(this);

    final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    httpClientBuilder.addNetworkInterceptor(SimpleNetworkLogger.INSTANCE);

    ParseObject.registerSubclass(ImagePost.class);
    final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
        .server(SERVER_URL)
        .clientKey(CLIENT_KEY)
        .applicationId(APPLICATION_ID)
        .clientBuilder(httpClientBuilder)
        .build();

    Parse.initialize(configuration);
  }
}
