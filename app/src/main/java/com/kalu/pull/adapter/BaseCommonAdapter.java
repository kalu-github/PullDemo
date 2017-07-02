package com.kalu.pull.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kalu.pull.adapter.animation.AlphaInAnimation;
import com.kalu.pull.adapter.animation.BaseAnimation;
import com.kalu.pull.adapter.animation.ScaleInAnimation;
import com.kalu.pull.adapter.animation.SlideInBottomAnimation;
import com.kalu.pull.adapter.animation.SlideInLeftAnimation;
import com.kalu.pull.adapter.animation.SlideInRightAnimation;
import com.kalu.pull.adapter.holder.BaseViewHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * description: 没有加载更多
 * created by kalu on 2017/5/26 14:22
 */
public abstract class BaseCommonAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    @IntDef({BaseAnimation.ALPHAIN, BaseAnimation.SCALEIN, BaseAnimation.SLIDEIN_BOTTOM, BaseAnimation.SLIDEIN_LEFT, BaseAnimation.SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    protected int mLastPosition = -1;

    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();
    //header footer
    protected LinearLayout mHeaderLayout;
    protected LinearLayout mFooterLayout;
    //empty
    protected FrameLayout mEmptyLayout;
    private boolean mIsUseEmpty = true;
    protected boolean mHeadAndEmptyEnable;
    protected boolean mFootAndEmptyEnable;

    protected static final String TAG = BaseCommonAdapter.class.getSimpleName();
    protected Context mContext;
    protected int mLayoutResId;
    protected LayoutInflater mLayoutInflater;
    protected List<T> mData;

    private RecyclerView mRecyclerView;

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    protected void checkNotNull() {
        if (getRecyclerView() == null) {
            throw new RuntimeException("please bind recyclerView first!");
        }
    }

    public void bindToRecyclerView(RecyclerView recyclerView) {
        if (getRecyclerView() != null) {
            throw new RuntimeException("Don't bind twice");
        }
        setRecyclerView(recyclerView);
        getRecyclerView().setAdapter(this);
    }

    protected int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    public void setNotDoAnimationCount(int count) {
        mLastPosition = count;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }


    public BaseCommonAdapter(@LayoutRes int layoutResId, @Nullable List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseCommonAdapter() {
        this(0, null);
    }

    public BaseCommonAdapter(@Nullable List<T> data) {
        this(0, data);
    }

    public BaseCommonAdapter(@LayoutRes int layoutResId) {
        this(layoutResId, null);
    }

    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    public void setNewData(@Nullable List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        mLastPosition = -1;
        notifyDataSetChanged();
    }


    /**
     * insert  a item associated with the specified position of adapter
     *
     * @param position
     * @param item
     * @deprecated use {@link #addData(int, Object)} instead
     */
    @Deprecated
    public void add(@IntRange(from = 0) int position, @NonNull T item) {
        addData(position, item);
    }

    /**
     * add one new data in to certain location
     *
     * @param position
     */
    public void addData(@IntRange(from = 0) int position, @NonNull T data) {
        mData.add(position, data);
        notifyItemInserted(position + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * add one new data
     */
    public void addData(@NonNull T data) {
        mData.add(data);
        notifyItemInserted(mData.size() + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * remove the item associated with the specified position of adapter
     *
     * @param position
     */
    public void remove(@IntRange(from = 0) int position) {
        mData.remove(position);
        int internalPosition = position + getHeaderLayoutCount();
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, mData.size() - internalPosition);
    }

    /**
     * change data
     */
    public void setData(@IntRange(from = 0) int index, @NonNull T data) {
        mData.set(index, data);
        notifyItemChanged(index + getHeaderLayoutCount());
    }

    /**
     * add new data in to certain location
     *
     * @param position the insert position
     * @param newData  the new data collection
     */
    public void addData(@IntRange(from = 0) int position, @NonNull Collection<? extends T> newData) {
        mData.addAll(position, newData);
        notifyItemRangeInserted(position + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * add new data to the end of mData
     *
     * @param newData the new data collection
     */
    public void addData(@NonNull Collection<? extends T> newData) {
        mData.addAll(newData);
        notifyItemRangeInserted(mData.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * compatible getLoadMoreViewCount and getEmptyViewCount may change
     *
     * @param size Need compatible data size
     */
    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mData == null ? 0 : mData.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    /**
     * Get the data of list
     *
     * @return
     */
    @NonNull
    public List<T> getData() {
        return mData;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Nullable
    public T getItem(@IntRange(from = 0) int position) {
        if (position < mData.size())
            return mData.get(position);
        else
            return null;
    }

    /**
     * if setHeadView will be return 1 if not will be return 0.
     * notice: Deprecated! Use {@link ViewGroup#getChildCount()} of {@link #getHeaderLayout()} to replace.
     *
     * @return
     */
    @Deprecated
    public int getHeaderViewsCount() {
        return getHeaderLayoutCount();
    }

    /**
     * if mFooterLayout will be return 1 or not will be return 0.
     * notice: Deprecated! Use {@link ViewGroup#getChildCount()} of {@link #getFooterLayout()} to replace.
     *
     * @return
     */
    @Deprecated
    public int getFooterViewsCount() {
        return getFooterLayoutCount();
    }

    /**
     * 头总数
     */
    public int getHeaderLayoutCount() {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * if addFooterView will be return 1, if not will be return 0
     */
    public int getFooterLayoutCount() {
        if (mFooterLayout == null || mFooterLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * if show empty view will be return 1 or not will be return 0
     *
     * @return
     */
    public int getEmptyViewCount() {
        if (mEmptyLayout == null || mEmptyLayout.getChildCount() == 0) {
            return 0;
        }
        if (!mIsUseEmpty) {
            return 0;
        }
        if (mData.size() != 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == 1) {
            count = 1;
            if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                count++;
            }
            if (mFootAndEmptyEnable && getFooterLayoutCount() != 0) {
                count++;
            }
        } else {
            count = getHeaderLayoutCount() + mData.size() + getFooterLayoutCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            boolean header = mHeadAndEmptyEnable && getHeaderLayoutCount() != 0;
            switch (position) {
                case 0:
                    if (header) {
                        return BaseViewHolder.HEADER_VIEW;
                    } else {
                        return BaseViewHolder.EMPTY_VIEW;
                    }
                case 1:
                    if (header) {
                        return BaseViewHolder.EMPTY_VIEW;
                    } else {
                        return BaseViewHolder.FOOTER_VIEW;
                    }
                case 2:
                    return BaseViewHolder.FOOTER_VIEW;
                default:
                    return BaseViewHolder.EMPTY_VIEW;
            }
        }
        int numHeaders = getHeaderLayoutCount();
        if (position < numHeaders) {
            return BaseViewHolder.HEADER_VIEW;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = mData.size();
            if (adjPosition < adapterCount) {
                return getDefItemViewType(adjPosition);
            } else {
                adjPosition = adjPosition - adapterCount;
                int numFooters = getFooterLayoutCount();
                if (adjPosition < numFooters) {
                    return BaseViewHolder.FOOTER_VIEW;
                } else {
                    return BaseViewHolder.LOADING_VIEW;
                }
            }
        }
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder;
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case BaseViewHolder.HEADER_VIEW:
                baseViewHolder = createBaseViewHolder(mHeaderLayout);
                break;
            case BaseViewHolder.EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(mEmptyLayout);
                break;
            case BaseViewHolder.FOOTER_VIEW:
                baseViewHolder = createBaseViewHolder(mFooterLayout);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
        }
        baseViewHolder.setAdapter(this);
        return baseViewHolder;
    }

    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == BaseViewHolder.EMPTY_VIEW || type == BaseViewHolder.HEADER_VIEW || type == BaseViewHolder.FOOTER_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == BaseViewHolder.HEADER_VIEW && isHeaderViewAsFlow()) {
                        return 1;
                    }
                    if (type == BaseViewHolder.FOOTER_VIEW && isFooterViewAsFlow()) {
                        return 1;
                    }
                    if (mSpanSizeLookup == null) {
                        return isFixedViewType(type) ? gridManager.getSpanCount() : 1;
                    } else {
                        return (isFixedViewType(type)) ? gridManager.getSpanCount() : mSpanSizeLookup.getSpanSize(gridManager,
                                position - getHeaderLayoutCount());
                    }
                }


            });
        }
    }

    protected boolean isFixedViewType(int type) {
        return type == BaseViewHolder.EMPTY_VIEW || type == BaseViewHolder.HEADER_VIEW || type == BaseViewHolder.FOOTER_VIEW;
    }

    /**
     * if asFlow is true, footer/header will arrange like normal item view.
     * only works when use {@link GridLayoutManager},and it will ignore span size.
     */
    private boolean headerViewAsFlow, footerViewAsFlow;

    public void setHeaderViewAsFlow(boolean headerViewAsFlow) {
        this.headerViewAsFlow = headerViewAsFlow;
    }

    public boolean isHeaderViewAsFlow() {
        return headerViewAsFlow;
    }

    public void setFooterViewAsFlow(boolean footerViewAsFlow) {
        this.footerViewAsFlow = footerViewAsFlow;
    }

    public boolean isFooterViewAsFlow() {
        return footerViewAsFlow;
    }

    private SpanSizeLookup mSpanSizeLookup;

    public interface SpanSizeLookup {
        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    /**
     * @param spanSizeLookup instance to be used to query number of spans occupied by each item
     */
    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    /**
     * To bind different types of holder and solve different the bind events
     *
     * @param holder
     * @see #getDefItemViewType(int)
     */
    @Override
    public void onBindViewHolder(K holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                convert(holder, mData.get(holder.getLayoutPosition() - getHeaderLayoutCount()), position);
                break;
            case BaseViewHolder.HEADER_VIEW:
                break;
            case BaseViewHolder.EMPTY_VIEW:
                break;
            case BaseViewHolder.FOOTER_VIEW:
                break;
            default:
                convert(holder, mData.get(holder.getLayoutPosition() - getHeaderLayoutCount()), position);
                break;
        }
    }

    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mLayoutResId;
        return createBaseViewHolder(parent, layoutId);
    }

    protected K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }

    protected K createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K k = createGenericKInstance(z, view);
        return null != k ? k : (K) new BaseViewHolder(view);
    }

    private K createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            String buffer = Modifier.toString(z.getModifiers());
            String className = z.getName();
            // inner and unstatic class
            if (className.contains("$") && !buffer.contains("static")) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get generic parameter K
     *
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return root layout of header
     */

    public LinearLayout getHeaderLayout() {
        return mHeaderLayout;
    }

    /**
     * Return root layout of footer
     */
    public LinearLayout getFooterLayout() {
        return mFooterLayout;
    }

    /**
     * Append header to the rear of the mHeaderLayout.
     *
     * @param header
     */
    public int addHeaderView(View header) {
        return addHeaderView(header, -1);
    }

    /**
     * Add header view to mHeaderLayout and set header view position in mHeaderLayout.
     * When index = -1 or index >= child count in mHeaderLayout,
     * the effect of this method is the same as that of {@link #addHeaderView(View)}.
     *
     * @param header
     * @param index  the position in mHeaderLayout of this header.
     *               When index = -1 or index >= child count in mHeaderLayout,
     *               the effect of this method is the same as that of {@link #addHeaderView(View)}.
     */
    public int addHeaderView(View header, int index) {
        return addHeaderView(header, index, LinearLayout.VERTICAL);
    }

    /**
     * @param header
     * @param index
     * @param orientation
     */
    public int addHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null) {
            mHeaderLayout = new LinearLayout(header.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
                mHeaderLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeaderLayout.setLayoutParams(new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mHeaderLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mHeaderLayout.addView(header, index);
        if (mHeaderLayout.getChildCount() == 1) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setHeaderView(View header) {
        return setHeaderView(header, 0, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index) {
        return setHeaderView(header, index, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() <= index) {
            return addHeaderView(header, index, orientation);
        } else {
            mHeaderLayout.removeViewAt(index);
            mHeaderLayout.addView(header, index);
            return index;
        }
    }

    /**
     * Append footer to the rear of the mFooterLayout.
     *
     * @param footer
     */
    public int addFooterView(View footer) {
        return addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index) {
        return addFooterView(footer, index, LinearLayout.VERTICAL);
    }

    /**
     * Add footer view to mFooterLayout and set footer view position in mFooterLayout.
     * When index = -1 or index >= child count in mFooterLayout,
     * the effect of this method is the same as that of {@link #addFooterView(View)}.
     *
     * @param footer
     * @param index  the position in mFooterLayout of this footer.
     *               When index = -1 or index >= child count in mFooterLayout,
     *               the effect of this method is the same as that of {@link #addFooterView(View)}.
     */
    public int addFooterView(View footer, int index, int orientation) {
        if (mFooterLayout == null) {
            mFooterLayout = new LinearLayout(footer.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout.setOrientation(LinearLayout.VERTICAL);
                mFooterLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFooterLayout.setOrientation(LinearLayout.HORIZONTAL);
                mFooterLayout.setLayoutParams(new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mFooterLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mFooterLayout.addView(footer, index);
        if (mFooterLayout.getChildCount() == 1) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setFooterView(View header) {
        return setFooterView(header, 0, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index) {
        return setFooterView(header, index, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index, int orientation) {
        if (mFooterLayout == null || mFooterLayout.getChildCount() <= index) {
            return addFooterView(header, index, orientation);
        } else {
            mFooterLayout.removeViewAt(index);
            mFooterLayout.addView(header, index);
            return index;
        }
    }

    /**
     * remove header view from mHeaderLayout.
     * When the child count of mHeaderLayout is 0, mHeaderLayout will be set to null.
     *
     * @param header
     */
    public void removeHeaderView(View header) {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeView(header);
        if (mHeaderLayout.getChildCount() == 0) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * remove footer view from mFooterLayout,
     * When the child count of mFooterLayout is 0, mFooterLayout will be set to null.
     *
     * @param footer
     */
    public void removeFooterView(View footer) {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeView(footer);
        if (mFooterLayout.getChildCount() == 0) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * remove all header view from mHeaderLayout and set null to mHeaderLayout
     */
    public void removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeAllViews();
        int position = getHeaderViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    /**
     * remove all footer view from mFooterLayout and set null to mFooterLayout
     */
    public void removeAllFooterView() {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeAllViews();
        int position = getFooterViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    private int getHeaderViewPosition() {
        //Return to header view notify position
        if (getEmptyViewCount() == 1) {
            if (mHeadAndEmptyEnable) {
                return 0;
            }
        } else {
            return 0;
        }
        return -1;
    }

    private int getFooterViewPosition() {
        //Return to footer view notify position
        if (getEmptyViewCount() == 1) {
            int position = 1;
            if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                position++;
            }
            if (mFootAndEmptyEnable) {
                return position;
            }
        } else {
            return getHeaderLayoutCount() + mData.size();
        }
        return -1;
    }

    public void setEmptyView(int layoutResId, ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        setEmptyView(view);
    }

    /**
     * bind recyclerView {@link #bindToRecyclerView(RecyclerView)} before use!
     *
     * @see #bindToRecyclerView(RecyclerView)
     */
    public void setEmptyView(int layoutResId) {
        checkNotNull();
        setEmptyView(layoutResId, getRecyclerView());
    }

    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (mEmptyLayout == null) {
            mEmptyLayout = new FrameLayout(emptyView.getContext());
            final LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mEmptyLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        mEmptyLayout.removeAllViews();
        mEmptyLayout.addView(emptyView);
        mIsUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == 1) {
                int position = 0;
                if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                    position++;
                }
                notifyItemInserted(position);
            }
        }
    }

    /**
     * Call before {@link RecyclerView#setAdapter(RecyclerView.Adapter)}
     *
     * @param isHeadAndEmpty false will not show headView if the data is empty true will show emptyView and headView
     */
    public void setHeaderAndEmpty(boolean isHeadAndEmpty) {
        setHeaderFooterEmpty(isHeadAndEmpty, false);
    }

    /**
     * set emptyView show if adapter is empty and want to show headview and footview
     * Call before {@link RecyclerView#setAdapter(RecyclerView.Adapter)}
     *
     * @param isHeadAndEmpty
     * @param isFootAndEmpty
     */
    public void setHeaderFooterEmpty(boolean isHeadAndEmpty, boolean isFootAndEmpty) {
        mHeadAndEmptyEnable = isHeadAndEmpty;
        mFootAndEmptyEnable = isFootAndEmpty;
    }

    public void isUseEmpty(boolean isUseEmpty) {
        mIsUseEmpty = isUseEmpty;
    }

    public View getEmptyView() {
        return mEmptyLayout;
    }

    public void removeEmptyView() {

        if (null != mEmptyLayout) {
            mEmptyLayout.removeAllViews();
            mEmptyLayout.setVisibility(View.GONE);
        }
    }

    protected void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * set anim to start when loading
     *
     * @param anim
     * @param index
     */
    protected void startAnim(Animator anim, int index) {
        anim.setDuration(mDuration).start();
        anim.setInterpolator(mInterpolator);
    }

    /**
     * @param layoutResId ID for an XML layout resource to load
     * @param parent      Optional view to be the parent of the generated hierarchy or else simply an object that
     *                    provides a set of LayoutParams values for root of the returned
     *                    hierarchy
     * @return view will be return
     */
    protected View getItemView(@LayoutRes int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case BaseAnimation.ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case BaseAnimation.SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case BaseAnimation.SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case BaseAnimation.SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case BaseAnimation.SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    protected abstract void convert(K holder, T result, int position);

    public View getViewByPosition(int position, @IdRes int viewId) {
        checkNotNull();
        return getViewByPosition(getRecyclerView(), position, viewId);
    }

    public View getViewByPosition(RecyclerView recyclerView, int position, @IdRes int viewId) {
        if (recyclerView == null) {
            return null;
        }
        BaseViewHolder viewHolder = (BaseViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
        if (viewHolder == null) {
            return null;
        }
        return viewHolder.getView(viewId);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}