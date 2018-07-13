package me.niccorder.instagram.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.MarginItemDecoration;
import me.niccorder.instagram.internal.Refreshable;
import me.niccorder.instagram.model.ImagePost;

public class FeedFragment extends Fragment implements Refreshable, FeedAdapter.Callback {
  private static final String TAG = FeedFragment.class.getSimpleName();

  private FeedAdapter feedAdapter;
  private LinearLayoutManager layoutManager;
  private RecyclerView feedRecycler;

  private SwipeRefreshLayout refreshLayout;

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_feed, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    refreshLayout = view.findViewById(R.id.refresh_layout);
    refreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(getContext(), R.color.white)
    );
    refreshLayout.setColorSchemeResources(R.color.ig_brand_purple, R.color.ig_brand_pinkish, R.color.ig_brand_yellow_gold);
    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        ImagePost.query().newestFirst().findInBackground(new FindCallback<ImagePost>() {
          @Override
          public void done(List<ImagePost> objects, ParseException e) {
            if (e == null) {
              feedAdapter.submitList(objects);
              if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
              }
            } else {
              e.printStackTrace();
            }
          }
        });
      }
    });

    feedRecycler = view.findViewById(R.id.feed_recycler);
    feedAdapter = new FeedAdapter(this);
    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

    feedRecycler.addItemDecoration(new MarginItemDecoration(
            getResources().getDimensionPixelSize(R.dimen.item_post_margin_h),
            getResources().getDimensionPixelSize(R.dimen.item_post_margin_v),
            MarginItemDecoration.VERTICAL
    ));
    feedRecycler.setAdapter(feedAdapter);
    feedRecycler.setLayoutManager(layoutManager);
  }

  @Override
  public void onStart() {
    super.onStart();

    ImagePost.query()
            .newestFirst()
            .limit20()
            .findInBackground(new FindCallback<ImagePost>() {
              @Override
              public void done(List<ImagePost> objects, ParseException e) {
                if (e == null) {
                  for (ImagePost post : objects) {
                    Log.d(TAG, post.getDescription());
                  }

                  feedAdapter.submitList(objects);

                  if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                  }
                } else {
                  e.printStackTrace();
                }
              }
            });
  }

  @Override
  public void onRefresh() {
    if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
      feedRecycler.scrollToPosition(0);
    } else if (!refreshLayout.isRefreshing()) {
      refreshLayout.setRefreshing(true);

      ImagePost.query().newestFirst().findInBackground(new FindCallback<ImagePost>() {
        @Override
        public void done(List<ImagePost> objects, ParseException e) {
          if (e == null) {
            feedAdapter.submitList(objects);
            refreshLayout.setRefreshing(false);
          } else {
            e.printStackTrace();
          }
        }
      });
    } else {
      Log.d(TAG, "Feed is already refreshing.");
    }
  }

  @Override
  public void onPostClicked(ImagePost post) {
    Log.d(TAG, "onPostClicked(" + post.getObjectId() + ")");
  }

  @Override
  public void onLikeClicked(ImagePost post, boolean liked) {
    Log.d(TAG, "onLikeClicked(liked = " + Boolean.toString(liked) + ", id = " + post.getObjectId() + ")");

    final ParseUser user = ParseUser.getCurrentUser();
    if (liked) {
      post.likePost(user);
    } else {
      post.unlikePost(user);
    }
  }

  @Override
  public void onCommentClicked(ImagePost post) {
    Log.d(TAG, "onCommentClicked(" + post.getObjectId() + ")");
  }

  @Override
  public void onMessageClicked(ImagePost post) {
    Log.d(TAG, "onMessageClicked(" + post.getObjectId() + ")");
  }
}
