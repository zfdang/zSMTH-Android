package com.zfdang.zsmth_android.helpers;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

// Workaround for https://code.google.com/p/android/issues/detail?id=177195
// Based off of solution originally found here: http://stackoverflow.com/a/31140112/1317564
@SuppressWarnings("unused") public class CustomScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {
  private AppBarLayout appBarLayout;
  private boolean onAnimationRunnablePosted = false;
  private int appBarTop = Integer.MAX_VALUE;

  @SuppressWarnings("unused") public CustomScrollingViewBehavior() {

  }

  @SuppressWarnings("unused") public CustomScrollingViewBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target,
      int nestedScrollAxes) {
    if (appBarLayout != null) {
      // We need to check from when a scroll is started, as we may not have had the chance to update the layout at
      // the start of a scroll or fling event.
      startAnimationRunnable(child, appBarLayout);
    }
    return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
  }

  @Override public boolean onMeasureChild(CoordinatorLayout parent, final View child, int parentWidthMeasureSpec, int widthUsed,
      int parentHeightMeasureSpec, int heightUsed) {
    if (appBarLayout != null) {
      final int bottomPadding = calculateBottomPadding(appBarLayout);
      if (bottomPadding != child.getPaddingBottom()) {
        // We need to update the padding in onMeasureChild as otherwise we won't have the correct padding in
        // place when the view is flung, and the changes done in onDependentViewChanged will only take effect on
        // the next animation frame, which means it will be out of sync with the new scroll offset. This is only
        // needed when the view is flung -- when dragged with a finger, things work fine with just
        // implementing onDependentViewChanged().
        child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), bottomPadding);
      }
    }

    return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
  }

  @Override public boolean onDependentViewChanged(CoordinatorLayout parent, final View child, final View dependency) {
    if (appBarLayout == null) appBarLayout = (AppBarLayout) dependency;

    final boolean result = super.onDependentViewChanged(parent, child, dependency);
    final int bottomPadding = calculateBottomPadding(appBarLayout);
    final boolean paddingChanged = bottomPadding != child.getPaddingBottom();
    if (paddingChanged) {
      // If we've changed the padding, then update the child and make sure a layout is requested.
      child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), bottomPadding);
      child.requestLayout();
    }

    // Even if we didn't change the padding, if onDependentViewChanged was called then that means that the app bar
    // layout was changed or was flung. In that case, we want to check for these changes over the next few animation
    // frames so that we can ensure that we capture all the changes and update the view pager padding to match.
    startAnimationRunnable(child, dependency);
    return paddingChanged || result;
  }

  // Calculate the padding needed to keep the bottom of the view pager's content at the same location on the screen.
  private int calculateBottomPadding(AppBarLayout dependency) {
    final int totalScrollRange = dependency.getTotalScrollRange();
    if (appBarTop == Integer.MAX_VALUE) {
      appBarTop = dependency.getTop();
    }
    return totalScrollRange + dependency.getTop() - appBarTop;
  }

  private void startAnimationRunnable(final View child, final View dependency) {
    if (onAnimationRunnablePosted) return;

    final int onPostChildTop = child.getTop();
    final int onPostDependencyTop = dependency.getTop();
    onAnimationRunnablePosted = true;
    // Start looking for changes at the beginning of each animation frame. If there are any changes, we have to
    // ensure that layout is run again so that we can update the padding to take the changes into account.
    child.postOnAnimation(new Runnable() {
      private static final int MAX_COUNT_OF_FRAMES_WITH_NO_CHANGES = 5;
      private int previousChildTop = onPostChildTop;
      private int previousDependencyTop = onPostDependencyTop;
      private int countOfFramesWithNoChanges;

      @Override public void run() {
        // Make sure we request a layout at the beginning of each animation frame, until we notice a few
        // frames where nothing changed.
        final int currentChildTop = child.getTop();
        final int currentDependencyTop = dependency.getTop();
        boolean hasChanged = false;

        if (currentChildTop != previousChildTop) {
          previousChildTop = currentChildTop;
          hasChanged = true;
          countOfFramesWithNoChanges = 0;
        }
        if (currentDependencyTop != previousDependencyTop) {
          previousDependencyTop = currentDependencyTop;
          hasChanged = true;
          countOfFramesWithNoChanges = 0;
        }
        if (!hasChanged) {
          countOfFramesWithNoChanges++;
        }
        if (countOfFramesWithNoChanges <= MAX_COUNT_OF_FRAMES_WITH_NO_CHANGES) {
          // We can still look for changes on subsequent frames.
          child.requestLayout();
          child.postOnAnimation(this);
        } else {
          // We've encountered enough frames with no changes. Do a final layout request, and don't repost.
          child.requestLayout();
          onAnimationRunnablePosted = false;
        }
      }
    });
  }
}