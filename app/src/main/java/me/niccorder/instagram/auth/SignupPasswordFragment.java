package me.niccorder.instagram.auth;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import me.niccorder.instagram.R;

public class SignupPasswordFragment extends Fragment {

  private static final String TAG = SignupPasswordFragment.class.getSimpleName();
  private static final String KEY_USERNAME = "key_username";

  interface Callback {
    void attemptSignup(String username, String password);
  }

  public static SignupPasswordFragment create(@NonNull String username) {
    final Bundle arguments = new Bundle();
    arguments.putString(KEY_USERNAME, username);

    final SignupPasswordFragment fragment = new SignupPasswordFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  private final ViewTreeObserver.OnGlobalLayoutListener gradientListener = new ViewTreeObserver.OnGlobalLayoutListener() {
    @Override
    public void onGlobalLayout() {
      Shader textShader = new LinearGradient(0, 0, title.getMeasuredWidth(), title.getHeight() / 4,
              new int[]{
                      ContextCompat.getColor(getContext(), R.color.ig_brand_purple),
                      ContextCompat.getColor(getContext(), R.color.ig_brand_pinkish)
              },
              new float[]{0f, 1f},
              Shader.TileMode.CLAMP
      );
      title.getPaint().setShader(textShader);
    }
  };

  private TextView title;
  private EditText password;
  private Button signupButton;

  private String username;
  private Callback callback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof Callback) {
      this.callback = (Callback) context;
    } else {
      throw new IllegalStateException("Containing activity must implement SignupUsernameFragment.Callback");
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
    return inflater.inflate(R.layout.fragment_signup_password, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    title = view.findViewById(R.id.title_tv);
    password = view.findViewById(R.id.password_tv);
    signupButton = view.findViewById(R.id.signup_btn);

    title.getViewTreeObserver().addOnGlobalLayoutListener(gradientListener);

    username = getUsername(savedInstanceState);
    signupButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.attemptSignup(username, password.getText().toString());
      }
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    title.getViewTreeObserver().removeOnGlobalLayoutListener(gradientListener);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(KEY_USERNAME, username);
  }

  private String getUsername(Bundle savedState) {
    if (savedState == null) {
      return getArguments().getString(KEY_USERNAME);
    }
    return savedState.getString(KEY_USERNAME);
  }
}
