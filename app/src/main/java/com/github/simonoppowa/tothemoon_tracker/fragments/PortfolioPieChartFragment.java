package com.github.simonoppowa.tothemoon_tracker.fragments;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.utils.PicassoUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PortfolioPieChartFragment extends Fragment {

    private static final String COIN_LIST_KEY = "coinListKey";
    private static final String TRANSACTION_KEY = "transactionKey";

    private ArrayList<Coin> mCoinList;
    private List<Transaction> mTransactionList;

    private Map<String, Palette> mPalettes;

    private int callsRemaining;

    // Picasso doesn't hold a strong reference to the Target object
    private List<Target> mTarget;

    @BindView(R.id.portfolio_pie_chart)
    PieChart mChart;

    public PortfolioPieChartFragment() {
        // Required empty public constructor
    }

    public static PortfolioPieChartFragment newInstance(ArrayList<Coin> coins, List<Transaction> transactions) {
        PortfolioPieChartFragment fragment = new PortfolioPieChartFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(COIN_LIST_KEY, coins);
        args.putParcelableArrayList(TRANSACTION_KEY, (ArrayList<? extends Parcelable>) transactions);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCoinList = getArguments().getParcelableArrayList(COIN_LIST_KEY);
            mTransactionList = getArguments().getParcelableArrayList(TRANSACTION_KEY);

            mPalettes = new HashMap<>();
        }  else {
        throw new NullPointerException("No bundle was passed to PortfolioPieChartFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio_pie_chart, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);

        createPieChart();

        return view;
    }

    private void createPieChart() {

        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.getLegend().setEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(getResources().getColor(R.color.colorPrimary));
        mChart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        mChart.setTransparentCircleColor(R.color.colorPrimary);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);
        mChart.setCenterText(getString(R.string.portfolio_name));
        mChart.setCenterTextColor(getResources().getColor(R.color.defaultTextColor));

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(false);

        mChart.setNoDataText(getString(R.string.graph_no_data));
        mChart.setNoDataTextColor(R.color.colorAccent);

        getColorsFromImages();
    }

    private void setData() {

        ArrayList<PieEntry> entries = new ArrayList<>();

        for(Coin coin : mCoinList) {
            Transaction coinTransaction = mTransactionList.get(mTransactionList.indexOf(new Transaction(coin.getName(),
                0, 0)));
            // Calculate current coin price * quantity
            BigDecimal coinValue = new BigDecimal(coin.getCurrentPrice() * coinTransaction.getQuantity());
            entries.add(new PieEntry(coinValue.floatValue(), coin.getFullName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.portfolio_name));

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> chartColors = new ArrayList<>();
        ArrayList<Integer> textColors = new ArrayList<>();

        for(Coin coin : mCoinList) {
            Palette.Swatch chartSwatch = mPalettes.get(coin.getName()).getDominantSwatch();
            chartColors.add(chartSwatch.getRgb());
            textColors.add(chartSwatch.getBodyTextColor());
        }

        dataSet.setColors(chartColors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColors(textColors);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private void getColorsFromImages() {
        callsRemaining = mCoinList.size();
        mTarget = new ArrayList<>();

        for(final Coin coin : mCoinList) {
            String imageUrl = PicassoUtils.getFullCoinImageUrlSmall(coin.getImageUrl());
            Timber.d(imageUrl);

            Target newTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, final Picasso.LoadedFrom from) {
                    Palette.from(bitmap)
                            .generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {
                                    synchronized (this) {
                                        mPalettes.put(coin.getName(), palette);
                                        callsRemaining--;
                                        Timber.d("Calls remaining: %s", callsRemaining);
                                    }

                                    if(callsRemaining == 0) {
                                        setData();

                                        mChart.animateY(1400);
                                    }
                                }
                            });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Timber.d("Call failed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            mTarget.add(newTarget);

            Picasso.get()
                    .load(imageUrl)
                    .into(newTarget);
        }
    }

}
