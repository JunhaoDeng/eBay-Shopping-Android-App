package ebay_app.ebay_product_search.adapters;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPageAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();
    private Context context;

    public ViewPageAdapter(Context context, FragmentManager mFragmentManager) {
        super(mFragmentManager);
        this.context = context;
    }

    public void addFragment(Fragment fragment, int stringId) {
        fragmentList.add(fragment);
        fragmentTitles.add(context.getString(stringId));
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }
}
