package me.niccorder.instagram.profile;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BindableViewHolder;
import me.niccorder.instagram.model.ImagePost;

import static me.niccorder.instagram.profile.ProfilePostAdapter.Type.HEADER;
import static me.niccorder.instagram.profile.ProfilePostAdapter.Type.POST;

public class ProfilePostAdapter extends ListAdapter<ImagePost, ProfilePostAdapter.BaseHolder> {

    enum Type {
        HEADER,
        POST
    }

    interface Callback {
        void onAvatarClicked();
        void onChangeAvatarClicked();
        void onLogoutClicked();
    }

    private final Callback callback;

    public ProfilePostAdapter(final Callback callback) {
        super(
                new AsyncDifferConfig.Builder<>(Differ.INSTANCE)
                        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
                        .build()
        );

        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER.ordinal();
        }
        return POST.ordinal();
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER.ordinal()) {
            return HeaderHolder.create(parent);
        }
        return Holder.create(parent);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        if (position == 0) {
            bindHeader((HeaderHolder) holder, ParseUser.getCurrentUser());
        } else {
            bindPost((Holder)holder, getItem(position - 1));
        }
    }

    private void bindHeader(final HeaderHolder holder, final ParseUser user) {
        holder.username.setText(user.getUsername());

        ParseUser.getQuery()
                .whereEqualTo("objectId", user.getObjectId())
                .findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        ParseUser user = objects.get(0);

                        final ParseFile avatarFile = user.getParseFile("avatar");
                        if (avatarFile != null) {
                            Glide.with(holder.itemView)
                                    .load(avatarFile.getUrl())
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(holder.avatar);
                        }
                    }
                });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onChangeAvatarClicked();
            }
        });

        final PopupMenu popupMenu = new PopupMenu(
                holder.itemView.getContext(),
                holder.menuButton
        );
        popupMenu.inflate(R.menu.profile);
        popupMenu.setGravity(Gravity.START | Gravity.BOTTOM);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_logout:
                        callback.onLogoutClicked();
                        return true;
                    default:
                        return false;
                }
            }
        });

        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void bindPost(final Holder holder, final ImagePost post) {
        Glide.with(holder.postImage)
                .asBitmap()
                .load(post.getImage().getUrl())
                .apply(RequestOptions.centerCropTransform())
                .into(holder.postImage);
    }

    static abstract class BaseHolder extends BindableViewHolder {
        public BaseHolder(View itemView) {
            super(itemView);
        }
    }

    static class Holder extends BaseHolder {
        @BindView(R.id.post_image_iv) ImageView postImage;

        public static Holder create(ViewGroup parent) {
            return new Holder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_image_post_square,
                            parent,
                            false
                    )
            );
        }

        public Holder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderHolder extends BaseHolder {
        @BindView(R.id.username_tv) TextView username;
        @BindView(R.id.avatar_iv) ImageView avatar;
        @BindView(R.id.menu_iv) ImageView menuButton;

        static HeaderHolder create(ViewGroup parent) {
            return new HeaderHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_profile_header,
                            parent,
                            false
                    )
            );
        }

        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    static class Differ extends DiffUtil.ItemCallback<ImagePost> {
        static final Differ INSTANCE = new Differ();

        private Differ() {
        }

        @Override
        public boolean areItemsTheSame(ImagePost oldItem, ImagePost newItem) {
            return oldItem.getObjectId().equals(newItem.getObjectId());
        }

        @Override
        public boolean areContentsTheSame(ImagePost oldItem, ImagePost newItem) {
            return oldItem.getImage().getUrl().equals(newItem.getImage().getUrl());
        }
    }
}
