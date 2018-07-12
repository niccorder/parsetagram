package me.niccorder.instagram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseSession;
import me.niccorder.instagram.auth.AuthenticationActivity;

public class SplashActivity extends AppCompatActivity {
  private static final String TAG = SplashActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
  }

  @Override
  protected void onResume() {
    super.onResume();

    ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
      @Override
      public void done(ParseSession object, ParseException e) {
        if (e != null || object == null) {
          Log.d(TAG, "No session has been found. Starting authentication activity.");
          AuthenticationActivity.start(SplashActivity.this);
        } else {
          Log.d(TAG, String.format("Found user session: %s. Opening home activity.", object.getSessionToken()));
          HomeActivity.start(SplashActivity.this);
        }

        finish();
      }
    });
  }
}
