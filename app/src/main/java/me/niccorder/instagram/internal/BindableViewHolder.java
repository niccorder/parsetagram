package me.niccorder.instagram.internal;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

public abstract class BindableViewHolder extends RecyclerView.ViewHolder {

    public BindableViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
