package me.niccorder.instagram.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import me.niccorder.instagram.HomeActivity;
import me.niccorder.instagram.R;

public class AuthenticationActivity extends AppCompatActivity implements LoginFragment.Callback,
    SignupUsernameFragment.Callback,
    SignupPasswordFragment.Callback {
  private static final String TAG = AuthenticationActivity.class.getSimpleName();

  private LoginFragment loginFragment = LoginFragment.create();

  public static void start(Context from) {
    final Intent intent = new Intent(from, AuthenticationActivity.class);
    from.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_authentication);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.auth_fragment_container, loginFragment)
          .commit();
    }
  }

  @Override
  public void attemptLogin(String username, String password) {
    ParseUser.logInInBackground(username, password, new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Login success. Starting home activity.");
          HomeActivity.start(AuthenticationActivity.this);
          finish();
        } else {
          Log.w(TAG, "Login failure.");
          loginFragment.onLoginError(e);
        }
      }
    });
  }

  @Override
  public void beginSignup() {
    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
                R.anim.slide_in_from_right,
                R.anim.slide_out_to_left,
                R.anim.slide_in_from_left,
                R.anim.slide_out_to_right
        )
        .replace(R.id.auth_fragment_container, SignupUsernameFragment.create())
        .addToBackStack("username_step")
        .commit();
  }

  @Override
  public void onNextClicked(String username) {
    getSupportFragmentManager()
        .beginTransaction()
            .setCustomAnimations(
                    R.anim.slide_in_from_right,
                    R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left,
                    R.anim.slide_out_to_right
            )
        .replace(R.id.auth_fragment_container, SignupPasswordFragment.create(username))
        .addToBackStack("password_step")
        .commit();
  }

  @Override
  public void attemptSignup(String username, String password) {
    Log.d(TAG, "Attempting signup...");

    ParseUser user = new ParseUser();
    user.setUsername(username);
    user.setPassword(password);

    user.signUpInBackground(new SignUpCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          Log.d(TAG, "Sign up success");
          HomeActivity.start(AuthenticationActivity.this);
          finish();
        } else {
          e.printStackTrace();
        }
      }
    });
  }
}
