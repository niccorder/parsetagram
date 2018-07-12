package me.niccorder.instagram.internal;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacingHorizontal;
    private final int spacingVertical;
    private int displayMode;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int GRID = 2;

    public MarginItemDecoration(int spacing) {
        this(spacing, spacing, -1);
    }

    public MarginItemDecoration(int spacingHorizontal, int spacingVertical) {
        this(spacingHorizontal, spacingVertical, VERTICAL);
    }

    public MarginItemDecoration(int spacingHorizontal, int spacingVertical, int displayMode) {
        this.spacingHorizontal = spacingHorizontal;
        this.spacingVertical = spacingVertical;
        this.displayMode = displayMode;
    }

    @Override
    public void getItemOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state
    ) {
        int position = parent.getChildViewHolder(view).getAdapterPosition();
        int itemCount = state.getItemCount();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        setSpacingForDirection(outRect, layoutManager, position, itemCount);
    }

    private void setSpacingForDirection(
            Rect outRect,
            RecyclerView.LayoutManager layoutManager,
            int position,
            int itemCount
    ) {
        // Resolve display mode automatically
        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager);
        }

        switch (displayMode) {
            case HORIZONTAL:
                outRect.left = spacingHorizontal;
                outRect.right = position == itemCount - 1 ? spacingHorizontal : 0;
                outRect.top = spacingVertical;
                outRect.bottom = spacingVertical;
                break;
            case VERTICAL:
                outRect.left = spacingHorizontal;
                outRect.right = spacingHorizontal;
                outRect.top = spacingVertical;
                outRect.bottom = position == itemCount - 1 ? spacingVertical : 0;
                break;
            case GRID:
                if (layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    int cols = gridLayoutManager.getSpanCount();
                    int rows = itemCount / cols;

                    outRect.left = spacingHorizontal;
                    outRect.right = position % cols == cols - 1 ? spacingHorizontal : 0;
                    outRect.top = spacingVertical;
                    outRect.bottom = position / cols == rows - 1 ? spacingVertical : 0;
                }
                break;
        }
    }

    private int resolveDisplayMode(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) return GRID;
        if (layoutManager.canScrollHorizontally()) return HORIZONTAL;
        return VERTICAL;
    }
}
