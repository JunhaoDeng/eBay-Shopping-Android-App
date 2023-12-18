package ebay_app.ebay_product_search;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import ebay_app.ebay_product_search.adapters.ViewPageAdapter;
import ebay_app.ebay_product_search.fragments.SearchFragment;
import ebay_app.ebay_product_search.fragments.WishlistFragment;
import edu.gyaneshm.ebay_product_search.R;


public class MainActivity extends AppCompatActivity {
    private ViewPageAdapter mViewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.main_activity_layout);

        getSupportActionBar().setElevation(0);

        ViewPager viewPager = findViewById(R.id.main_container);
        setUpViewPager();
        viewPager.setAdapter(mViewPageAdapter);
        setTitle("Product Search");
    }

    private void setUpViewPager() {
        mViewPageAdapter = new ViewPageAdapter(this, getSupportFragmentManager());
        mViewPageAdapter.addFragment(new SearchFragment(), R.string.tab_text_1);
        mViewPageAdapter.addFragment(new WishlistFragment(), R.string.wish_list);
    }
}

