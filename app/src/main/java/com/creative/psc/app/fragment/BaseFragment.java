package com.creative.psc.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;

import com.creative.psc.app.util.UtilClass;

/**
 * Created by GS on 2017-08-21.
 */
public class BaseFragment extends Fragment {
    public final String TAG = getClass().getSimpleName();
    Animation slideUp;
    Animation slideDown;
    boolean isDown = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        UtilClass.logD(TAG, "onCreate");
        super.onCreate(savedInstanceState);

//        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
//        slideUp.setAnimationListener(animationListener);
//        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
//        slideDown.setAnimationListener(animationListener);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        UtilClass.logD(TAG, "onResume");
        super.onResume();
    }


    @Override
    public void onDestroy() {
        UtilClass.logD(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @Override
    public void onStart() {
        super.onStart();
        UtilClass.logD(TAG, "onStart");

    }
}
