package com.example.listview;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyRefreshListView extends ListView implements android.widget.AdapterView.OnItemClickListener {
	private View mHeadView, mFooterView;
	private float endY = -1;
	private int headViewHeight, footViewHeight;
	private final int RELASE_REFRESH = 0;// 松开刷新
	private final int REFRESHING = 1;// 正在刷新
	private final int DOWN_REFRESH = 2;// 下拉刷新
	private final int UP_LOADING = 3;// 上拉加载
	private final int LOADING = 4;// 正在加载
	private final int RELEAS_LOADING = 5;// 松手加载加载
	private final int UP_DISTANCE = 10;// 上拉多少距离显示可松手
	private int headViewCurrentState = DOWN_REFRESH;
	private int footViewCurrentState = UP_LOADING;
	private TextView tv_title, tv_time, tv_foot;
	private ImageView iv_arr;
	private RotateAnimation downRotateAnimation, upRotateAnimation;
	private ProgressBar pb_head, pb_foot;
	private static int DURATIOM_Millis = 200;// 动画执行时间
	private OnRefreshListener onRefreshListener;
	private String lastRefreshTime = getContext().getString(R.string.last_refresh_time) + getCurrentTime();
	// private boolean isLoadMore = false;// 保证onLoadMore()方法只调用一次,同时标记是否正在加载
	private final static float OFFSET_RADIO = 1.8f;

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	public MyRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MyRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyRefreshListView(Context context) {
		super(context);
		init();
	}

	private void init() {
		initAnimation();
		initHeadView();
		initFootView();
		setOnItemClickListener(this);
		// setOnScrollListener(new OnScrollListener() {
		//
		// @Override
		// public void onScrollStateChanged(AbsListView view, int scrollState) {
		// if ((getCount() > 0) && (scrollState == SCROLL_STATE_IDLE ||
		// scrollState == SCROLL_STATE_FLING) && !isLoadMore
		// && getFootBottomMagin() > 0) {
		// // if (getCount() > 0 && getLastVisiblePosition() ==
		// // getCount() - 1) {
		// // 滑动到了底部，显示footView
		// // mFooterView.setPadding(0, 0, 0, 0);
		// // if (getCount() > 0) {
		// // setSelection(getCount() - 1);
		// // }
		// isLoadMore = true;
		// if (onRefreshListener != null && foodViewCurrentState == UP_LOADING)
		// {
		// onRefreshListener.onLoadMore();
		// }
		// // }
		// }
		// }
		//
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem, int
		// visibleItemCount, int totalItemCount) {
		//
		// }
		// });
	}

	private void initFootView() {
		mFooterView = View.inflate(getContext(), R.layout.refresh_listview_footer, null);
		this.addFooterView(mFooterView);
		tv_foot = (TextView) mFooterView.findViewById(R.id.tv_foot);
		pb_foot = (ProgressBar) mFooterView.findViewById(R.id.pb_foot);
		pb_foot.setVisibility(View.GONE);
		mFooterView.measure(0, 0);
		footViewHeight = mFooterView.getMeasuredHeight();
		// mFooterView.setPadding(0, -footViewHeight, 0, 0);// 隐藏
		mFooterView.setPadding(0, 0, 0, 0);// 默认显示
	}

	//
	// public int getHeadTopMagin() {
	// // mContentView有父布局才行，用mFootView就会报空指针，因为没有父布局包裹
	// LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
	// headContentView.getLayoutParams();
	// return lp.topMargin;
	// }

	private void initHeadView() {
		mHeadView = View.inflate(getContext(), R.layout.headview_layout, null);
		tv_time = (TextView) mHeadView.findViewById(R.id.tv_time);
		tv_time.setText(lastRefreshTime);
		tv_title = (TextView) mHeadView.findViewById(R.id.tv_title);
		iv_arr = (ImageView) mHeadView.findViewById(R.id.iv_arr);
		pb_head = (ProgressBar) mHeadView.findViewById(R.id.pb_head);
		// 测量一下控件的大小
		mHeadView.measure(0, 0);
		headViewHeight = mHeadView.getMeasuredHeight();
		mHeadView.setPadding(0, -headViewHeight, 0, 0);
		this.addHeaderView(mHeadView);
	}

	/**
	 * 初始化动画
	 */
	public void initAnimation() {
		downRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		downRotateAnimation.setDuration(DURATIOM_Millis);
		downRotateAnimation.setFillAfter(true);
		upRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		upRotateAnimation.setDuration(DURATIOM_Millis);
		upRotateAnimation.setFillAfter(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (endY == -1) {
			endY = ev.getRawY();
			;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			endY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			float dy = ev.getRawY() - endY;
			if (headViewCurrentState == REFRESHING) {
				break;
			}
			// System.out.println("dy" + dy + "getFirstVisiblePosition()" +
			// getFirstVisiblePosition());
			// flingDownDis += dy;
			headViewOffset = (int) (dy - headViewHeight);
			if (dy > 0 && getFirstVisiblePosition() == 0) {
				// dy > 0下拉
				mHeadView.setPadding(0, headViewOffset, 0, 0);// 设置当前的padding 值
				if (headViewOffset > 0 && headViewCurrentState != RELASE_REFRESH && mHeadView.getPaddingTop() >= 0) {
					// offset > 0表示headView已经完全出来
					// 松开刷新
					headViewCurrentState = RELASE_REFRESH;
					refreshByState(headViewCurrentState);
					// 注意return
					// true位置，要是放在下面break的位置，那么Move的事件都会被拦截，listview向下滑动困难
					return true;
				}
			}
			if (headViewCurrentState == RELASE_REFRESH && getFirstVisiblePosition() == 0 && headViewOffset < 0) {
				// 上拉(返回初始状态)
				headViewCurrentState = DOWN_REFRESH;
				refreshByState(headViewCurrentState);
				return true;
			}
			footViewOffset = (int) (Math.abs(dy) - footViewHeight - UP_DISTANCE);
			if (dy < 0 && getCount() > 0 && (getLastVisiblePosition() == (getCount() - 1)) && footViewOffset < 0) {
				// 上拉加载
				mFooterView.setPadding(0, 0, 0, (int) Math.abs(dy));
				if (mFooterView.getPaddingBottom() > UP_DISTANCE) {
					footViewCurrentState = RELEAS_LOADING;
					tv_foot.setText("放手吧");
				} else {
					footViewCurrentState = UP_LOADING;
					tv_foot.setText("上拉刷新");
				}

			}
			// if (footViewCurrentState==) {
			//
			// }
			break;
		case MotionEvent.ACTION_UP:
			endY = -1;
			// int temp = (int) ev.getRawY() - headViewHeight;
			if (headViewOffset > 0 && headViewCurrentState == RELASE_REFRESH) {
				// offset > 0表示headView已经完全出来
				// 正在刷新
				headViewCurrentState = REFRESHING;
				refreshByState(headViewCurrentState);
				return true;
			}
			// 下拉刷新，拉下来后没有松手，又上拉一部分，将headView部分隐藏部分显示。此时松手，将headView重置
			if (mHeadView.getPaddingTop() != -headViewHeight && headViewOffset < 0 && headViewCurrentState == DOWN_REFRESH) {
				resetHeadView();
				return true;
			}

			if (footViewCurrentState == RELEAS_LOADING && footViewOffset > 0) {
				footViewCurrentState = LOADING;
				refreshByState(footViewCurrentState);
				return true;
			}
			// 上拉加载，上拉后后没有松手，又下拉一部分，将footView部分隐藏部分显示。此时松手，将footView重置
			if (footViewCurrentState == UP_LOADING && mFooterView.getPaddingBottom() < UP_DISTANCE && footViewOffset < 0) {
				resetFootView();
				return true;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 将headView初始化到初始状态
	 */
	public void resetHeadView() {
		headViewCurrentState = DOWN_REFRESH;
		mHeadView.setPadding(0, -headViewHeight, 0, 0);
		tv_title.setText("下拉刷新");
		iv_arr.clearAnimation();
		setSelection(0);
	}

	/**
	 * 将footView初始化到初始状态
	 */
	public void resetFootView() {
		headViewCurrentState = UP_LOADING;
		mHeadView.setPadding(0, 0, 0, 0);
		tv_title.setText("上拉刷新");
		if (getCount() > 0) {
			setSelection(getCount() - 1);
		}
	}

	public void refreshByState(int state) {
		switch (state) {
		case DOWN_REFRESH:
			tv_title.setText("下拉刷新");
			iv_arr.startAnimation(downRotateAnimation);
			break;
		case REFRESHING:
			tv_title.setText("正在刷新");
			iv_arr.clearAnimation();// 清除动画后才能隐藏
			iv_arr.setVisibility(View.INVISIBLE);
			pb_head.setVisibility(View.VISIBLE);
			mHeadView.setPadding(0, 0, 0, 0);
			if (onRefreshListener != null) {
				onRefreshListener.onRefresh();
			}
			break;
		case RELASE_REFRESH:
			tv_title.setText("放手吧");
			iv_arr.startAnimation(upRotateAnimation);
			break;
		case LOADING:
			pb_foot.setVisibility(View.VISIBLE);
			tv_foot.setText("正在加载");
			if (onRefreshListener != null) {
				onRefreshListener.onLoadMore();
			}
			break;
		default:
			break;
		}
	};

	public void onComplete() {
		if (footViewCurrentState == LOADING) {
			mFooterView.setPadding(0, 0, 0, 0);
			footViewCurrentState = UP_LOADING;
			pb_foot.setVisibility(View.INVISIBLE);
			tv_foot.setText("上拉刷新");
		}
		if (headViewCurrentState == REFRESHING) {
			tv_title.setText("下拉刷新");
			iv_arr.startAnimation(downRotateAnimation);
			iv_arr.clearAnimation();
			pb_head.clearAnimation();
			iv_arr.setVisibility(View.VISIBLE);
			pb_head.setVisibility(View.INVISIBLE);
			mHeadView.setPadding(0, -headViewHeight, 0, 0);
			tv_time.setText(lastRefreshTime);
			headViewCurrentState = DOWN_REFRESH;
		}
	}

	/**
	 * 获取当前时间
	 */
	public String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	OnItemClickListener mItemClickListener;
	/**
	 * 下拉过程中headView下拉的距离-headViewHeight
	 */
	private int headViewOffset;
	/**
	 * 上拉过程中headView上拉了的距离-footViewHeight-UP-DISTANCE
	 */
	private int footViewOffset;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mItemClickListener != null) {
			mItemClickListener.onItemClick(parent, view, position - getHeaderViewsCount(), id);
		}
	}

	@Override
	public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
		super.setOnItemClickListener(this);// 为了使调用setOnItemClickListener方法时调用本类的onItemClick
		mItemClickListener = listener;// 调用本类的onItemClick时调用mItemClickListener的onItemClick
	}
}