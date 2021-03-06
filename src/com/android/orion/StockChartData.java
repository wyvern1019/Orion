package com.android.orion;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Paint;

import com.android.orion.database.Deal;
import com.android.orion.database.Stock;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class StockChartData {
	String mPeriod;
	String mDescription;

	ArrayList<String> mXValues = null;

	ArrayList<CandleEntry> mCandleEntryList = null;
	ArrayList<Entry> mAverage5EntryList = null;
	ArrayList<Entry> mAverage10EntryList = null;
	ArrayList<Entry> mDrawEntryList = null;
	ArrayList<Entry> mStrokeEntryList = null;
	ArrayList<Entry> mSegmentEntryList = null;
	ArrayList<Entry> mOverlapHighEntryList = null;
	ArrayList<Entry> mOverlapLowEntryList = null;

	ArrayList<Entry> mDIFEntryList = null;
	ArrayList<Entry> mDEAEntryList = null;
	ArrayList<BarEntry> mHistogramEntryList = null;
	// ArrayList<Entry> mSigmaHistogramEntryList = null;
	// ArrayList<Entry> mTrendsEffortsEntryList = null;
	ArrayList<Entry> mAverageEntryList = null;
	ArrayList<Entry> mVelocityEntryList = null;
	ArrayList<Entry> mAccelerateEntryList = null;

	ArrayList<LimitLine> mLimitLineList = null;

	CombinedData mCombinedDataMain = null;
	CombinedData mCombinedDataSub = null;

	public StockChartData() {
	}

	public StockChartData(String period) {
		if (mXValues == null) {
			mXValues = new ArrayList<String>();
		}

		if (mCandleEntryList == null) {
			mCandleEntryList = new ArrayList<CandleEntry>();
		}

		if (mDrawEntryList == null) {
			mDrawEntryList = new ArrayList<Entry>();
		}

		if (mStrokeEntryList == null) {
			mStrokeEntryList = new ArrayList<Entry>();
		}

		if (mSegmentEntryList == null) {
			mSegmentEntryList = new ArrayList<Entry>();
		}

		if (mOverlapHighEntryList == null) {
			mOverlapHighEntryList = new ArrayList<Entry>();
		}

		if (mOverlapLowEntryList == null) {
			mOverlapLowEntryList = new ArrayList<Entry>();
		}

		if (mAverage5EntryList == null) {
			mAverage5EntryList = new ArrayList<Entry>();
		}

		if (mAverage10EntryList == null) {
			mAverage10EntryList = new ArrayList<Entry>();
		}

		if (mDIFEntryList == null) {
			mDIFEntryList = new ArrayList<Entry>();
		}

		if (mDEAEntryList == null) {
			mDEAEntryList = new ArrayList<Entry>();
		}

		// if (mSigmaHistogramEntryList == null) {
		// mSigmaHistogramEntryList = new ArrayList<Entry>();
		// }
		//
		// if (mTrendsEffortsEntryList == null) {
		// mTrendsEffortsEntryList = new ArrayList<Entry>();
		// }

		if (mHistogramEntryList == null) {
			mHistogramEntryList = new ArrayList<BarEntry>();
		}

		if (mAverageEntryList == null) {
			mAverageEntryList = new ArrayList<Entry>();
		}

		if (mVelocityEntryList == null) {
			mVelocityEntryList = new ArrayList<Entry>();
		}

		if (mAccelerateEntryList == null) {
			mAccelerateEntryList = new ArrayList<Entry>();
		}

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValues);
		}

		if (mCombinedDataSub == null) {
			mCombinedDataSub = new CombinedData(mXValues);
		}

		if (mLimitLineList == null) {
			mLimitLineList = new ArrayList<LimitLine>();
		}

		mPeriod = period;
		mDescription = mPeriod;

		setMainChartData();
		setSubChartData();
	}

	void setMainChartData() {
		CandleData candleData = new CandleData();
		CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList, "K");
		candleDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
		candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
		candleDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
		candleDataSet.setShadowColorSameAsCandle(true);
		candleDataSet.setAxisDependency(AxisDependency.LEFT);
		candleDataSet.setColor(Color.RED);
		candleDataSet.setHighLightColor(Color.TRANSPARENT);
		candleDataSet.setDrawTags(true);
		candleData.addDataSet(candleDataSet);

		LineData lineData = new LineData();

		LineDataSet average5DataSet = new LineDataSet(mAverage5EntryList, "MA5");
		average5DataSet.setColor(Color.WHITE);
		// average5DataSet.setCircleColor(Color.YELLOW);
		average5DataSet.setDrawCircles(false);
		average5DataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(average5DataSet);

		LineDataSet average10DataSet = new LineDataSet(mAverage10EntryList,
				"MA10");
		average10DataSet.setColor(Color.CYAN);
		// average10DataSet.setCircleColor(Color.RED);
		average10DataSet.setDrawCircles(false);
		average10DataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(average10DataSet);

		LineDataSet drawDataSet = new LineDataSet(mDrawEntryList, "Draw");
		drawDataSet.setColor(Color.GRAY);
		drawDataSet.setCircleColor(Color.GRAY);
		drawDataSet.setCircleSize(3f);
		drawDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(drawDataSet);

		LineDataSet strokeDataSet = new LineDataSet(mStrokeEntryList, "Stroke");
		strokeDataSet.setColor(Color.YELLOW);
		strokeDataSet.setCircleColor(Color.YELLOW);
		strokeDataSet.setCircleSize(3f);
		strokeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(strokeDataSet);

		LineDataSet segmentDataSet = new LineDataSet(mSegmentEntryList,
				"Segment");
		segmentDataSet.setColor(Color.BLACK);
		segmentDataSet.setCircleColor(Color.BLACK);
		segmentDataSet.setCircleSize(3f);
		segmentDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(segmentDataSet);

		LineDataSet overlapHighDataSet = new LineDataSet(mOverlapHighEntryList,
				"overHigh");
		overlapHighDataSet.setColor(Color.MAGENTA);
		overlapHighDataSet.setDrawCircles(false);
		overlapHighDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(overlapHighDataSet);

		LineDataSet overlapLowDataSet = new LineDataSet(mOverlapLowEntryList,
				"overLow");
		overlapLowDataSet.setColor(Color.BLUE);
		overlapLowDataSet.setDrawCircles(false);
		overlapLowDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(overlapLowDataSet);

		mCombinedDataMain.setData(candleData);
		mCombinedDataMain.setData(lineData);
	}

	void setSubChartData() {
		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		histogramDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
		histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(histogramDataSet);

		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(Color.YELLOW);
		difDataSet.setCircleColor(Color.YELLOW);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		deaDataSet.setColor(Color.WHITE);
		deaDataSet.setCircleColor(Color.WHITE);
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

		// LineDataSet sigmaHistogramDataSet = new LineDataSet(
		// mSigmaHistogramEntryList, "Sigma");
		// sigmaHistogramDataSet.setColor(Color.CYAN);
		// sigmaHistogramDataSet.setCircleColor(Color.CYAN);
		// sigmaHistogramDataSet.setDrawCircles(false);
		// sigmaHistogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		// lineData.addDataSet(sigmaHistogramDataSet);

		// LineDataSet trendEffortsDataSet = new LineDataSet(
		// mTrendEffortsEntryList, "Trend");
		// trendEffortsDataSet.setColor(Color.BLUE);
		// trendEffortsDataSet.setCircleColor(Color.BLUE);
		// trendEffortsDataSet.setDrawCircles(false);
		// trendEffortsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		// lineData.addDataSet(trendEffortsDataSet);

		// LineDataSet averageDataSet = new LineDataSet(mAverageEntryList,
		// "Average");
		// averageDataSet.setColor(Color.GRAY);
		// averageDataSet.setDrawCircles(false);
		// averageDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		// lineData.addDataSet(averageDataSet);

		LineDataSet velocityDataSet = new LineDataSet(mVelocityEntryList,
				"velocity");
		velocityDataSet.setColor(Color.BLUE);
		velocityDataSet.setDrawCircles(false);
		velocityDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(velocityDataSet);

		LineDataSet acclerateDataSet = new LineDataSet(mAccelerateEntryList,
				"accelerate");
		acclerateDataSet.setColor(Color.MAGENTA);
		acclerateDataSet.setDrawCircles(false);
		acclerateDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(acclerateDataSet);

		mCombinedDataSub.setData(barData);
		mCombinedDataSub.setData(lineData);
	}

	void updateDescription(Stock stock) {
		mDescription = "";

		if (stock == null) {
			return;
		}
		mDescription += mPeriod;
		mDescription += " ";

		mDescription += stock.getPrice() + "  ";
		if (stock.getNet() > 0) {
			mDescription += "+";
		} else if (stock.getNet() < 0) {
			mDescription += "-";
		}
		mDescription += stock.getNet();
	}

	void updateLimitLine(ArrayList<Deal> dealList) {
		double dealPrice = 0;
		long dealVolume = 0;

		if ((dealList == null) || (mLimitLineList == null)) {
			return;
		}

		mLimitLineList.clear();

		for (Deal deal : dealList) {
			LimitLine limitLine = new LimitLine(0);
			limitLine.enableDashedLine(10f, 10f, 0f);
			limitLine.setLineWidth(3);
			limitLine.setTextSize(10f);
			limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);

			dealPrice = deal.getDeal();
			dealVolume = deal.getVolume();

			limitLine.setLimit((float) dealPrice);

			if (dealVolume > 0) {
				limitLine.setLineColor(Color.RED);
			} else {
				limitLine.setLineColor(Color.GREEN);
			}

			limitLine.setLabel(dealPrice + " " + dealVolume + " "
					+ deal.getNet() + " " + deal.getProfit());

			mLimitLineList.add(limitLine);
		}
	}

	void clear() {
		mXValues.clear();
		mDrawEntryList.clear();
		mStrokeEntryList.clear();
		mSegmentEntryList.clear();
		mCandleEntryList.clear();
		mOverlapHighEntryList.clear();
		mOverlapLowEntryList.clear();
		mAverageEntryList.clear();
		mAverage5EntryList.clear();
		mAverage10EntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
		// mSigmaHistogramEntryList.clear();
		// mTrendsEffortsEntryList.clear();
		mVelocityEntryList.clear();
		mAccelerateEntryList.clear();
	}
}
