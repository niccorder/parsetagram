package me.niccorder.instagram.internal;

import android.util.Log;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class SimpleNetworkLogger implements Interceptor {

  public static final SimpleNetworkLogger INSTANCE = new SimpleNetworkLogger();

  private SimpleNetworkLogger() {
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    final Request req = chain.request();
    final HttpUrl url = req.url();
    Log.d("Network", String.format("%s\t\t\t%s", req.method(), url.encodedPath()));

    return chain.proceed(req);
  }
}
