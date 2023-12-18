package ebay_app.ebay_product_search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ebay_app.ebay_product_search.ProductDetailActivity;
import ebay_app.ebay_product_search.adapters.SearchItemRecyclerViewAdapter;
import ebay_app.ebay_product_search.data.ProductData;
import ebay_app.ebay_product_search.data.SearchResultData;
import ebay_app.ebay_product_search.listeners.ItemClickListener;
import ebay_app.ebay_product_search.models.SearchFormModel;
import ebay_app.ebay_product_search.models.SearchResultModel;
import ebay_app.ebay_product_search.listeners.DataFetchingListener;
import ebay_app.ebay_product_search.shared.Utils;
import edu.gyaneshm.ebay_product_search.R;

public class SearchResultActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private AppCompatActivity activity;

    private LinearLayout mProgressBarContainer;
    private LinearLayout mSearchResultsContainer;
    private TextView mTotalItemsTextView;
    private TextView mErrorTextView;
    private RecyclerView mSearchItemsRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private SearchResultData mSearchResultData;
    ArrayList<SearchResultModel> mSearchResults = null;

    private SearchItemRecyclerViewAdapter searchItemRecyclerViewAdapter = null;

    private DataFetchingListener dataFetchingListener = new DataFetchingListener() {
        @Override
        public void dataSuccessFullyFetched() {
            if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
            initiate();
        }
    };

    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onItemClicked(int position) {
            if (mSearchResults != null) {
                ProductData.getInstance().setItem(mSearchResults.get(position));
                Intent intent = new Intent(getApplicationContext(), ProductDetailActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.search_results_activity_layout);
        mActionBar = getSupportActionBar();
        setUpActionBar();

        mProgressBarContainer = findViewById(R.id.search_results_progress_bar_container);
        mSearchResultsContainer = findViewById(R.id.search_results_container);
        mSearchItemsRecyclerView = findViewById(R.id.search_results_items);
        mErrorTextView = findViewById(R.id.search_results_error);
        mSwipeRefreshLayout = findViewById(R.id.search_results_refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               SearchFormModel data = mSearchResultData.getSearchFormData();
                Log.i("Refresh", "onRefresh: "+data.toString());
                mSearchResultData.setSearchFormData(data);

                mSearchResultData.fetchData(activity);
            }
        });
        mSearchResultData = SearchResultData.getInstance();
        mSearchResultData.registerCallback(dataFetchingListener);

        initiate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchResultData.unregisterCallback(dataFetchingListener);
        mSearchResultData.cancelRequest();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setUpActionBar() {
        mActionBar.setTitle(R.string.search_results_title);
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initiate() {
        mErrorTextView.setVisibility(View.GONE);
        mSearchResultsContainer.setVisibility(View.GONE);
        mProgressBarContainer.setVisibility(View.GONE);
        mSearchResults = null;

        if (mSearchResultData.isDataFetched()) {
            checkForErrors();
        } else {
            mProgressBarContainer.setVisibility(View.VISIBLE);
            mSearchResultData.fetchData(this);
        }
    }

    private void checkForErrors() {
        String errorMessage = mSearchResultData.getErrorMessage();
        if (errorMessage == null) {
            setUpSearchItemRecycleView();
            return;
        }

        Utils.showToast(errorMessage);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void setUpSearchItemRecycleView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mSearchResults = mSearchResultData.getSearchResults();
        mSearchItemsRecyclerView.setHasFixedSize(true);
        mSearchItemsRecyclerView.setLayoutManager(gridLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, gridLayoutManager.getOrientation());
        mSearchItemsRecyclerView.addItemDecoration(dividerItemDecoration);
        DividerItemDecoration dividerItemDecoration2 = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        mSearchItemsRecyclerView.addItemDecoration(dividerItemDecoration2);

        searchItemRecyclerViewAdapter = new SearchItemRecyclerViewAdapter(this, mSearchResults, itemClickListener);
        mSearchItemsRecyclerView.setAdapter(searchItemRecyclerViewAdapter);

        SearchFormModel searchFormModel = mSearchResultData.getSearchFormData();

        String color = "#" + Integer.toHexString(getColor(R.color.search_results_total_items_text_color)).substring(2);
        String totalItems = getString(R.string.search_results_total_items, color, mSearchResults.size(), color, searchFormModel.getKeyword());

        mSearchResultsContainer.setVisibility(View.VISIBLE);
    }
}
