package me.niccorder.instagram.profile;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BaseFragment;
import me.niccorder.instagram.internal.MarginItemDecoration;
import me.niccorder.instagram.model.ImagePost;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment implements ProfilePostAdapter.Callback {

  private static final String TAG = ProfileFragment.class.getSimpleName();
  private static final String KEY_USER = "key_user";

  public interface Callback {
    void onLogoutClick();
  }

  public static ProfileFragment create(ParseUser user) {
    final Bundle arguments = new Bundle();
    arguments.putParcelable(KEY_USER, user);

    final ProfileFragment profileFragment = new ProfileFragment();
    profileFragment.setArguments(arguments);
    return profileFragment;
  }

  @BindView(R.id.profile_recycler) RecyclerView profileRecycler;

  private ParseUser user;
  private Callback callback;

  private ProfilePostAdapter adapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      user = getArguments().getParcelable(KEY_USER);
    } else {
      user = savedInstanceState.getParcelable(KEY_USER);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof Callback) {
      this.callback = (Callback) context;
    } else {
      throw new IllegalStateException("The containing context must implement ProfileFragment.Callback");
    }
  }

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initRecycler();

    ImagePost.query().createdBy(user).findInBackground(new FindCallback<ImagePost>() {
        @Override
        public void done(List<ImagePost> objects, ParseException e) {
            if (e == null) {
                adapter.submitList(objects);
            } else {
                e.printStackTrace();
            }
        }
    });
  }

  private void initRecycler() {
    adapter = new ProfilePostAdapter(this);

    final int gridSpanCount = 3;
    final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), gridSpanCount);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        if (position == 0)
          return gridSpanCount;
        return 1;
      }
    });

    profileRecycler.setLayoutManager(layoutManager);
    profileRecycler.addItemDecoration(
            new MarginItemDecoration(
                    getResources().getDimensionPixelSize(R.dimen.item_post_margin_h),
                    getResources().getDimensionPixelSize(R.dimen.item_post_grid_margin_v),
                    MarginItemDecoration.GRID
            )
    );
    profileRecycler.setAdapter(adapter);
  }

  @Override
  public void onAvatarClicked() {

  }

  @Override
  public void onChangeAvatarClicked() {
    Log.d(TAG, "onChangeAvatarClicked()");

    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    galleryIntent.setType("image/*");

    //Create chooser
    Intent chooser = Intent.createChooser(galleryIntent, "Choose an avatar");
    startActivity(chooser);
  }

  @Override
  public void onLogoutClicked() {
    callback.onLogoutClick();
  }
}
