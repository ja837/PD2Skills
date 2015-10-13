package com.dawgandpony.pd2skills.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dawgandpony.pd2skills.Activities.EditBuildActivity;
import com.dawgandpony.pd2skills.Consts.Trees;
import com.dawgandpony.pd2skills.Database.MySQLiteHelper;
import com.dawgandpony.pd2skills.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie on 13/10/2015.
 */
public class SkillTreeParentFragment extends Fragment {

    Adapter mAdapter;

    public static SkillTreeParentFragment newInstance(int currentTree){
        SkillTreeParentFragment fragment = new SkillTreeParentFragment();
        Bundle args = new Bundle();
        args.putInt(EditBuildActivity.SKILL_TREE_INDEX, currentTree);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new Adapter(getChildFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_skilltree_parent, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {


        for (int i = Trees.MASTERMIND; i <= Trees.FUGITIVE; i++){
            String title = getResources().getStringArray(R.array.skill_trees)[i];
            mAdapter.addFragment(SkillTreeFragment.newInstance(i), title);
        }

        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(getArguments().getInt(EditBuildActivity.SKILL_TREE_INDEX));
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
