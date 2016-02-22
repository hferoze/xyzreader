package com.example.xyzreader.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.ui.ImageLoaderHelper;
import com.example.xyzreader.ui.ParallaxRecyclerViewImageView;
import com.example.xyzreader.ui.Utils;

public class ArticleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener{

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;

    private boolean mIsRefreshing = false;

    private Context mContext;

    public static Fragment newInstance() {
        ArticleListFragment mFrgment = new ArticleListFragment();
        return mFrgment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_article_list, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar_list);
        ((AppCompatActivity) getContext()).setSupportActionBar(mToolbar);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar_list);
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mCollapsingToolbarLayout.setTitle(" ");

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark, R.color.theme_primary, R.color.theme_accent, R.color.theme_primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_list);
        mAppBarLayout.addOnOffsetChangedListener(this);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mGridLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.grid_column_count));

        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (Utils.isDataAvaialable(mContext)) {
                refresh();
            }else{
                Toast.makeText(mContext, getString(R.string.data_unavailable), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.gridview_saved_instance_ext), mGridLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(getString(R.string.gridview_saved_instance_ext)));
        }
    }

    private void refresh() {
        getActivity().startService(new Intent(getContext(), UpdaterService.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.getCurrentVersion() >= Build.VERSION_CODES.M) {
            mContext = getContext();
        }else{
            mContext = getActivity();
        }
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mRefreshingReceiver);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Math.abs(verticalOffset) >= mAppBarLayout.getTotalScrollRange()) {
            mSwipeRefreshLayout.setEnabled(false);
            if (Utils.getCurrentVersion() >= android.os.Build.VERSION_CODES.M) {
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.theme_primary_dark));
            }
        } else if (Math.abs(verticalOffset) == 0) {
            mSwipeRefreshLayout.setEnabled(true);
            if (Utils.getCurrentVersion() >= android.os.Build.VERSION_CODES.M) {
                getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>{
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public int getItemViewType(int position) {

            return super.getItemViewType(position);
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Fragment viewPagerFragment = ViewPagerFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(getResources().getString(R.string.image_uri_ext),
                            ItemsContract.Items.buildItemUri(getItemId(viewHolder.getAdapterPosition())).toString());
                    bundle.putString(getString(R.string.title_ext), viewHolder.titleView.getText().toString());
                    bundle.putString(getString(R.string.date_ext), DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString());
                    bundle.putString(getString(R.string.author_ext),  mCursor.getString(ArticleLoader.Query.AUTHOR));
                    viewPagerFragment.setArguments(bundle);
                    launchDetailFragment(viewPagerFragment, viewHolder.thumbnailView);
                }
            });

            return viewHolder;
        }

        public void launchDetailFragment(Fragment fr, ImageView imageView){
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left,
                    R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_right)
                    .add(R.id.container, fr, getString(R.string.detail_fragment_tag))
                    .hide(ArticleListFragment.this)
                    .addToBackStack(getString(R.string.list_fragment_tag))
                    .commit();
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));


            ImageLoaderHelper.getInstance(getActivity()).getImageLoader().get(mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                    new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bm = imageContainer.getBitmap();
                            if (bm != null) {
                                Palette.from(bm).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        int mutedColor = palette.getMutedColor(getResources().getColor(R.color.theme_primary));
                                        holder.cardView.setBackgroundColor(mutedColor);
                                    }
                                });
                            }
                        }
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d("TAG", " NO IMAGESSS!!! cause: " + volleyError.getCause() + "\n message " + volleyError.getMessage());
                        }
                    });


            holder.thumbnailView.setImageUrl(
                      mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                      ImageLoaderHelper.getInstance(getActivity()).getImageLoader());

            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
            holder.cardView.setTag(position);

            holder.thumbnailView.setContainerAndRecyclerView(holder.cardView, mRecyclerView);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ParallaxRecyclerViewImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.list_card_view);
            thumbnailView = (ParallaxRecyclerViewImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }
}
