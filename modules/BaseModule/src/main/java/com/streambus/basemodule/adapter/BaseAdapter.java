package com.streambus.basemodule.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.streambus.basemodule.utils.SLog;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/4/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> implements BaseLoadingHolder.IGetDataPresenter, BaseLoadMoreHolder.IGetMoreDataPresenter {

    private static final String TAG = "BaseAdapter";

    public static final int LOADING_VIEW  = 0x00000111;
    public static final int LOADMORE_VIEW = 0x00000222;

    protected List<T> mData;

    protected BaseLoadingHolder mLoadingHolder;
    protected BaseLoadMoreHolder mLoadMoreHolder;
    protected boolean mIsLoadingEnable;
    protected boolean mIsLoadMoreEnable;
    private IRequestDataPresenter mRequestDataPresenter;
    private OnRecyclerViewItemClickListener mClickListener;
    private OnRecyclerViewItemLongClickListener mLongClickListener;
    private OnRecyclerViewItemFocusListener mFocusListener;
    private boolean mIsNoData;
    private boolean mIsEmptyData;
    private boolean mIsNoMoreData;

    public BaseAdapter() {
        this(false, false);
    }
    public BaseAdapter(boolean isLoadingEnable, boolean isLoadMoreEnable) {
        mIsLoadingEnable = isLoadingEnable;
        mIsLoadMoreEnable = isLoadMoreEnable;
        mData = new ArrayList<>();
        setHasStableIds(true);
    }

    public List<T> data() {
        return mData;
    }

    /**
     * 添加更多数据
     * @param moreData
     */
    public void addMoreData(List<T> moreData) {
        int position = mData.size();
        mData.addAll(moreData);
        notifyItemRangeInserted(position, moreData.size());
    }

    /**
     * 更新数据，
     * @param data
     */
    public void upData(List<T> data) {
        SLog.d(TAG, "upData data.size: "+data.size());
        mData = new ArrayList<>(data);
        mIsNoMoreData = false;
        notifyDataSetChanged();
    }

    /**
     * 首部插入一条数据
     * @param item 数据
     */
    public void addFirst(T item) {
        mData.add(0, item);
        notifyItemInserted(0);
    }

    /**
     * 尾部插入一条数据
     *
     * @param item 数据
     */
    public void addLast(T item) {
        mData.add(item);
        notifyItemInserted(mData.size() - 1);
    }

    /**
     * 首部插入一组数据
     * @param data
     */
    public void addDataAtFirst(List<T> data){
        mData.addAll(0,data);
        notifyItemRangeInserted(0,data.size());
    }

    /**
     * 移除一条数据
     *
     * @param item 数据
     */
    public void removeItem(T item) {
        int i = mData.indexOf(item);
        if (i != -1) {
            mData.remove(item);
            notifyItemRemoved(i);
            if (i < mData.size()) {
                notifyItemRangeChanged(i, mData.size() - i);
            }
        }
    }

    public void removeItem(int position){
        int itemViewType = getItemViewType(position);
        if (itemViewType == LOADING_VIEW || itemViewType == LOADMORE_VIEW) {
            return;
        }
        ArrayList<T> list = new ArrayList<>(mData);
        list.remove(position);
        mData = list;
        notifyItemRemoved(position);
    }


    /**
     * 清除数据，回到初始状态
     */
    public void cleanData() {
        mIsEmptyData = false;
        mIsNoData = false;
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    public T getItemData(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        int count = mData.size();
        if (mIsLoadingEnable && count == 0){
            count++;
        } else if (mIsLoadMoreEnable && count != 0){
            count++;
        }
        return count;
    }


    @Override
    public int getItemViewType(int position) {
        if (mIsLoadingEnable && mData.size() == 0  && position == 0 ) {
            return LOADING_VIEW; //true && mData.size == 0 && position == 0
        }   if (mIsLoadMoreEnable && mData.size() != 0 && (position == (getItemCount() - 1))) {
            return LOADMORE_VIEW;//true && mData.size != 0 && position = lastPosition
        }
        return super.getItemViewType(position);
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADING_VIEW) {
            //if (mLoadingHolder == null) {
                //Called attach on a child which is not detached:
                mLoadingHolder=  createLoadingHolder(parent, this);
            //}
            return mLoadingHolder.createBaseViewHolder();
        }
        if (viewType == LOADMORE_VIEW) {
            //if (mLoadMoreHolder == null) {
                //Called attach on a child which is not detached:
                //ViewHolder views must not be attached when created. Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate
                //不可复用，重新创建
                mLoadMoreHolder =  createLoadMoreHolder(parent, this);
            //}
            return mLoadMoreHolder.createBaseViewHolder();
        }
        SLog.d(TAG, "create baseViewHolder");
        BaseViewHolder<T> baseViewHolder = createBaseViewHolder(parent, viewType);
        _initItemListener(baseViewHolder);
        return baseViewHolder;
    }

    protected BaseViewHolder<T> createBaseViewHolder(ViewGroup parent, int viewType){
        SLog.d(TAG, "create createDefBaseViewHolder");
        return createDefBaseViewHolder(parent);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (getItemViewType(position) == LOADING_VIEW) {
            if (mIsEmptyData) {
                mLoadingHolder.showEmptyData();
            }else if (mIsNoData) {
                mLoadingHolder.showNoData();
            } else {
                mLoadingHolder.showLoading();
            }
            return;
        }
        if (getItemViewType(position) == LOADMORE_VIEW) {
            if (mIsNoMoreData) {
                mLoadMoreHolder.showNoMoreData();
            } else {
                mLoadMoreHolder.showLoading();
                getMoreData();
            }
            return;
        }
//        Logger.d(Logger._JN, "mData.size():  " + mData.size());
        holder.setData(holder.itemData = getItemData(position));

    }


    protected abstract BaseViewHolder<T> createDefBaseViewHolder(ViewGroup parent);

    protected BaseLoadingHolder createLoadingHolder(ViewGroup parent, BaseLoadingHolder.IGetDataPresenter presenter) {
        return null;
    }

    protected BaseLoadMoreHolder createLoadMoreHolder(ViewGroup parent, BaseLoadMoreHolder.IGetMoreDataPresenter presenter) {
        return null;
    }

    /************************************* - Loading和LoadMore - ****************************************/

    public void setRequestDataPresenter(IRequestDataPresenter presenter) {
        mRequestDataPresenter = presenter;
    }
    public void setRequestDataPresenter(Runnable load) {
        mRequestDataPresenter = new IRequestDataPresenter() {
            @Override public void getData() { load.run(); }
            @Override public void getMoreData() {}
        };
    }
    public void setRequestDataPresenter(Runnable load, Runnable more) {
        mRequestDataPresenter = new IRequestDataPresenter() {
            @Override public void getData() { load.run(); }
            @Override public void getMoreData() { more.run(); }
        };
    }
    public interface IRequestDataPresenter extends BaseLoadingHolder.IGetDataPresenter, BaseLoadMoreHolder.IGetMoreDataPresenter{
    }

    @Override
    public void getData() {
        if (mRequestDataPresenter != null) {
            mRequestDataPresenter.getData();
        }
    }

    @Override
    public void getMoreData() {
        if (mRequestDataPresenter != null) {
            mRequestDataPresenter.getMoreData();
        }
    }

    public void emptyData() {
        SLog.d(TAG, "emptyData");
        mIsEmptyData = true;
        if (mLoadingHolder != null) {
            mLoadingHolder.showEmptyData();
        }
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void noData() {
        SLog.d(TAG, "noData ");
        mIsNoData = true;
        if (mLoadingHolder != null) {
            mLoadingHolder.showNoData();
        }
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void loadDataError(String msg) {
        if (mLoadingHolder != null) {
            mLoadingHolder.showError(msg);
        }
    }

    public void noMoreData() {
        mIsNoMoreData = true;
        if (mLoadMoreHolder != null) {
            mLoadMoreHolder.showNoMoreData();
        }
    }

    public void loadMoreError(String msg) {
        if (mLoadMoreHolder != null) {
            mLoadMoreHolder.showLoadMoreError(msg);
        }
    }


    private void _initItemListener(final BaseViewHolder baseViewHolder) {
        if (baseViewHolder.applyClick || mClickListener != null) {
            baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (baseViewHolder.applyClick) {
                        baseViewHolder.onClick(baseViewHolder.getLayoutPosition());
                    }
                    if (mClickListener != null) {
                        mClickListener.onItemClick(baseViewHolder, v, baseViewHolder.getLayoutPosition());
                    }
                }
            });
        }

        if (baseViewHolder.applyFocus || mFocusListener != null) {
            baseViewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (baseViewHolder.applyFocus) {
                        baseViewHolder.onFocusChange(hasFocus, baseViewHolder.getLayoutPosition());
                    }
                    if (mFocusListener != null) {
                        mFocusListener.onFocus(baseViewHolder, v, baseViewHolder.getLayoutPosition(), hasFocus);
                    }
                }
            });
        }

        if (mLongClickListener != null){
            baseViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mLongClickListener.onLongClick(baseViewHolder, v, baseViewHolder.getLayoutPosition());
                    return true;
                }
            });
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    public void setOnOnItemLongClickListener(OnRecyclerViewItemLongClickListener clickListener) {
        mLongClickListener = clickListener;
    }

    public void setOnItemFocusListener(OnRecyclerViewItemFocusListener focusListener) {
        mFocusListener = focusListener;
    }


   public interface OnRecyclerViewItemClickListener<T>{
        void onItemClick(BaseViewHolder<T> baseViewHolder,View v, int position);


   }

    public interface OnRecyclerViewItemLongClickListener {
        void onLongClick(BaseViewHolder baseViewHolder, View v, int position);
    }

    public interface OnRecyclerViewItemFocusListener{
        void onFocus(BaseViewHolder baseViewHolder, View v, int position, boolean hasFocus);
    }

}
