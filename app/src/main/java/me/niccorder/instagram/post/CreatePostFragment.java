package me.niccorder.instagram.post;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.cameraview.CameraView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.OnClick;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BaseFragment;
import me.niccorder.instagram.model.ImagePost;

public class CreatePostFragment extends BaseFragment {
  private static final String TAG = CreatePostFragment.class.getSimpleName();
  private static final int REQ_CAMERA_PERMISSION = 1010;

  public interface Callback {

      /**
       * To be called when a post has been successfully created.
       */
      void onPostCreated();
  }

  private final ViewTreeObserver.OnGlobalLayoutListener gradientListener = new ViewTreeObserver.OnGlobalLayoutListener() {
    @Override
    public void onGlobalLayout() {
      Shader textShader = new LinearGradient(0, 0, createPostTitle.getMeasuredWidth(), createPostTitle.getHeight() / 4,
              new int[]{
                      ContextCompat.getColor(getContext(), R.color.ig_brand_purple),
                      ContextCompat.getColor(getContext(), R.color.ig_brand_pinkish)
              },
              new float[]{0f, 1f},
              Shader.TileMode.CLAMP
      );
      createPostTitle.getPaint().setShader(textShader);
    }
  };

  @BindView(R.id.root) ConstraintLayout root;

  @BindView(R.id.first_step_group) Group firstStepGroup;
  @BindView(R.id.create_post_title_tv) TextView createPostTitle;

  @BindView(R.id.camera_take_group) Group cameraSelectionGroup;
  @BindView(R.id.camera_view) CameraView cameraView;

  @BindView(R.id.enter_caption_group) Group submitPostGroup;
  @BindView(R.id.selected_picture_iv) ImageView selectedPhotoView;
  @BindView(R.id.caption_et) EditText captionEditText;

  private boolean cameraStarted = false;
  private File selectedPhotoFile;

  private Callback callback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof Callback) {
      callback = (Callback) context;
    } else {
      throw new IllegalStateException("Contaning context must implement " + CreatePostFragment.class.getSimpleName() + ".Callback");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    callback = null;
  }

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_add_content, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    createPostTitle.getViewTreeObserver().addOnGlobalLayoutListener(gradientListener);

    cameraView.addCallback(new CameraView.Callback() {
      @Override
      public void onPictureTaken(CameraView cameraView, byte[] data) {
        super.onPictureTaken(cameraView, data);
        Log.d(TAG, "on picture taken.");

        CreatePostFragment.this.onPictureTaken(data);
      }
    });
  }

  @OnClick(R.id.take_picture_btn)
  void onTakePictureClicked() {
    Log.d(TAG, "Take picture click");
    cameraView.takePicture();
  }

  @OnClick(R.id.submit_btn)
  void onSubmitClicked() {
    hideKeyboardFrom(getContext(), captionEditText);

    final String description = captionEditText.getText().toString();
    final ParseUser currentUser = ParseUser.getCurrentUser();
    final ParseFile imageFile = new ParseFile(selectedPhotoFile);

    onCreatePost(imageFile, currentUser, description);
  }

  @OnClick(R.id.from_camera_btn)
  void onRequestCameraPermission() {
    Log.d(TAG, "Requesting camera permission.");
    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
  }

  @Override
  public void onStart() {
    super.onStart();

    if (hasPermission(Manifest.permission.CAMERA)) {
      cameraSelectionGroup.setVisibility(View.VISIBLE);
      firstStepGroup.setVisibility(View.GONE);

      cameraStarted = true;
      cameraView.start();
    } else {
      cameraSelectionGroup.setVisibility(View.GONE);
      firstStepGroup.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onStop() {
    super.onStop();

    if (cameraStarted) {
      cameraStarted = false;
      cameraView.stop();
    }
  }

  @Override
  public void onDestroyView() {
    createPostTitle.getViewTreeObserver().removeOnGlobalLayoutListener(gradientListener);
    super.onDestroyView();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    boolean granted;
    switch (requestCode) {
      case REQ_CAMERA_PERMISSION:
        granted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);

        Log.d(TAG, String.format("request permission granted: %b", granted));
        if (granted) onCameraPermissionGranted();
        return;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private boolean hasPermission(String permission) {
    return ActivityCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
  }

  private void onCameraPermissionGranted() {
    if (hasPermission(Manifest.permission.CAMERA)) {
      TransitionManager.beginDelayedTransition(root);
      firstStepGroup.setVisibility(View.GONE);
      cameraSelectionGroup.setVisibility(View.VISIBLE);

      if (!cameraStarted) {
        cameraStarted = true;
        cameraView.start();
      }
    } else {
      Log.w(TAG, "Camera permission was not granted.");
    }
  }

  private void onPictureTaken(byte[] data) {
    OutputStream os = null;
    try {
      selectedPhotoFile = File.createTempFile(
          "parse",
          "tg",
          getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      );

      Log.d(TAG, "Saving photo to: " + selectedPhotoFile.getAbsolutePath());
      os = new FileOutputStream(selectedPhotoFile);
      os.write(data);
      os.close();

      final Bitmap image = BitmapFactory.decodeFile(selectedPhotoFile.getAbsolutePath());

      cameraSelectionGroup.setVisibility(View.GONE);
      submitPostGroup.setVisibility(View.VISIBLE);

      selectedPhotoView.setImageBitmap(image);
    } catch (IOException e) {
      Log.w(TAG, "Cannot write to temporary file.");
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          // Ignore
        }
      }
    }
  }

  public static void hideKeyboardFrom(Context context, View view) {
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  private void onCreatePost(
          @NonNull final ParseFile imageFile,
          @NonNull final ParseUser user,
          @NonNull final String description
  ) {
    final ImagePost newPost = ParseObject.create(ImagePost.class);
    newPost.setImage(imageFile);
    newPost.setUser(user);
    newPost.setDescription(description);

    newPost.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          Log.d(TAG, "Saved new post successfully!");
          onCreatePostSuccess();
        } else {
          e.printStackTrace();
        }
      }
    });
  }

  private void onCreatePostSuccess() {
    TransitionManager.beginDelayedTransition(root);

    cameraSelectionGroup.setVisibility(View.VISIBLE);
    submitPostGroup.setVisibility(View.GONE);
    callback.onPostCreated();
  }
}
