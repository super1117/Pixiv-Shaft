package ceui.lisa.fragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import ceui.lisa.R;
import ceui.lisa.activities.Shaft;
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding;

public class FragmentPv extends BaseFragment<ViewpagerWithTablayoutBinding> {

    private static final String[] CHINESE_TITLES = new String[]{
            Shaft.getContext().getString(R.string.type_illust),
            Shaft.getContext().getString(R.string.type_manga)};

    @Override
    public void initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout;
    }

    @Override
    public void initView(View view) {
        ImageView head = view.findViewById(R.id.head);
        ViewGroup.LayoutParams headParams = head.getLayoutParams();
        headParams.height = Shaft.statusHeight;
        head.setLayoutParams(headParams);
        baseBind.toolbar.setNavigationOnClickListener(v -> mActivity.finish());
        baseBind.toolbar.setTitle("特辑");
        baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(), 0) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return FragmentPivision.newInstance("illust");
                } else if (position == 1) {
                    return FragmentPivision.newInstance("manga");
                } else {
                    return new Fragment();
                }
            }

            @Override
            public int getCount() {
                return CHINESE_TITLES.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return CHINESE_TITLES[position];
            }
        });
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager);
    }
}
