package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.adapter.dropdownmenuadapter.BaseDropDownAdapter;
import com.wxt.library.base.adapter.dropdownmenuadapter.ListDropDownAdapter;
import com.wxt.library.base.adapter.dropdownmenuadapter.MutilSelectDropDownAdapter;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.SharedPreferenceUtil;
import com.wxt.library.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2015/6/17.
 */
public class DropDownMenu extends LinearLayout {

    private static final String SHARE_FILENAME = "dropdownmenusharefilename";
    private static final String SHARE_KEY = "tabwrapcontent";

    private LayoutInflater inflater = null;
    // 顶部菜单布局
    private LinearLayout tabMenuView;
    // 底部容器，包含popupMenuViews，maskView
    private FrameLayout containerView;
    // 弹出菜单父布局
    private FrameLayout popupMenuViews;
    // 遮罩半透明View，点击可关闭DropDownMenu
    private View maskView;
    // tabMenuView里面选中的tab位置，-1表示未选中
    private int current_tab_position = -1;

    // 分割线颜色
    private int dividerColor = 0xffcccccc;
    // tab选中颜色
    private int textSelectedColor = 0xff890c85;
    // tab未选中颜色
    private int textUnselectedColor = 0xff111111;
    // 遮罩颜色
    private int maskColor = 0x88888888;
    // tab字体大小
    private int menuTextSize;

    //
    private boolean isWarp;
    //    private boolean isCenter;
    // tab选中图标
    private int menuSelectedIcon;
    // tab未选中图标
    private int menuUnselectedIcon;
    // tab是否自适应宽度
    private boolean menuTabWrapContent = false;
    // tab的最小宽度（menuTabWrapContent true是有效）
    private float menuTabMinWidth = 0f;

    private float arrowWidth;
    private float arrowHeight;

    // tab的高度,高度指定时padding失效
    private int menuTabHeight = 0;
    // tab的上下padding（menuTabHeight>0 则无效）
    private int menuTabPadding = 0;
    // tab中文字和图片是否同时居中
    private boolean menuTabDrawableCenter = false;
    // tab中文字和图片是否同时居中时，文字和图片的间隔
    private float menuTabDrawableCenterPadding;
    // tab中text的高度
    private int tabTextHeight;
    //
    private AutoFillEmptyHorizontalScrollView scrollView;

    private List<String> tabTexts;
    private List<View> popupViews;
    private View contentView;

    public interface InterceptClickListener {
        boolean isInterruptOpenOrClose(View view, int index);
    }

    public DropDownMenu(Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    public DropDownMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        inflater = LayoutInflater.from(getContext());

        // 为DropDownMenu添加自定义属性
        int menuBackgroundColor = 0xffffffff;
        int underlineColor = 0xffcccccc;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownMenu);
        underlineColor = a.getColor(R.styleable.DropDownMenu_ddunderlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.DropDownMenu_dddividerColor, dividerColor);
        textSelectedColor = a.getColor(R.styleable.DropDownMenu_ddtextSelectedColor, textSelectedColor);
        textUnselectedColor = a.getColor(R.styleable.DropDownMenu_ddtextUnselectedColor, textUnselectedColor);
        menuBackgroundColor = a.getColor(R.styleable.DropDownMenu_ddmenuBackgroundColor, menuBackgroundColor);
        maskColor = a.getColor(R.styleable.DropDownMenu_ddmaskColor, maskColor);
        menuTextSize = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuTextSize, Util.dp2px(getContext(), 14));
        arrowWidth = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuArrowWidth, Util.dp2px(getContext(), 14));
        arrowHeight = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuArrowHeight, Util.dp2px(getContext(), 14));
        menuSelectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuSelectedIcon, R.drawable.drop_down_selected_icon);
        menuUnselectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuUnselectedIcon, R.drawable.drop_down_unselected_icon);
        menuTabWrapContent = a.getBoolean(R.styleable.DropDownMenu_ddmenuTabWrapContent, false);
        menuTabDrawableCenter = a.getBoolean(R.styleable.DropDownMenu_ddmenuTabDrawableCenter, false);
        menuTabDrawableCenterPadding = a.getDimension(R.styleable.DropDownMenu_ddmenuTabDrawableCenterPadding, Util.dp2px(getContext(), 5));
        menuTabMinWidth = a.getDimension(R.styleable.DropDownMenu_ddmenuTabMinWidth, Util.dp2px(getContext(), 70));
        menuTabHeight = (int) a.getDimension(R.styleable.DropDownMenu_ddmenuTabHeight, Util.dp2px(getContext(), 40));
        menuTabPadding = (int) a.getDimension(R.styleable.DropDownMenu_ddmenuTabPadding, Util.dp2px(getContext(), 12));
        isWarp = a.getBoolean(R.styleable.DropDownMenu_ddmenuIsWrap, false);
        a.recycle();

        tabTextHeight = menuTabHeight > 0 ? menuTabHeight : ViewGroup.LayoutParams.WRAP_CONTENT;

        // 初始化tabMenuView并添加到tabMenuView
        tabMenuView = new LinearLayout(context);
        tabMenuView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabMenuView.setOrientation(HORIZONTAL);
        tabMenuView.setBackgroundColor(menuBackgroundColor);

        // 图片居中的话，文字必须全部显示，即控件左右可滑动，控件不够的话（文本自己不滚动）
        if (menuTabDrawableCenter) menuTabWrapContent = menuTabDrawableCenter;

        if (menuTabWrapContent) {
            scrollView = new AutoFillEmptyHorizontalScrollView(getContext());
            scrollView.setHorizontalScrollBarEnabled(false);
            scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.addView(tabMenuView);
            addView(scrollView, 0);
        } else {
            addView(tabMenuView, 0);
        }

        // 为tabMenuView添加下划线
        View underLine = new View(getContext());
        underLine.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Util.dp2px(getContext(), 0.5f)));
        underLine.setBackgroundColor(underlineColor);
        addView(underLine, 1);

        // 初始化containerView并将其添加到DropDownMenu
        containerView = new FrameLayout(context);
        containerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        addView(containerView, 2);
    }

    private Drawable getVectorDrawable(int resourceId) {
        if (resourceId != -1) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), resourceId);
            if (drawable != null) {
                return drawable;
            }
        }
        return null;
    }

    public boolean isMenuTabWrapContent() {
        return menuTabWrapContent;
    }

    public void setMenuTabWrapContent(boolean isWrap) {

        if (isWrap != menuTabWrapContent && !menuTabDrawableCenter) {
            menuTabWrapContent = isWrap;

            if (menuTabWrapContent) {
                removeViewAt(0);
                scrollView = new AutoFillEmptyHorizontalScrollView(getContext());
                scrollView.setHorizontalScrollBarEnabled(false);
                scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                scrollView.addView(tabMenuView);
                addView(scrollView, 0);
                for (int i = 0; i < tabMenuView.getChildCount(); i++) {
                    if (tabMenuView.getChildAt(i) instanceof LinearLayout) {
                        tabMenuView.getChildAt(i).setLayoutParams(new LinearLayout.LayoutParams(20, tabTextHeight));
                        tabMenuView.getChildAt(i).setMinimumWidth((int) menuTabMinWidth);
                    }
                }
            } else {
                scrollView.removeAllViews();
                removeViewAt(0);
                addView(tabMenuView, 0);
                for (int i = 0; i < tabMenuView.getChildCount(); i++) {
                    if (tabMenuView.getChildAt(i) instanceof LinearLayout) {
                        LinearLayout child = (LinearLayout) tabMenuView.getChildAt(i);
                        tabMenuView.getChildAt(i).setLayoutParams(new LinearLayout.LayoutParams(0, tabTextHeight, 1.0f));
                        getTextView(child).setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        getTextView(child).setMarqueeRepeatLimit(-1);
                    }
                }
            }

            setMatch(menuTabWrapContent);

            SharedPreferenceUtil.getInstance(getContext()).saveParam(SHARE_FILENAME, SHARE_KEY, menuTabWrapContent ? true : false);
        }
    }

    /**
     * 设置弹出的列表是否充满还是对齐
     *
     * @param isWrap
     */
    private void setMatch(boolean isWrap) {
        if (isWarp) {
            int length = popupMenuViews.getChildCount();

            for (int i = 0; i < length; i++) {
                FrameLayout.LayoutParams params = null;
                if (!isWrap) {
                    params = new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels / length, FrameLayout.LayoutParams.WRAP_CONTENT);
                } else {
                    params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                }
                params.setMargins(getResources().getDisplayMetrics().widthPixels * i / length, 0, 0, 0);
                popupViews.get(i).setLayoutParams(params);
            }
        }
    }

    public DropDownMenu setTabTexts(List<String> tabTexts) {
        this.tabTexts = tabTexts;
        return this;
    }

    public DropDownMenu setPopupViews(List<View> popupViews) {
        this.popupViews = popupViews;
        return this;
    }

    public DropDownMenu setContentView(View contentView) {
        this.contentView = contentView;
        return this;
    }

    public void show() {
        if (tabTexts.size() != popupViews.size() || tabTexts.size() == 0) {
            throw new IllegalArgumentException("params not match, tabTexts.size() should be equal popupViews.size()");
        }
        if (tabMenuView.getChildCount() > 0) {
            tabMenuView.removeAllViews();
        }
        if (containerView.getChildCount() > 0) {
            // popupMenuViews 移除
            ((ViewGroup) containerView.getChildAt(2)).removeAllViews();
            containerView.removeAllViews();
        }
        for (int i = 0; i < this.tabTexts.size(); i++) {
            addTab(this.tabTexts, i);
        }
        int index = 0;
        if (contentView != null) {
            containerView.addView(contentView, index++);
        } else {
            containerView.addView(new DefaultNullRecyclerView(getContext()), index++);
        }

        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView, index++);
        maskView.setVisibility(GONE);

        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews, index++);

        boolean isWrapContent = SharedPreferenceUtil.getInstance(getContext()).readBooleanParam(SHARE_FILENAME, SHARE_KEY, menuTabWrapContent);

        for (int i = 0; i < popupViews.size(); i++) {
            if (popupViews.get(i) instanceof RecyclerView) {
                RecyclerView.Adapter adapter = ((RecyclerView) popupViews.get(i)).getAdapter();
                if (adapter instanceof BaseDropDownAdapter) {
                    ((BaseDropDownAdapter) adapter).setTabText(tabTexts.get(i));
                    ((BaseDropDownAdapter) adapter).setViewIndex(i);
                    ((BaseDropDownAdapter) adapter).setDropDownView(this);
                }
            }
            popupMenuViews.addView(popupViews.get(i), i);
        }

        setMatch(menuTabWrapContent);

        setMenuTabWrapContent(isWrapContent);
    }

    private void addTab(@NonNull List<String> tabTexts, final int i) {

        final LinearLayout rootLayout = (LinearLayout) inflater.inflate(R.layout.ddmenu_warp_content, null);

        AlwaysMarqueeTextView textView = rootLayout.findViewById(R.id.ddmenu_tab_tv);
        AppCompatImageView imageView = rootLayout.findViewById(R.id.ddmenu_tab_icon_im);
        View blank1 = rootLayout.findViewById(R.id.ddmenu_blank1);
        View blank2 = rootLayout.findViewById(R.id.ddmenu_blank2);

        imageView.setImageDrawable(getVectorDrawable(menuUnselectedIcon));

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, menuTextSize);
        textView.setText(tabTexts.get(i));
        textView.setTextColor(textUnselectedColor);

        if (menuTabDrawableCenter) {
            rootLayout.setLayoutParams(new LinearLayout.LayoutParams(20, tabTextHeight));
            rootLayout.setMinimumWidth((int) menuTabMinWidth);
            // 图片文字都居中，则间距生效
            ((LinearLayout.LayoutParams) imageView.getLayoutParams()).setMargins((int) menuTabDrawableCenterPadding, 0, 0, 0);
            textView.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
            blank1.setVisibility(VISIBLE);
            blank2.setVisibility(VISIBLE);
        } else {
            blank1.setVisibility(GONE);
            blank2.setVisibility(GONE);
            textView.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
            ((LayoutParams) textView.getLayoutParams()).weight = 1;
            if (menuTabWrapContent) {
                rootLayout.setLayoutParams(new LinearLayout.LayoutParams(20, tabTextHeight));
                rootLayout.setMinimumWidth((int) menuTabMinWidth);
            } else {
                rootLayout.setLayoutParams(new LinearLayout.LayoutParams(0, tabTextHeight, 1.0f));
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setMarqueeRepeatLimit(-1);
            }
        }

        // 添加点击事件
        rootLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMenu(rootLayout, i);
            }
        });

        tabMenuView.addView(rootLayout);

        // 添加分割线
        if (i < tabTexts.size() - 1) {
            View view = new View(getContext());
            view.setLayoutParams(new LayoutParams(Util.dp2px(getContext(), 0.5f), ViewGroup.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(dividerColor);
            tabMenuView.addView(view);
        }
    }

    private TextView getTextView(LinearLayout rootLayout) {
        TextView textView = null;
        if (rootLayout.getChildCount() > 2) {
            textView = (TextView) rootLayout.getChildAt(1);
        } else {
            textView = (TextView) rootLayout.getChildAt(0);
        }
        return textView;
    }

    private TextView getTextView(int index) {
        LinearLayout rootLayout = (LinearLayout) tabMenuView.getChildAt(index);
        TextView textView = null;
        if (rootLayout.getChildCount() > 2) {
            textView = (TextView) rootLayout.getChildAt(1);
        } else {
            textView = (TextView) rootLayout.getChildAt(0);
        }
        return textView;
    }

    private ImageView getImageView(int index) {
        LinearLayout rootLayout = (LinearLayout) tabMenuView.getChildAt(index);
        return (ImageView) rootLayout.getChildAt(rootLayout.getChildCount() / 2);
    }

    /**
     * 改变tab文字
     *
     * @param text
     */
    public void setTabText(String text) {
        if (current_tab_position != -1) {
            getTextView(current_tab_position).setText(text);
        }
    }

    public void setTabText(String text, int position) {
        if (position <= tabMenuView.getChildCount() / 2) {
            getTextView(position * 2).setText(text);
        }
    }

    public void setTabClickable(boolean clickable) {
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            tabMenuView.getChildAt(i).setClickable(clickable);
        }
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (current_tab_position != -1) {
            getTextView(current_tab_position).setTextColor(textUnselectedColor);
            getImageView(current_tab_position).setImageDrawable(getVectorDrawable(menuUnselectedIcon));
            popupMenuViews.setVisibility(View.GONE);
            popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
            maskView.setVisibility(GONE);
            maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
            current_tab_position = -1;
        }
    }

    /**
     * DropDownMenu是否处于可见状态
     *
     * @return
     */
    public boolean isShowing() {
        return current_tab_position != -1;
    }

    public int getSelectPosition() {
        return current_tab_position;
    }

    /**
     * 切换菜单
     *
     * @param target
     */
    private void switchMenu(View target, int index) {
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            if (target == tabMenuView.getChildAt(i)) {
                if (getContext() instanceof InterceptClickListener) {
                    InterceptClickListener listener = (InterceptClickListener) getContext();
                    if (listener.isInterruptOpenOrClose(getTextView((LinearLayout) target), index)) {
                        closeMenu();
                        continue;
                    }
                }
                if (current_tab_position == i) {
                    closeMenu();
                } else {
                    if (current_tab_position == -1) {
                        popupMenuViews.setVisibility(View.VISIBLE);
                        popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
                        maskView.setVisibility(VISIBLE);
                        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    } else {
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    }
                    current_tab_position = i;

                    getTextView(current_tab_position).setTextColor(textSelectedColor);
                    getImageView(current_tab_position).setImageDrawable(getVectorDrawable(menuSelectedIcon));
                }
            } else {
                getTextView(i).setTextColor(textUnselectedColor);
                getImageView(i).setImageDrawable(getVectorDrawable(menuUnselectedIcon));
                popupMenuViews.getChildAt(i / 2).setVisibility(View.GONE);
            }
        }
    }

    private class AutoFillEmptyHorizontalScrollView extends HorizontalScrollView {

        private float screenWidth;

        public AutoFillEmptyHorizontalScrollView(Context context) {
            super(context);
            init();
        }

        public AutoFillEmptyHorizontalScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public AutoFillEmptyHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            screenWidth = getResources().getDisplayMetrics().widthPixels;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            View view = getChildAt(0);

            int count = ((LinearLayout) view).getChildCount();
            float childTotalWidth = 0f;
            List<View> childList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                View child = ((LinearLayout) view).getChildAt(i);
                int measureWidth = child.getMeasuredWidth();
                childTotalWidth += measureWidth;
                if (measureWidth > Util.dp2px(getContext(), 5)) {
                    childList.add(child);
                }
            }

            float residueWidth = screenWidth - childTotalWidth;
            if (residueWidth > 0) {

                int viewHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, view.getPaddingTop()
                        + view.getPaddingBottom(), view.getLayoutParams().height);
                int viewWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) screenWidth, MeasureSpec.EXACTLY);
                view.measure(viewWidthMeasureSpec, viewHeightMeasureSpec);

                int length = childList.size();
                float addWidth = residueWidth / length;
                for (int j = 0; j < length; j++) {
                    View child = childList.get(j);
                    final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();

                    int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, child.getPaddingTop() + child.getPaddingBottom(), lp.height);
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (child.getMeasuredWidth() + addWidth), MeasureSpec.EXACTLY);

                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        tabTexts = (List<String>) ss.childrenStates.get(index++);
        show();
        for (int i = 0; i < tabTexts.size(); i++) {
            getTextView(i * 2).setText((String) ss.childrenStates.get(index++));
        }
        int openMenuIndex = (int) ss.childrenStates.get(index++);
        if (openMenuIndex != -1) {
            // 当前有打开的菜单，需要恢复
            tabMenuView.getChildAt(openMenuIndex).performClick();
        }
        menuTabWrapContent = (boolean) ss.childrenStates.get(index++);
        setMenuTabWrapContent(menuTabWrapContent);

        for (int i = 0; i < popupMenuViews.getChildCount(); i++) {
            if (popupMenuViews.getChildAt(i) instanceof RecyclerView) {
                RecyclerView.Adapter adapter = ((RecyclerView) popupMenuViews.getChildAt(i)).getAdapter();
                if (adapter instanceof BaseDropDownAdapter) {
                    if (adapter instanceof ListDropDownAdapter) {
                        ((ListDropDownAdapter) adapter).setCheckItemPosition((int) ss.childrenStates.get(index++), false);
                    } else if (adapter instanceof MutilSelectDropDownAdapter) {
                        ((MutilSelectDropDownAdapter) adapter).setCheckItemPosition((String) ss.childrenStates.get(index++));
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        try {
            ss.childrenStates = new SparseArray<Integer>();

            int index = 1;
            ss.childrenStates.put(index++, tabTexts);
            for (int i = 0; i < tabTexts.size(); i++) {
                ss.childrenStates.put(index++, getTextView(i * 2).getText());
            }
            ss.childrenStates.put(index++, current_tab_position);
            ss.childrenStates.put(index++, menuTabWrapContent);

            for (int i = 0; i < popupMenuViews.getChildCount(); i++) {
                if (popupMenuViews.getChildAt(i) instanceof RecyclerView) {
                    RecyclerView.Adapter adapter = ((RecyclerView) popupMenuViews.getChildAt(i)).getAdapter();
                    if (adapter instanceof BaseDropDownAdapter) {
                        if (adapter instanceof ListDropDownAdapter) {
                            ss.childrenStates.put(index++, ((ListDropDownAdapter) adapter).getCheckItemPosition());
                        } else if (adapter instanceof MutilSelectDropDownAdapter) {
                            ss.childrenStates.put(index++, ((MutilSelectDropDownAdapter) adapter).getCheckItemPosition());
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return ss;
    }

}
