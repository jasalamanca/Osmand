/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.osmand.plus.widgets.tools;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.design.R;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

//NOTE jsala *BottomSheet* es casi todo android 23. Ver como quitarlo de osmand
/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as
 * a bottom sheet.
 */
public class ExtendedBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

	/**
	 * Callback for monitoring events about bottom sheets.
	 */
	public abstract static class BottomSheetCallback {

		/**
		 * Called when the bottom sheet changes its state.
		 *
		 * @param bottomSheet The bottom sheet view.
		 * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
		 *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
		 *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
		 */
		public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

		/**
		 * Called when the bottom sheet is being dragged.
		 *
		 * @param bottomSheet The bottom sheet view.
		 * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
		 *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
		 *                    is between collapsed and expanded states and from -1 to 0 it is
		 *                    between hidden and collapsed states.
		 */
		public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
	}

	/**
	 * The bottom sheet is dragging.
	 */
	private static final int STATE_DRAGGING = 1;

	/**
	 * The bottom sheet is settling.
	 */
	private static final int STATE_SETTLING = 2;

	/**
	 * The bottom sheet is expanded.
	 */
	public static final int STATE_EXPANDED = 3;

	/**
	 * The bottom sheet is collapsed.
	 */
	private static final int STATE_COLLAPSED = 4;

	/**
	 * The bottom sheet is hidden.
	 */
	public static final int STATE_HIDDEN = 5;

	/**
	 * The bottom sheet is manually opened to a certain height.
	 */
	private static final int STATE_MANUALLY_MOVED = 6;

	/**
	 * @hide
	 */
	@RestrictTo(LIBRARY_GROUP)
	@IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN, STATE_MANUALLY_MOVED})
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {
	}

	/**
	 * Peek at the 16:9 ratio keyline of its parent.
	 * <p>
	 * <p>This can be used as a parameter for {@link #setPeekHeight(int)}.
	 * {@link #getPeekHeight()} will return this when the value is set.</p>
	 */
	private static final int PEEK_HEIGHT_AUTO = -1;
	private static final float HIDE_THRESHOLD = 0.5f;
	private static final float HIDE_FRICTION = 0.1f;
	private static final float MIN_VELOCITY_FOR_SLIDE = 2000;

	private float mMaximumVelocity;
	private int mPeekHeight;
	private boolean mPeekHeightAuto;
	private int mPeekHeightMin;
	private int mMinOffset;
	private int mMaxOffset;
	private final boolean mHideable = false;

	@State
	private
	int mState = STATE_COLLAPSED;

	private ViewDragHelper mViewDragHelper;
	private boolean mIgnoreEvents;
	private int mLastNestedScrollDy;
	private boolean mNestedScrolled;
	private int mParentHeight;

	private WeakReference<V> mViewRef;
	private WeakReference<View> mNestedScrollingChildRef;
	private BottomSheetCallback mCallback;

	private VelocityTracker mVelocityTracker;
	private int mActivePointerId;
	private int mInitialY;
	private boolean mTouchingScrollingChild;

	/**
	 * Default constructor for instantiating BottomSheetBehaviors.
	 */
	public ExtendedBottomSheetBehavior() {
	}

	@Override
	public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
		return new SavedState(super.onSaveInstanceState(parent, child), mState);
	}

	@Override
	public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(parent, child, ss.getSuperState());
		// Intermediate states are restored as collapsed state
		if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
			mState = STATE_COLLAPSED;
		} else {
			mState = ss.state;
		}
	}

	@Override
	public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
		if (parent.getFitsSystemWindows() && !child.getFitsSystemWindows()) {
			child.setFitsSystemWindows(true);
		}
		int savedTop = child.getTop();
		// First let the parent lay it out
		parent.onLayoutChild(child, layoutDirection);
		// Offset the bottom sheet
		mParentHeight = parent.getHeight();
		int peekHeight;
		if (mPeekHeightAuto) {
			if (mPeekHeightMin == 0) {
				mPeekHeightMin = parent.getResources().getDimensionPixelSize(
						R.dimen.design_bottom_sheet_peek_height_min);
			}
			peekHeight = Math.max(mPeekHeightMin, mParentHeight - parent.getWidth() * 9 / 16);
		} else {
			peekHeight = mPeekHeight;
		}
		mMinOffset = Math.max(0, mParentHeight - child.getHeight());
		mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset);
		if (mState == STATE_EXPANDED) {
			child.offsetTopAndBottom(mMinOffset);
		} else if (mHideable && mState == STATE_HIDDEN) {
			child.offsetTopAndBottom(mParentHeight);
		} else if (mState == STATE_COLLAPSED) {
			child.offsetTopAndBottom(mMaxOffset);
		} else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
			child.offsetTopAndBottom(savedTop - child.getTop());
		}
		if (mViewDragHelper == null) {
			mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
		}
		mViewRef = new WeakReference<>(child);
		mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
		if (!child.isShown()) {
			mIgnoreEvents = true;
			return false;
		}
		int action = event.getActionMasked();
		// Record the velocity
		if (action == MotionEvent.ACTION_DOWN) {
			reset();
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		switch (action) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mTouchingScrollingChild = false;
				mActivePointerId = MotionEvent.INVALID_POINTER_ID;
				// Reset the ignore flag
				if (mIgnoreEvents) {
					mIgnoreEvents = false;
					return false;
				}
				break;
			case MotionEvent.ACTION_DOWN:
				int initialX = (int) event.getX();
				mInitialY = (int) event.getY();
				View scroll = mNestedScrollingChildRef != null
						? mNestedScrollingChildRef.get() : null;
				if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
					mActivePointerId = event.getPointerId(event.getActionIndex());
					mTouchingScrollingChild = true;
				}
				mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
						!parent.isPointInChildBounds(child, initialX, mInitialY);
				break;
		}
		if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
			return true;
		}
		// We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
		// it is not the top most view of its parent. This is not necessary when the touch event is
		// happening over the scrolling content as nested scrolling logic handles that case.
		View scroll = mNestedScrollingChildRef.get();
		return action == MotionEvent.ACTION_MOVE && scroll != null &&
				!mIgnoreEvents && mState != STATE_DRAGGING &&
				!parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) &&
				Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
	}

	@Override
	public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
		if (!child.isShown()) {
			return false;
		}
		int action = event.getActionMasked();
		if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
			return true;
		}
		if (null != mViewDragHelper)
			mViewDragHelper.processTouchEvent(event);
		// Record the velocity
		if (action == MotionEvent.ACTION_DOWN) {
			reset();
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		// The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
		// to capture the bottom sheet in case it is not captured and the touch slop is passed.
		if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
			if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop()) {
				mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
			}
		}
		return !mIgnoreEvents;
	}

	@Override
	public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
									   @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes) {
		mLastNestedScrollDy = 0;
		mNestedScrolled = false;
		return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
	}

	@Override
	public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx,
								  int dy, @NonNull int[] consumed) {
		View scrollingChild = mNestedScrollingChildRef.get();
		if (target != scrollingChild) {
			return;
		}
		int currentTop = child.getTop();
		int newTop = currentTop - dy;
		if (dy > 0) { // Upward
			if (newTop < mMinOffset) {
				consumed[1] = currentTop - mMinOffset;
				child.offsetTopAndBottom(-consumed[1]);
				setStateInternal(STATE_EXPANDED);
			} else {
				consumed[1] = dy;
				child.offsetTopAndBottom(-dy);
				setStateInternal(STATE_DRAGGING);
			}
		} else if (dy < 0) { // Downward
			if (!target.canScrollVertically(-1)) {
				if (newTop <= mMaxOffset || mHideable) {
					consumed[1] = dy;
					child.offsetTopAndBottom(-dy);
					setStateInternal(STATE_DRAGGING);
				} else {
					consumed[1] = currentTop - mMaxOffset;
					child.offsetTopAndBottom(-consumed[1]);
					setStateInternal(STATE_COLLAPSED);
				}
			}
		}
		dispatchOnSlide(child.getTop());
		mLastNestedScrollDy = dy;
		mNestedScrolled = true;
	}

	@Override
	public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target) {
		if (child.getTop() == mMinOffset) {
			setStateInternal(STATE_EXPANDED);
			return;
		}
		if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef.get()
				|| !mNestedScrolled) {
			return;
		}
		int top;
		int targetState;
		if (mLastNestedScrollDy > 0) {
			if (Math.abs(getYVelocity()) > MIN_VELOCITY_FOR_SLIDE) {
				top = mMinOffset;
				targetState = STATE_EXPANDED;
			} else {
				top = child.getTop();
				targetState = STATE_MANUALLY_MOVED;
			}
		} else if (mHideable && shouldHide(child, getYVelocity())) {
			top = mParentHeight;
			targetState = STATE_HIDDEN;
		} else if (mLastNestedScrollDy == 0) {
			top = child.getTop();
			targetState = STATE_MANUALLY_MOVED;
		} else {
			if (Math.abs(getYVelocity()) > MIN_VELOCITY_FOR_SLIDE) {
				top = mMaxOffset;
				targetState = STATE_COLLAPSED;
			} else {
				top = child.getTop();
				targetState = STATE_MANUALLY_MOVED;
			}
		}
		if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
			setStateInternal(STATE_SETTLING);
			child.postOnAnimation(new SettleRunnable(child, targetState));
		} else {
			setStateInternal(targetState);
		}
		mNestedScrolled = false;
	}

	@Override
	public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target,
									float velocityX, float velocityY) {
		return target == mNestedScrollingChildRef.get() &&
				(mState != STATE_EXPANDED ||
						super.onNestedPreFling(coordinatorLayout, child, target,
								velocityX, velocityY));
	}

	/**
	 * Sets the height of the bottom sheet when it is collapsed.
	 *
	 * @param peekHeight The height of the collapsed bottom sheet in pixels, or
	 *                   {@link #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically
	 *                   at 16:9 ratio keyline.
	 * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
	 */
	public final void setPeekHeight(int peekHeight) {
		boolean layout = false;
		if (peekHeight == PEEK_HEIGHT_AUTO) {
			if (!mPeekHeightAuto) {
				mPeekHeightAuto = true;
				layout = true;
			}
		} else if (mPeekHeightAuto || mPeekHeight != peekHeight) {
			mPeekHeightAuto = false;
			mPeekHeight = Math.max(0, peekHeight);
			mMaxOffset = mParentHeight - peekHeight;
			layout = true;
		}
		if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
			V view = mViewRef.get();
			if (view != null) {
				view.requestLayout();
			}
		}
	}

	/**
	 * Sets a callback to be notified of bottom sheet events.
	 *
	 * @param callback The callback to notify when bottom sheet events occur.
	 */
	public void setBottomSheetCallback(BottomSheetCallback callback) {
		mCallback = callback;
	}

	/**
	 * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
	 * animation.
	 *
	 * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, or
	 *              {@link #STATE_HIDDEN}.
	 */
	public final void setState(final @State int state) {
		if (state == mState) {
			return;
		}
		if (mViewRef == null) {
			// The view is not laid out yet; modify mState and let onLayoutChild handle it later
			if (state == STATE_COLLAPSED || state == STATE_EXPANDED ||
					(mHideable && state == STATE_HIDDEN)) {
				mState = state;
			}
			return;
		}
		final V child = mViewRef.get();
		if (child == null) {
			return;
		}
		// Start the animation; wait until a pending layout if there is one.
		ViewParent parent = child.getParent();
		if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child)) {
			child.post(() -> startSettlingAnimation(child, state));
		} else {
			startSettlingAnimation(child, state);
		}
	}

	private void setStateInternal(@State int state) {
		if (mState == state) {
			return;
		}
		mState = state;
		View bottomSheet = mViewRef.get();
		if (bottomSheet != null && mCallback != null) {
			mCallback.onStateChanged(bottomSheet, state);
		}
	}

	private void reset() {
		mActivePointerId = ViewDragHelper.INVALID_POINTER;
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private boolean shouldHide(View child, float yvel) {
		if (child.getTop() < mMaxOffset) {
			// It should not hide, but collapse.
			return false;
		}
		final float newTop = child.getTop() + yvel * HIDE_FRICTION;
		return Math.abs(newTop - mMaxOffset) / (float) mPeekHeight > HIDE_THRESHOLD;
	}

	@VisibleForTesting
	private View findScrollingChild(View view) {
		if (ViewCompat.isNestedScrollingEnabled(view)) {
			return view;
		}
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0, count = group.getChildCount(); i < count; i++) {
				View scrollingChild = findScrollingChild(group.getChildAt(i));
				if (scrollingChild != null) {
					return scrollingChild;
				}
			}
		}
		return null;
	}

	private float getYVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
		return mVelocityTracker.getYVelocity(mActivePointerId);
	}

	private void startSettlingAnimation(View child, int state) {
		int top;
		if (state == STATE_COLLAPSED) {
			top = mMaxOffset;
		} else if (state == STATE_EXPANDED) {
			top = mMinOffset;
		} else if (mHideable && state == STATE_HIDDEN) {
			top = mParentHeight;
		} else {
			throw new IllegalArgumentException("Illegal state argument: " + state);
		}
		if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
			setStateInternal(STATE_SETTLING);
			child.postOnAnimation(new SettleRunnable(child, state));
		} else {
			setStateInternal(state);
		}
	}

	private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {
		@Override
		public boolean tryCaptureView(@NonNull View child, int pointerId) {
			if (mState == STATE_DRAGGING) {
				return false;
			}
			if (mTouchingScrollingChild) {
				return false;
			}
			if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
				View scroll = mNestedScrollingChildRef.get();
				if (scroll != null && scroll.canScrollVertically(-1)) {
					// Let the content scroll up
					return false;
				}
			}
			return mViewRef != null && mViewRef.get() == child;
		}

		@Override
		public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
			dispatchOnSlide(top);
		}

		@Override
		public void onViewDragStateChanged(int state) {
			if (state == ViewDragHelper.STATE_DRAGGING) {
				setStateInternal(STATE_DRAGGING);
			}
		}

		@Override
		public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
			int top;
			@State int targetState;
			if (yvel < 0) { // Moving up
				top = mMinOffset;
				targetState = STATE_EXPANDED;
			} else if (mHideable && shouldHide(releasedChild, yvel)) {
				top = mParentHeight;
				targetState = STATE_HIDDEN;
			} else if (yvel == 0.f) {
				int currentTop = releasedChild.getTop();
				if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
					top = mMinOffset;
					targetState = STATE_EXPANDED;
				} else {
					top = mMaxOffset;
					targetState = STATE_COLLAPSED;
				}
			} else {
				top = mMaxOffset;
				targetState = STATE_COLLAPSED;
			}
			if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
				setStateInternal(STATE_SETTLING);
				releasedChild.postOnAnimation(new SettleRunnable(releasedChild, targetState));
			} else {
				setStateInternal(targetState);
			}
		}

		@Override
		public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
			return MathUtils.clamp(top, mMinOffset, mHideable ? mParentHeight : mMaxOffset);
		}

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			return child.getLeft();
		}

		@Override
		public int getViewVerticalDragRange(@NonNull View child) {
			if (mHideable) {
				return mParentHeight - mMinOffset;
			} else {
				return mMaxOffset - mMinOffset;
			}
		}
	};

	private void dispatchOnSlide(int top) {
		View bottomSheet = mViewRef.get();
		if (bottomSheet != null && mCallback != null) {
			if (top > mMaxOffset) {
				mCallback.onSlide(bottomSheet, (float) (mMaxOffset - top) /
						(mParentHeight - mMaxOffset));
			} else {
				mCallback.onSlide(bottomSheet,
						(float) (mMaxOffset - top) / ((mMaxOffset - mMinOffset)));
			}
		}
	}

	private class SettleRunnable implements Runnable {

		private final View mView;

		@State
		private final int mTargetState;

		SettleRunnable(View view, @State int targetState) {
			mView = view;
			mTargetState = targetState;
		}

		@Override
		public void run() {
			if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
				mView.postOnAnimation(this);
			} else {
				setStateInternal(mTargetState);
			}
		}
	}

	protected static class SavedState extends AbsSavedState {
		@State
		final int state;

		SavedState(Parcel source, ClassLoader loader) {
			super(source, loader);
			//noinspection ResourceType
			state = source.readInt();
		}

		SavedState(Parcelable superState, @State int state) {
			super(superState);
			this.state = state;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(state);
		}

		public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in, ClassLoader loader) {
				return new SavedState(in, loader);
			}

			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in, null);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	/**
	 * A utility function to get the {@link ExtendedBottomSheetBehavior} associated with the {@code view}.
	 *
	 * @param view The {@link View} with {@link ExtendedBottomSheetBehavior}.
	 * @return The {@link ExtendedBottomSheetBehavior} associated with the {@code view}.
	 */
	public static <V extends View> ExtendedBottomSheetBehavior<V> from(V view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (!(params instanceof CoordinatorLayout.LayoutParams)) {
			throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
		}
		CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
				.getBehavior();
		if (!(behavior instanceof ExtendedBottomSheetBehavior)) {
			throw new IllegalArgumentException(
					"The view is not associated with BottomSheetBehavior");
		}
		return (ExtendedBottomSheetBehavior<V>) behavior;
	}
}