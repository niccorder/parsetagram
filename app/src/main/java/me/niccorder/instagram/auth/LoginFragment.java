package me.niccorder.instagram.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.parse.ParseException;

import butterknife.BindView;
import butterknife.OnClick;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BaseFragment;

public class LoginFragment extends BaseFragment {
  private static final String TAG = LoginFragment.class.getSimpleName();

  interface Callback {
    void attemptLogin(String username, String password);
    void beginSignup();
  }

  public static LoginFragment create() {
    return new LoginFragment();
  }

  @BindView(R.id.username) EditText username;
  @BindView(R.id.password) EditText password;

  private Callback callback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof Callback) {
      this.callback = (Callback) context;
    } else {
      throw new IllegalStateException("Containing activity must implement LoginFragment.Callback.");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    this.callback = null;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @OnClick(R.id.sign_up_btn)
  void onSignupClicked() {
    callback.beginSignup();
  }

  @OnClick(R.id.log_in_btn)
  void onLoginClicked() {
    final String usernameText = username.getText().toString();
    final String passwordText = password.getText().toString();

    showLoading(true);
    callback.attemptLogin(usernameText, passwordText);
  }

  public void onLoginError(ParseException exception) {
    showLoading(false);

    switch (exception.getCode()) {
      case ParseException.USERNAME_MISSING:
        username.setError("Username can not be empty.");
        break;
      case ParseException.PASSWORD_MISSING:
        password.setError("Password can not be empty.");
        break;
      case ParseException.VALIDATION_ERROR:
        username.setError("Incorrect username or password.");
        break;
      default:
        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        break;
    }
  }

  void showLoading(boolean show) {
    Log.d(TAG, "showLoading(" + show + ")");
    // TODO(niccorder) – Show a loading view
  }
}
