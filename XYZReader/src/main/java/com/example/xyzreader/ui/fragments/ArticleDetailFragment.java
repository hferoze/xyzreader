package com.example.xyzreader.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ArticleListActivity;
import com.example.xyzreader.ui.DynamicHeightNetworkImageView;
import com.example.xyzreader.ui.ImageLoaderHelper;
import com.example.xyzreader.ui.Utils;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = "ArticleDetailFragment";

    private static final String IS_TEXT_SHOWING_KEY = "is_text_showing_key";
    public static final String ARG_ITEM_ID = "item_id";

    private Context mContext;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;

    private Toolbar mToolBar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private NestedScrollView mNestedScrollView;
    private AppBarLayout mAppbarLayout;

    private TextView mTitleView;
    private TextView mBylineView;
    private TextView mBodyView;
    private DynamicHeightNetworkImageView mPhotoView;
    private Bitmap mCurrentBitmap;
    private FrameLayout mTextContainer;

    private boolean mIsTextShowing = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (Utils.getCurrentVersion() >= Build.VERSION_CODES.M) {
            mContext = getContext();
        } else {
            mContext = getActivity();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_TEXT_SHOWING_KEY, mIsTextShowing);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        mToolBar = (Toolbar) mRootView.findViewById(R.id.detail_toolbar);
        ((AppCompatActivity) mContext).setSupportActionBar(mToolBar);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mTitleView = (TextView) mRootView.findViewById(R.id.article_title);
        mBylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        mBodyView = (TextView) mRootView.findViewById(R.id.article_body);
        mPhotoView = (DynamicHeightNetworkImageView) mRootView.findViewById(R.id.header);

        mNestedScrollView = (NestedScrollView) mRootView.findViewById(R.id.scrollView);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(mTitleView.getText() + "\n" + mBylineView.getText())
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        mAppbarLayout = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mAppbarLayout.addOnOffsetChangedListener(this);

        mTextContainer = (FrameLayout) mRootView.findViewById(R.id.text_container);

        if (savedInstanceState != null && savedInstanceState.containsKey(IS_TEXT_SHOWING_KEY)) {
            mIsTextShowing = savedInstanceState.getBoolean(IS_TEXT_SHOWING_KEY);
        }

        if (!mIsTextShowing) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ObjectAnimator bottomUp = ObjectAnimator.ofFloat(mTextContainer, "y", mRootView.getHeight(), 0);
                    bottomUp.setRepeatCount(0);
                    bottomUp.setDuration(600);
                    bottomUp.start();
                    mIsTextShowing = true;
                }
            }, 10);
        } else {
            mTextContainer.setTranslationY(0);
        }
    }

    /*
    * Dynamically change status bar color to the palette
    */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Math.abs(verticalOffset) >= mAppbarLayout.getTotalScrollRange()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().getWindow().setStatusBarColor(mMutedColor);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

    public void bindViews() {
        if (mCursor != null) {
            try {
                mCollapsingToolbarLayout.setTitle(" ");
                mTitleView.setText((mCursor.getString(ArticleLoader.Query.TITLE)));
                mBylineView.setText(Html.fromHtml("<font color='#000000'>" +
                        DateUtils.getRelativeTimeSpanString(
                                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                        + "</font> by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                mBodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

                //Log.d("TAg", "Detail bindViews " + mCursor.getString(ArticleLoader.Query.TITLE));

                ImageLoaderHelper.getInstance(mContext).getImageLoader().get(mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                        new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                mCurrentBitmap = imageContainer.getBitmap();
                                if (mCurrentBitmap != null) {
                                    Palette.from(mCurrentBitmap).generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            try {
                                                if (getActivity()!=null) {
                                                    mMutedColor = palette.getMutedColor(getResources().getColor(R.color.theme_primary));
                                                    mTitleView.setTextColor(mMutedColor);
                                                    mBylineView.setTextColor(mMutedColor);
                                                }
                                            }catch (IllegalStateException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    try {
                                        if (getActivity()!=null) {
                                            mPhotoView.setImageUrl(
                                                    mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                                                    ImageLoaderHelper.getInstance(mContext).getImageLoader());
                                            mPhotoView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

                                            if (mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO) < 1 &&
                                                    Utils.getCurrentOrientation(mContext) == Configuration.ORIENTATION_PORTRAIT) {
                                                setOverlapTop();
                                                mPhotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            } else if (Utils.getCurrentOrientation(mContext) == Configuration.ORIENTATION_LANDSCAPE) {
                                                setOverlapTop();
                                                mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                                            }
                                        }
                                    } catch (NullPointerException | IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            mTitleView.setText("N/A");
            mBylineView.setText("N/A");
            mBodyView.setText("N/A");
        }
    }

    public void setOverlapTop() {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mNestedScrollView.getLayoutParams();
        AppBarLayout.ScrollingViewBehavior behavior =
                (AppBarLayout.ScrollingViewBehavior) params.getBehavior();
        behavior.setOverlayTop((int) (Utils.getPx(mContext,
                Utils.getScreenHeightDpi(mContext) / getResources().getInteger(R.integer.detail_coordinator_layout_overlay_factor))));
    }
}
