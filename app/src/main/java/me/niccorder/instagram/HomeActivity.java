package me.niccorder.instagram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import me.niccorder.instagram.auth.AuthenticationActivity;
import me.niccorder.instagram.post.CreatePostFragment;
import me.niccorder.instagram.feed.FeedFragment;
import me.niccorder.instagram.internal.Refreshable;
import me.niccorder.instagram.profile.ProfileFragment;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener,
    ProfileFragment.Callback,
    CreatePostFragment.Callback {

  private static final String TAG = HomeActivity.class.getSimpleName();

  public static void start(Context context) {
    final Intent intent = new Intent(context, HomeActivity.class);
    context.startActivity(intent);
  }

  private ViewPager homePager;
  private BottomNavigationView navigationView;

  private HomeAdapter homeAdapter;

  private final Fragment[] fragments = new Fragment[] {
      new FeedFragment(),
      new CreatePostFragment(),
      ProfileFragment.create(ParseUser.getCurrentUser())
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.findViewById(R.id.messages_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(HomeActivity.this, "Messages clicked", Toast.LENGTH_SHORT).show();
      }
    });

    setSupportActionBar(toolbar);
    getSupportActionBar().setElevation(
            getResources().getDimensionPixelSize(R.dimen.action_bar_elevation)
    );

    navigationView = findViewById(R.id.bottom_nav);
    navigationView.setSelectedItemId(R.id.action_home);
    navigationView.setOnNavigationItemSelectedListener(this);
    navigationView.setOnNavigationItemReselectedListener(this);

    homePager = findViewById(R.id.home_pager);
    homePager.setOffscreenPageLimit(2);

    homeAdapter = new HomeAdapter(getSupportFragmentManager(), fragments);
    homePager.setAdapter(homeAdapter);

    homePager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        switch (position) {
          case 0:
            if(navigationView.getSelectedItemId() != R.id.action_home) {
              navigationView.setSelectedItemId(R.id.action_home);

              getSupportActionBar().setElevation(
                      getResources().getDimensionPixelSize(R.dimen.action_bar_elevation)
              );
            }
            break;
          case 1:
            if(navigationView.getSelectedItemId() != R.id.action_post) {
              navigationView.setSelectedItemId(R.id.action_post);
              getSupportActionBar().setElevation(0);
            }
            break;
          case 2:
            if (navigationView.getSelectedItemId() != R.id.action_profile) {
              navigationView.setSelectedItemId(R.id.action_profile);
              getSupportActionBar().setElevation(0);
            }
            break;
          default:
            break;
        }
      }
    });
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_home:
        Log.d(TAG, "home selected.");
        homePager.setCurrentItem(0);
        return true;
      case R.id.action_post:
        Log.d(TAG, "post selected.");
        homePager.setCurrentItem(1);
        return true;
      case R.id.action_profile:
        Log.d(TAG, "profile selected.");
        homePager.setCurrentItem(2);
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onNavigationItemReselected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_home:
        Log.d(TAG, "home re-selected.");
        final Refreshable fragment = (Refreshable)fragments[0];
        fragment.onRefresh();
        break;
      case R.id.action_post:
        Log.d(TAG, "post re-selected.");
        break;
      case R.id.action_profile:
        Log.d(TAG, "profile re-selected.");
        break;
      default:
        break;
    }
  }

  @Override
  public void onPostCreated() {
    navigationView.setSelectedItemId(R.id.action_home);
    ((Refreshable) fragments[0]).onRefresh();
  }

  @Override
  public void onLogoutClick() {
    Log.d(TAG, "onLogoutClick()");

    ParseUser.logOutInBackground(new LogOutCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          Log.d(TAG, "Logout successful!");
          startActivity(new Intent(HomeActivity.this, AuthenticationActivity.class));
          finish();
        } else {
          Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
          e.printStackTrace();
        }
      }
    });
  }

  static class HomeAdapter extends FragmentStatePagerAdapter {
    private final Fragment[] fragments;

    public HomeAdapter(FragmentManager fm, Fragment[] fragments) {
      super(fm);
      this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
      return fragments[position];
    }

    @Override
    public int getCount() {
      return fragments.length;
    }
  }
}
