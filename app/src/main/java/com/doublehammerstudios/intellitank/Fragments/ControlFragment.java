package com.doublehammerstudios.intellitank.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doublehammerstudios.intellitank.Adapters.ViewPager.ControlTabViewPagerAdapter;
import com.doublehammerstudios.intellitank.Fragments.Control.FeedingControlFragment;
import com.doublehammerstudios.intellitank.Fragments.Control.FilteringControlFragment;
import com.doublehammerstudios.intellitank.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ControlFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    ControlTabViewPagerAdapter controlTabViewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_control, container, false);

        tabLayout = view.findViewById(R.id.tabSystem);
        viewPager = view.findViewById(R.id.viewPagerTabSystem);

        controlTabViewPagerAdapter = new ControlTabViewPagerAdapter(getActivity());
        viewPager.setAdapter(controlTabViewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
        return view;
    }
}