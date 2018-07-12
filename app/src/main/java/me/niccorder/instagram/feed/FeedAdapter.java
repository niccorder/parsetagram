package me.niccorder.instagram.feed;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BindableViewHolder;
import me.niccorder.instagram.model.ImagePost;

public class FeedAdapter extends ListAdapter<ImagePost, FeedAdapter.ItemHolder> {
  private static final String TAG = FeedAdapter.class.getSimpleName();

  /**
   * Callbacks used to pass interactions to be handled in an upwards manor.
   */
  interface Callback {
    void onPostClicked(ImagePost post);
    void onLikeClicked(ImagePost post, boolean liked);
    void onCommentClicked(ImagePost post);
    void onMessageClicked(ImagePost post);
  }

  private final Callback callback;

  public FeedAdapter(@NonNull final Callback callback) {
    super(
            new AsyncDifferConfig.Builder<>(Differ.INSTANCE)
                    .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
                    .build()
    );

    this.callback = callback;
  }

  @NonNull
  @Override
  public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final ItemHolder holder = new ItemHolder(
            LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_image_post,
                    parent,
                    false
            )
    );

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.onPostClicked(getItem(holder.getAdapterPosition()));
      }
    });
    holder.likeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.onLikeClicked(
                getItem(holder.getAdapterPosition()),
                !holder.likeButton.isActivated()
        );
        holder.likeButton.setActivated(!holder.likeButton.isActivated());
      }
    });
    holder.messageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.onMessageClicked(getItem(holder.getAdapterPosition()));
      }
    });
    holder.commentButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.onCommentClicked(getItem(holder.getAdapterPosition()));
      }
    });

    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
    final ImagePost post = getItem(position);

    loadPostImage(post.getImage(), holder.image);
    loadAvatarImage(post.getAvatar(), holder.avatar);

    final String username = post.getUsername();
    holder.username.setText(username);

    setDescription(holder.description, username, post.getDescription());

    final int requestStartPosition = position;
    post.likeCount(new CountCallback() {
      @Override
      public void done(int count, ParseException e) {
        handleLikeCountCompleted(requestStartPosition, holder, count, e);
      }
    });
  }

  private boolean shouldUpdateHolder(int startPosition, int endPosition) {
    return endPosition != RecyclerView.NO_POSITION && startPosition == endPosition;
  }

  private String formatLikes(Context context, int likes) {
    return Integer.toString(likes) + " " + context.getResources().getQuantityString(
            R.plurals.n_likes,
            likes
    );
  }

  private void handleLikeCountCompleted(
          int requestStartPosition,
          ItemHolder holder,
          int count,
          ParseException e
  ) {
    final int requestEndedPosition = holder.getAdapterPosition();
    if (shouldUpdateHolder(requestStartPosition, requestEndedPosition)) {
      Log.d(TAG, "updating likes text for holder @ " + Integer.toString(requestEndedPosition));

      final String likesText = formatLikes(holder.itemView.getContext(), count);
      holder.likeCount.setText(likesText);
    } else {
      Log.d(TAG, "Skipping likes text @ " + Integer.toString(requestEndedPosition));
    }
  }

  private void loadPostImage(final ParseFile imageFile, final ImageView imageView) {
    if (imageFile == null) {
      imageView.setImageResource(R.color.grey_5);
    } else {
      Glide.with(imageView)
              .asBitmap()
              .load(imageFile.getUrl())
              .apply(
                      RequestOptions.centerCropTransform()
                              .placeholder(R.color.grey_5)
                              .error(R.color.grey_5)
              )
              .transition(
                      BitmapTransitionOptions.withCrossFade()
              )
              .into(imageView);
    }
  }

  private void loadAvatarImage(final ParseFile avatarFile, final ImageView avatarView) {
    if (avatarFile == null) {
      avatarView.setImageResource(R.drawable.ic_placeholder_circle);
    } else {
      Glide.with(avatarView)
              .asBitmap()
              .load(avatarFile.getUrl())
              .apply(
                      RequestOptions.circleCropTransform()
                              .placeholder(R.drawable.ic_placeholder_circle)
                              .error(R.drawable.ic_placeholder_circle)
              )
              .transition(BitmapTransitionOptions.withCrossFade())
              .into(avatarView);

    }
  }

  private void setDescription(TextView descriptionView, String username, String description) {
    SpannableStringBuilder descriptionSpan = new SpannableStringBuilder(username + ":");
    descriptionSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, username.length() + 1, 0);
    descriptionSpan.append(" " + description);

    descriptionView.setText(descriptionSpan);
  }

  static class ItemHolder extends BindableViewHolder {

    @BindView(R.id.post_image_iv) ImageView image;
    @BindView(R.id.user_avatar_iv) ImageView avatar;
    @BindView(R.id.username_tv) TextView username;
    @BindView(R.id.likes_count_tv) TextView likeCount;
    @BindView(R.id.description_tv) TextView description;
    @BindView(R.id.time_since_tv) TextView timeSince;
    @BindView(R.id.like_iv) ImageView likeButton;
    @BindView(R.id.comment_iv) ImageView commentButton;
    @BindView(R.id.message_iv) ImageView messageButton;

    ItemHolder(View itemView) {
      super(itemView);

      // Adds in the animation for a card click.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(
                itemView.getContext(),
                R.animator.lift_on_touch
        );
        itemView.setStateListAnimator(stateListAnimator);
      }
    }
  }

  static final class Differ extends DiffUtil.ItemCallback<ImagePost> {
    static final Differ INSTANCE = new Differ();

    private Differ() {
    }

    @Override
    public boolean areItemsTheSame(ImagePost oldItem, ImagePost newItem) {
      return oldItem.getObjectId().equals(newItem.getObjectId());
    }

    @Override
    public boolean areContentsTheSame(ImagePost oldItem, ImagePost newItem) {
      boolean sameTitle = oldItem.getTitle() != null
              && oldItem.getTitle().equals(newItem.getTitle());
      boolean sameDescription = oldItem.getDescription() != null
              && oldItem.getDescription().equals(newItem.getDescription());
      boolean sameImage = oldItem.getImage() != null
              && oldItem.getImage().getUrl() != null
              && oldItem.getImage().getUrl().equals(newItem.getImage().getUrl());
      boolean sameUsername = oldItem.getUsername() != null
              && oldItem.getUsername().equals(newItem.getUsername());

      return sameTitle && sameDescription && sameImage && sameUsername;
    }
  }
}
