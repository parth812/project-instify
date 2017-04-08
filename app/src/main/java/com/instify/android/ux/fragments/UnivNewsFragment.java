package com.instify.android.ux.fragments;

/**
 * Created by Abhish3k on 3/06/2016. Thanks to Ravi
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.instify.android.R;
import com.instify.android.app.AppController;
import com.thefinestartist.finestwebview.FinestWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnivNewsFragment extends Fragment {
    private String TAG = UnivNewsFragment.class.getSimpleName();

    public UnivNewsFragment() {
    }

    public static UnivNewsFragment newInstance() {
        UnivNewsFragment frag = new UnivNewsFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static final String endpoint = "https://hashbird.com/gogrit.in/workspace/srm-api/univ-news.php";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SimpleStringRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_university_news, container, false);
        // Taking control of the menu options
        setHasOptionsMenu(true);
        // Initialize SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red_primary, R.color.black, R.color.google_blue_900);

        // Setting up recycle view
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_university);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Make it look like something is happening
        showRefreshing();

        // Make the request!
        makeJSONRequest();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showRefreshing();
                makeJSONRequest();
            }
        });
        return rootView;
    }

    public void makeJSONRequest() {
        JsonObjectRequest req = new JsonObjectRequest(endpoint, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray newsItems = response.getJSONArray("newsItems");
                            mAdapter = new SimpleStringRecyclerViewAdapter(getContext(), newsItems);
                            // UI
                            hideRefreshing();
                            // Setting the adapter
                            recyclerView.setAdapter(mAdapter);
                        } catch (JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                            Toast.makeText(getContext(), "JSON Parsing error", Toast.LENGTH_LONG).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getContext(), "Error Receiving University News", Toast.LENGTH_LONG).show();
                hideRefreshing();
            }
        });

        int socketTimeout = 10000;  // 10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(req);
    }

    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {
        private Context mContext;
        private JSONArray newsArray;

        // Constructor
        private SimpleStringRecyclerViewAdapter(Context context, JSONArray newsArray) {
            mContext = context;
            this.newsArray = newsArray;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_univ, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                holder.mTextViewTitle.setText(newsArray.getJSONObject(holder.getAdapterPosition()).getString("title"));
                holder.mTextViewSnip.setText(newsArray.getJSONObject(holder.getAdapterPosition()).getString("snip"));
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            new FinestWebView.Builder(v.getContext()).theme(R.style.FinestWebViewTheme)
                                    .titleDefault("News Update")
                                    .showUrl(false)
                                    .statusBarColorRes(R.color.colorPrimaryDark)
                                    .toolbarColorRes(R.color.colorPrimary)
                                    .titleColorRes(R.color.finestWhite)
                                    .urlColorRes(R.color.colorPrimaryLight)
                                    .iconDefaultColorRes(R.color.finestWhite)
                                    .progressBarColorRes(R.color.finestWhite)
                                    .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                                    .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                                    .stringResCopiedToClipboard(R.string.copied_to_clipboard)
                                    .updateTitleFromHtml(true)
                                    .swipeRefreshColorRes(R.color.colorPrimaryDark)
                                    .menuSelector(R.drawable.selector_light_theme)
                                    .menuTextGravity(Gravity.CENTER)
                                    .menuTextPaddingRightRes(R.dimen.defaultMenuTextPaddingLeft)
                                    .dividerHeight(0)
                                    .gradientDivider(false)
                                    .setCustomAnimations(R.anim.slide_up, R.anim.hold, R.anim.hold, R.anim.slide_down)
                                    .show(newsArray.getJSONObject(holder.getAdapterPosition()).getString("link"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return newsArray.length();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final View mView;
            private final TextView mTextViewTitle, mTextViewSnip;

            private ViewHolder(View view) {
                super(view);
                mView = view;
                mTextViewTitle = (TextView) view.findViewById(R.id.univ_news_title);
                mTextViewSnip = (TextView) view.findViewById(R.id.univ_news_snip);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.removeGroup(R.id.main_menu_group);
        super.onPrepareOptionsMenu(menu);
    }

    private void showRefreshing() {
        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);
    }

    private void hideRefreshing() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }
}
