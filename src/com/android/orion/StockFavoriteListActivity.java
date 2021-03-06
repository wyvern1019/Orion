package com.android.orion;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;
import com.avos.avoscloud.AVUser;

public class StockFavoriteListActivity extends StorageActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
	public static final String EXTRA_STOCK_ID = "stock_id";

	static final int LOADER_ID_STOCK_FAVORITE_LIST = 0;

	static final int LOAD_FAVORITE_LIST_FROM_SD = 11;
	static final int SAVE_FAVORITE_LIST_TO_SD = 12;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	static final String FAVORITE_LIST_XML_FILE_NAME = "favorite.xml";

	String mSortOrderColumn = DatabaseContract.COLUMN_CODE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	AVUser mCurrentUser = null;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mStockNameCode = null;
	TextView mPrice = null;
	TextView mPriceNet = null;
	TextView mPriceType5M = null;
	TextView mPriceType15M = null;
	TextView mPriceType30M = null;
	TextView mPriceType60M = null;
	TextView mPriceTypeDay = null;
	TextView mPriceTypeWeek = null;
	TextView mPriceTypeMonth = null;
	TextView mOverlap = null;
	TextView mVelocity = null;
	TextView mAcceleration = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mResumed) {
				if (intent.getIntExtra(Constants.EXTRA_KEY_SERVICE_TYPE,
						Constants.SERVICE_TYPE_NONE) == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_REALTIME
						|| intent.getIntExtra(Constants.EXTRA_KEY_SERVICE_TYPE,
								Constants.SERVICE_TYPE_NONE) == Constants.SERVICE_SIMULATE_STOCK_FAVORITE_DATA_HISTORY) {
					restartLoader();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		mLoaderManager.initLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);

		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_new:
			mIntent = new Intent(this, StockActivity.class);
			mIntent.setAction(StockActivity.ACTION_STOCK_INSERT);
			startActivity(mIntent);
			return true;

		case R.id.action_search:
			startActivity(new Intent(this, StockSearchActivity.class));
			return true;

		case R.id.action_download:
			onActionSync(Constants.SERVICE_CLOUD_DOWNLOAD_STOCK_FAVORITE);
			return true;

		case R.id.action_upload:
			onActionSync(Constants.SERVICE_CLOUD_UPLOAD_STOCK_FAVORITE);
			return true;

		case R.id.action_save_sd:
			showSaveSDAlertDialog();
			return true;

		case R.id.action_load_sd:
			startLoadTask(LOAD_FAVORITE_LIST_FROM_SD);
			return true;

		case R.id.action_clean_data:
			deleteStockData(0);
			startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE);
			return true;
		case R.id.action_simulation:
			Intent intent = new Intent(this, StockSimulationActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	void onSaveSD() {
		super.onSaveSD();
		startSaveTask(SAVE_FAVORITE_LIST_TO_SD);
	}

	void onActionSync(int serviceType) {
		Intent intent = null;
		mCurrentUser = AVUser.getCurrentUser();

		if (mCurrentUser == null) {
			intent = new Intent(this, LeanCloudLoginActivity.class);
			startActivityForResult(intent, serviceType);
		} else {
			startService(serviceType, Constants.EXECUTE_IMMEDIATE);
		}
	}

	void doInBackgroundLoad(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case LOAD_FAVORITE_LIST_FROM_SD:
			loadListFromSD(FAVORITE_LIST_XML_FILE_NAME);
			break;

		default:
			break;
		}
	}

	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
		startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
				Constants.EXECUTE_IMMEDIATE);
	}

	@Override
	void doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case SAVE_FAVORITE_LIST_TO_SD:
			SaveListToSD(FAVORITE_LIST_XML_FILE_NAME);
			break;

		default:
			break;
		}
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);
	}

	@Override
	void xmlParse(XmlPullParser parser) {
		super.xmlParse(parser);

		int eventType;
		String now = Utility.getCurrentDateTimeString();
		String tagName = "";
		Stock stock = Stock.obtain();

		if (mStockDatabaseManager == null) {
			return;
		}

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						stock.init();
					} else if (DatabaseContract.COLUMN_SE.equals(tagName)) {
						stock.setSE(parser.nextText());
					} else if (DatabaseContract.COLUMN_CODE.equals(tagName)) {
						stock.setCode(parser.nextText());
					} else if (DatabaseContract.COLUMN_NAME.equals(tagName)) {
						stock.setName(parser.nextText());
					} else {
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						mStockDatabaseManager.getStock(stock);
						stock.setMark(Constants.STOCK_FLAG_MARK_FAVORITE);
						if (!mStockDatabaseManager.isStockExist(stock)) {
							stock.setCreated(now);
							stock.setModified(now);
							mStockDatabaseManager.insertStock(stock);
						} else {
							mStockDatabaseManager.updateStock(stock,
									stock.getContentValues());
						}
					}
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void xmlSerialize(XmlSerializer xmlSerializer) {
		super.xmlSerialize(xmlSerializer);

		Cursor cursor = null;
		String selection = DatabaseContract.Stock.COLUMN_MARK + " = '"
				+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";
		Stock stock = Stock.obtain();

		if (mStockDatabaseManager == null) {
			return;
		}

		try {
			cursor = mStockDatabaseManager.queryStock(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stock.set(cursor);
					xmlSerializer.startTag(null, XML_TAG_ITEM);
					xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_SE,
							stock.getSE());
					xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CODE,
							stock.getCode());
					xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_NAME,
							stock.getName());
					xmlSerializer.endTag(null, XML_TAG_ITEM);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			startService(requestCode, Constants.EXECUTE_IMMEDIATE);
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(id, mHeaderTextHighlightColor);

		switch (id) {
		case R.id.stock_name_code:
			mSortOrderColumn = DatabaseContract.COLUMN_CODE;
			break;
		case R.id.price:
			mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
			break;
		case R.id.price_net:
			mSortOrderColumn = DatabaseContract.COLUMN_NET;
			break;
		case R.id.stock_action_5min:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_5MIN;
			break;
		case R.id.stock_action_15min:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_15MIN;
			break;
		case R.id.stock_action_30min:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_30MIN;
			break;
		case R.id.stock_action_60min:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_60MIN;
			break;
		case R.id.stock_action_day:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_DAY;
			break;
		case R.id.stock_action_week:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_WEEK;
			break;
		case R.id.stock_action_month:
			mSortOrderColumn = DatabaseContract.Stock.COLUMN_ACTION_MONTH;
			break;
		case R.id.overlap:
			mSortOrderColumn = DatabaseContract.COLUMN_OVERLAP;
			break;
		case R.id.velocity:
			mSortOrderColumn = DatabaseContract.COLUMN_VELOCITY;
			break;
		case R.id.acceleration:
			mSortOrderColumn = DatabaseContract.COLUMN_ACCELERATION;
			break;
		default:
			mSortOrderColumn = DatabaseContract.COLUMN_CODE;
			break;
		}

		if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		saveSetting(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);

		restartLoader();
	}

	void setHeaderTextColor(int id, int color) {
		TextView textView = (TextView) findViewById(id);
		setHeaderTextColor(textView, color);
	}

	void setHeaderTextColor(TextView textView, int color) {
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	void resetHeaderTextColor() {
		setHeaderTextColor(mStockNameCode, mHeaderTextDefaultColor);
		setHeaderTextColor(mPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceType5M, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceType15M, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceType30M, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceType60M, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceTypeDay, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceTypeWeek, mHeaderTextDefaultColor);
		setHeaderTextColor(mPriceTypeMonth, mHeaderTextDefaultColor);
		setHeaderTextColor(mOverlap, mHeaderTextDefaultColor);
		setHeaderTextColor(mVelocity, mHeaderTextDefaultColor);
		setHeaderTextColor(mAcceleration, mHeaderTextDefaultColor);
	}

	void setVisibility(String key, TextView textView) {
		if (textView != null) {
			if (Utility.getSettingBoolean(this, key)) {
				textView.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}
		}
	}

	void initHeader() {
		mTitleSHSV = (SyncHorizontalScrollView) findViewById(R.id.title_shsv);
		mContentSHSV = (SyncHorizontalScrollView) findViewById(R.id.content_shsv);

		if (mTitleSHSV != null && mContentSHSV != null) {
			mTitleSHSV.setScrollView(mContentSHSV);
			mContentSHSV.setScrollView(mTitleSHSV);
		}

		mStockNameCode = (TextView) findViewById(R.id.stock_name_code);
		if (mStockNameCode != null) {
			mStockNameCode.setOnClickListener(this);
		}

		mPrice = (TextView) findViewById(R.id.price);
		if (mPrice != null) {
			mPrice.setOnClickListener(this);
		}

		mPriceNet = (TextView) findViewById(R.id.price_net);
		if (mPriceNet != null) {
			mPriceNet.setOnClickListener(this);
		}

		mPriceType5M = (TextView) findViewById(R.id.stock_action_5min);
		if (mPriceType5M != null) {
			mPriceType5M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_5MIN, mPriceType5M);
		}

		mPriceType15M = (TextView) findViewById(R.id.stock_action_15min);
		if (mPriceType15M != null) {
			mPriceType15M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_15MIN, mPriceType15M);
		}

		mPriceType30M = (TextView) findViewById(R.id.stock_action_30min);
		if (mPriceType30M != null) {
			mPriceType30M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_30MIN, mPriceType30M);
		}

		mPriceType60M = (TextView) findViewById(R.id.stock_action_60min);
		if (mPriceType60M != null) {
			mPriceType60M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_60MIN, mPriceType60M);
		}

		mPriceTypeDay = (TextView) findViewById(R.id.stock_action_day);
		if (mPriceTypeDay != null) {
			mPriceTypeDay.setOnClickListener(this);
			setVisibility(Constants.PERIOD_DAY, mPriceTypeDay);
		}

		mPriceTypeWeek = (TextView) findViewById(R.id.stock_action_week);
		if (mPriceTypeWeek != null) {
			mPriceTypeWeek.setOnClickListener(this);
			setVisibility(Constants.PERIOD_WEEK, mPriceTypeWeek);
		}

		mPriceTypeMonth = (TextView) findViewById(R.id.stock_action_month);
		if (mPriceTypeMonth != null) {
			mPriceTypeMonth.setOnClickListener(this);
			setVisibility(Constants.PERIOD_MONTH, mPriceTypeMonth);
		}

		mOverlap = (TextView) findViewById(R.id.overlap);
		if (mOverlap != null) {
			mOverlap.setOnClickListener(this);
		}

		mVelocity = (TextView) findViewById(R.id.velocity);
		if (mVelocity != null) {
			mVelocity.setOnClickListener(this);
		}

		mAcceleration = (TextView) findViewById(R.id.acceleration);
		if (mAcceleration != null) {
			mAcceleration.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mStockNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mPriceNet, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_5MIN)) {
			setHeaderTextColor(mPriceType15M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_15MIN)) {
			setHeaderTextColor(mPriceType15M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_30MIN)) {
			setHeaderTextColor(mPriceType30M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_60MIN)) {
			setHeaderTextColor(mPriceType60M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_DAY)) {
			setHeaderTextColor(mPriceTypeDay, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_WEEK)) {
			setHeaderTextColor(mPriceTypeWeek, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.Stock.COLUMN_ACTION_MONTH)) {
			setHeaderTextColor(mPriceTypeMonth, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_OVERLAP)) {
			setHeaderTextColor(mOverlap, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VELOCITY)) {
			setHeaderTextColor(mVelocity, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ACCELERATION)) {
			setHeaderTextColor(mAcceleration, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.Stock.COLUMN_ACTION_5MIN,
				DatabaseContract.Stock.COLUMN_ACTION_15MIN,
				DatabaseContract.Stock.COLUMN_ACTION_30MIN,
				DatabaseContract.Stock.COLUMN_ACTION_60MIN,
				DatabaseContract.Stock.COLUMN_ACTION_DAY,
				DatabaseContract.Stock.COLUMN_ACTION_WEEK,
				DatabaseContract.Stock.COLUMN_ACTION_MONTH,
				DatabaseContract.COLUMN_OVERLAP,
				DatabaseContract.COLUMN_VELOCITY,
				DatabaseContract.COLUMN_ACCELERATION };
		int[] mRightTo = new int[] { R.id.price, R.id.net, R.id.type_5min,
				R.id.type_15min, R.id.type_30min, R.id.type_60min,
				R.id.type_day, R.id.type_week, R.id.type_month, R.id.overlap,
				R.id.velocity, R.id.acceleration };

		mLeftListView = (ListView) findViewById(R.id.left_listview);
		mLeftAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_list_left_item, null, mLeftFrom,
				mLeftTo, 0);
		if ((mLeftListView != null) && (mLeftAdapter != null)) {
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		mRightListView = (ListView) findViewById(R.id.right_listview);
		mRightAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new CustomViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		restartLoader();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String selection = "";
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_STOCK_FAVORITE_LIST:
			selection = DatabaseContract.Stock.COLUMN_MARK + " = '"
					+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";

			loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
					DatabaseContract.Stock.PROJECTION_ALL, selection, null,
					mSortOrder);
			break;

		default:
			break;
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader == null) {
			return;
		}

		switch (loader.getId()) {
		case LOADER_ID_STOCK_FAVORITE_LIST:
			mLeftAdapter.swapCursor(cursor);
			mRightAdapter.swapCursor(cursor);
			break;

		default:
			break;
		}

		Utility.setListViewHeightBasedOnChildren(mLeftListView);
		Utility.setListViewHeightBasedOnChildren(mRightListView);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mLeftAdapter.swapCursor(null);
		mRightAdapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (id <= Constants.STOCK_ID_INVALID) {
			return;
		}

		if (ACTION_STOCK_ID.equals(mAction)) {
			if (mIntent != null) {
				mIntent.putExtra(EXTRA_STOCK_ID, id);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		} else {
			Intent intent = new Intent(this, StockChartListActivity.class);
			intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);
			intent.putExtra(DatabaseContract.COLUMN_STOCK_ID, id);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(this, StockFavoriteEditActivity.class);
		intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);
		startActivity(intent);
		return true;
	}

	boolean setTextViewValue(String key, View textView) {
		if (textView != null) {
			if (Utility.getSettingBoolean(this, key)) {
				textView.setVisibility(View.VISIBLE);
				return false;
			} else {
				textView.setVisibility(View.GONE);
				return true;
			}
		}

		return false;
	}

	private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_5MIN)) {
				return setTextViewValue(Constants.PERIOD_5MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_15MIN)) {
				return setTextViewValue(Constants.PERIOD_15MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_30MIN)) {
				return setTextViewValue(Constants.PERIOD_30MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_60MIN)) {
				return setTextViewValue(Constants.PERIOD_60MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_DAY)) {
				return setTextViewValue(Constants.PERIOD_DAY, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_WEEK)) {
				return setTextViewValue(Constants.PERIOD_WEEK, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_MONTH)) {
				return setTextViewValue(Constants.PERIOD_MONTH, view);
			}

			return false;
		}
	}
}
