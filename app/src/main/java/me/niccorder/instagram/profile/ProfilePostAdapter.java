package me.niccorder.instagram.profile;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.niccorder.instagram.R;
import me.niccorder.instagram.internal.BindableViewHolder;
import me.niccorder.instagram.model.ImagePost;

public class ProfilePostAdapter extends ListAdapter<ImagePost, ProfilePostAdapter.Holder> {


    public ProfilePostAdapter() {
        super(
                new AsyncDifferConfig.Builder<>(Differ.INSTANCE)
                        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
                        .build()
        );
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return Holder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final ImagePost current = getItem(position);

        Glide.with(holder.postImage)
                .asBitmap()
                .load(current.getImage().getUrl())
                .apply(RequestOptions.centerCropTransform())
                .into(holder.postImage);
    }

    static class Holder extends BindableViewHolder {
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
