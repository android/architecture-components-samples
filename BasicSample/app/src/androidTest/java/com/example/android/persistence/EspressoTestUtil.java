package com.example.android.persistence;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.rule.ActivityTestRule;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Utility methods for espresso tests.
 */
public class EspressoTestUtil {
    /**
     * Disables progress bar animations for the views of the given activity rule
     *
     * @param activityTestRule The activity rule whose views will be checked
     */
    public static void disableAnimations(
            ActivityTestRule<? extends FragmentActivity> activityTestRule) {
        activityTestRule.getActivity().getSupportFragmentManager()
                .registerFragmentLifecycleCallbacks(
                        new FragmentManager.FragmentLifecycleCallbacks() {
                            @Override
                            public void onFragmentViewCreated(@NonNull FragmentManager fm,
                                    @NonNull Fragment f, @NonNull View v,
                                    @Nullable Bundle savedInstanceState) {
                                // traverse all views, if any is a progress bar, replace its animation
                                traverseViews(v);
                            }
                        }, true);
    }

    private static void traverseViews(View view) {
        if (view instanceof ViewGroup) {
            traverseViewGroup((ViewGroup) view);
        } else {
            if (view instanceof ProgressBar) {
                disableProgressBarAnimation((ProgressBar) view);
            }
        }
    }

    private static void traverseViewGroup(ViewGroup view) {
        if (view instanceof RecyclerView) {
            disableRecyclerViewAnimations((RecyclerView) view);
        } else {
            final int count = view.getChildCount();
            for (int i = 0; i < count; i++) {
                traverseViews(view.getChildAt(i));
            }
        }
    }

    private static void disableRecyclerViewAnimations(RecyclerView view) {
        view.setItemAnimator(null);
    }

    /**
     * necessary to run tests on older API levels where progress bar uses handler loop to animate.
     *
     * @param progressBar The progress bar whose animation will be swapped with a drawable
     */
    private static void disableProgressBarAnimation(ProgressBar progressBar) {
        progressBar.setIndeterminateDrawable(new ColorDrawable(Color.BLUE));
    }
}