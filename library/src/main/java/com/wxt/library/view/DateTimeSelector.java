package com.wxt.library.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.wxt.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class DateTimeSelector {

    public interface SelectDateTimeListener {
        void onSelectClick(boolean isConfirm, String dateTime);

        void onSelectScroll(String dateTime);
    }

    //---------------------------------------------------------------------------------------------------
    private final String TIME_FORMAT_YEARSECOND = "yyyy-MM-dd HH:mm:ss";//19
    private final String TIME_FORMAT_YEARMINUTE = "yyyy-MM-dd HH:mm";//16
    private final String TIME_FORMAT_YEARHOUR = "yyyy-MM-dd HH";//13
    private final String TIME_FORMAT_YEARDAY = "yyyy-MM-dd";//10
    private final String TIME_FORMAT_HOURMINUTE = "HH:mm";//5
    private final String TIME_FORMAT_HOURSECOND = "HH:mm:ss";//8

    private final int MIN_SECOND = 0;
    private final int MAX_SECOND = 59;
    private final int MIN_MINUTE = 0;
    private final int MAX_MINUTE = 59;
    private final int MIN_HOUR = 0;
    private final int MAX_HOUR = 23;
    private final int MIN_DAY = 1;
    private final int MIN_MONTH = 1;
    private final int MAX_MONTH = 12;

    private final long ANIMATOR_DELAY = 200L;
    private final long CHANGE_DELAY = 90L;

    private Context context;
    private SelectDateTimeListener selectDateTimeListener;

    private Calendar startCalendar;
    private Calendar endCalendar;

    private Dialog seletorDialog;

    private int startYear, startMonth, startDay, startHour, startMininute, startSecond;
    private int endYear, endMonth, endDay, endHour, endMininute, endSecond;
    private ArrayList<String> year, month, day, hour, minute, second;

    private int selectYear, selectMonth, selectDay, selectHour, selectMinute, selectSecond;
    //----------
    private TextView tv_cancle;
    private TextView tv_select, tv_title;

    private PickerView year_pv;
    private PickerView month_pv;
    private PickerView day_pv;
    private PickerView hour_pv;
    private PickerView minute_pv;
    private PickerView second_pv;

    private TextView year_text;
    private TextView month_text;
    private TextView day_text;
    private TextView hour_text;
    private TextView minute_text;
    private TextView second_text;

    private Calendar defaultCalendar;

    private SimpleDateFormat format;

    public DateTimeSelector(Context context, SelectDateTimeListener selectDateTimeListener, @NonNull String startDate, @NonNull String endDate, SimpleDateFormat format) {
        this.context = context;
        this.format = format;
        this.selectDateTimeListener = selectDateTimeListener;
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        if (startDate.length() != endDate.length()) {
            throw new IllegalArgumentException("日期格式不一致");
        }
        if (format != null) {
            if (format.toPattern().contains("sss") || format.toPattern().contains("h")) {
                throw new IllegalArgumentException("不支持毫秒或12小时制");
            }
        }
        startCalendar.setTime(formatStrToDate(startDate, format));
        endCalendar.setTime(formatStrToDate(endDate, format));
        initDialog();
        initView();
    }

    public DateTimeSelector(Context context, SelectDateTimeListener selectDateTimeListener, @NonNull String startDate, @NonNull String endDate) {
        this(context, selectDateTimeListener, startDate, endDate, null);
    }

    public DateTimeSelector setTitle(String title) {
        tv_title.setText(title);
        return this;
    }

    public DateTimeSelector setDefaultDate(String defaultDate) {
        defaultCalendar = Calendar.getInstance();
        Date date = formatStrToDate(defaultDate, this.format);
        if (date.getTime() <= startCalendar.getTime().getTime()) {
            defaultCalendar.set(Calendar.YEAR, startCalendar.get(Calendar.YEAR));
            defaultCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH));
            defaultCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.get(Calendar.DAY_OF_MONTH));
            defaultCalendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
            defaultCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
            defaultCalendar.set(Calendar.SECOND, startCalendar.get(Calendar.SECOND));
        } else if (date.getTime() >= endCalendar.getTime().getTime()) {
            defaultCalendar.set(Calendar.YEAR, endCalendar.get(Calendar.YEAR));
            defaultCalendar.set(Calendar.MONTH, endCalendar.get(Calendar.MONTH));
            defaultCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.get(Calendar.DAY_OF_MONTH));
            defaultCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY));
            defaultCalendar.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE));
            defaultCalendar.set(Calendar.SECOND, endCalendar.get(Calendar.SECOND));
        } else {
            defaultCalendar.setTime(date);
        }

        return this;
    }

    /**
     * 根据指定日期返回相应的Date
     *
     * @param formatStr
     * @return
     */
    private Date formatStrToDate(String formatStr, SimpleDateFormat format) {
        if (TextUtils.isEmpty(formatStr)) {
            throw new IllegalArgumentException("格式化字符串为空!");
        }
        int length = formatStr.length();
        try {
            if (format != null) {
                return format.parse(formatStr);
            }
            if (length == TIME_FORMAT_YEARSECOND.length()) {
                //"yyyy-MM-dd HH:mm:ss"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_YEARSECOND)).parse(formatStr);
            } else if (length == TIME_FORMAT_YEARMINUTE.length()) {
                //"yyyy-MM-dd HH:mm"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_YEARMINUTE)).parse(formatStr);
            } else if (length == TIME_FORMAT_YEARHOUR.length()) {
                //"yyyy-MM-dd HH"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_YEARHOUR)).parse(formatStr);
            } else if (length == TIME_FORMAT_YEARDAY.length()) {
                //"yyyy-MM-dd"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_YEARDAY)).parse(formatStr);
            } else if (length == TIME_FORMAT_HOURMINUTE.length()) {
                //"HH:mm"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_HOURMINUTE)).parse(formatStr);
            } else if (length == TIME_FORMAT_HOURSECOND.length()) {
                //"HH:mm:ss"
                return (this.format = new SimpleDateFormat(TIME_FORMAT_HOURSECOND)).parse(formatStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("格式化字符串错误!请修改字符串格式或指定格式");
    }

    /**
     * 创建显示的Dialog
     */
    private void initDialog() {
        if (seletorDialog == null) {
            seletorDialog = new Dialog(context, R.style.Time_Dialog);
            seletorDialog.setCancelable(true);
            seletorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            seletorDialog.setContentView(R.layout.dialog_selector);
            Window window = seletorDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = context.getResources().getDisplayMetrics().widthPixels;
            window.setAttributes(lp);
        }
    }

    /**
     * 创建
     */
    private void initView() {
        tv_cancle = seletorDialog.findViewById(R.id.tv_cancle);
        tv_select = seletorDialog.findViewById(R.id.tv_select);
        tv_title = seletorDialog.findViewById(R.id.tv_title);

        year_pv = seletorDialog.findViewById(R.id.year_pv);
        month_pv = seletorDialog.findViewById(R.id.month_pv);
        day_pv = seletorDialog.findViewById(R.id.day_pv);
        hour_pv = seletorDialog.findViewById(R.id.hour_pv);
        minute_pv = seletorDialog.findViewById(R.id.minute_pv);
        second_pv = seletorDialog.findViewById(R.id.second_pv);

        year_text = seletorDialog.findViewById(R.id.year_text);
        month_text = seletorDialog.findViewById(R.id.month_text);
        day_text = seletorDialog.findViewById(R.id.day_text);
        hour_text = seletorDialog.findViewById(R.id.hour_text);
        minute_text = seletorDialog.findViewById(R.id.minute_text);
        second_text = seletorDialog.findViewById(R.id.second_text);

        tv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeChanged(false);
                seletorDialog.dismiss();
            }
        });

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeChanged(true);
                seletorDialog.dismiss();
            }
        });

        String formatStr = format.toPattern();
        if (!formatStr.contains("yy")) {
            year_pv.setVisibility(View.GONE);
            year_text.setVisibility(View.GONE);
        }
        if (!formatStr.contains("MM")) {
            month_pv.setVisibility(View.GONE);
            month_text.setVisibility(View.GONE);
        }
        if (!formatStr.contains("dd")) {
            day_pv.setVisibility(View.GONE);
            day_text.setVisibility(View.GONE);
        }
        if (!formatStr.contains("HH")) {
            hour_pv.setVisibility(View.GONE);
            hour_text.setVisibility(View.GONE);
        }
        if (!formatStr.contains("mm")) {
            minute_pv.setVisibility(View.GONE);
            minute_text.setVisibility(View.GONE);
        }
        if (!formatStr.contains("ss")) {
            second_pv.setVisibility(View.GONE);
            second_text.setVisibility(View.GONE);
        }
    }

    /**
     * 显示Dialog
     */
    public void show() {
        if (startCalendar.getTime().getTime() >= endCalendar.getTime().getTime()) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        initParameter();
        initTimer();
        addListener();

        seletorDialog.show();
    }

    private void initParameter() {
        // 开始日期属性 // 默认选择当前日期
        selectYear = startYear = startCalendar.get(Calendar.YEAR);
        selectMonth = startMonth = startCalendar.get(Calendar.MONTH) + 1;
        selectDay = startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        selectHour = startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        selectMinute = startMininute = startCalendar.get(Calendar.MINUTE);
        selectSecond = startSecond = startCalendar.get(Calendar.SECOND);

        // 结束日期属性
        endYear = endCalendar.get(Calendar.YEAR);
        endMonth = endCalendar.get(Calendar.MONTH) + 1;
        endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
        endMininute = endCalendar.get(Calendar.MINUTE);
        endSecond = endCalendar.get(Calendar.SECOND);

        if (defaultCalendar != null) {
            selectYear = defaultCalendar.get(Calendar.YEAR);
            selectMonth = defaultCalendar.get(Calendar.MONTH) + 1;
            selectDay = defaultCalendar.get(Calendar.DAY_OF_MONTH);
            selectHour = defaultCalendar.get(Calendar.HOUR_OF_DAY);
            selectMinute = defaultCalendar.get(Calendar.MINUTE);
            selectSecond = defaultCalendar.get(Calendar.SECOND);
        }
    }

    /**
     * 根据选择的年获取能显示的最大月份（1-12）
     *
     * @return
     */
    private int getMaxMonth() {
        return selectYear == endYear ? endMonth : MAX_MONTH;
    }

    /**
     * 根据选择的年获取能显示的最小月份（1-12）
     *
     * @return
     */
    private int getMinMonth() {
        return selectYear == startYear ? startMonth : MIN_MONTH;
    }

    /**
     * 根据选择的月获取能显示的最大天数（1-31），最大值不能超过当月的天数
     *
     * @return
     */
    private int getMaxDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectYear);
        calendar.set(Calendar.MONTH, selectMonth - 1);
        return (selectYear == endYear
                && selectMonth == endMonth) ? endDay : calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 根据选择的月获取能显示的最小天数（1-31），最大值不能超过当月的天数
     *
     * @return
     */
    private int getMinDay() {
        return (selectYear == startYear
                && selectMonth == startMonth) ? startDay : MIN_DAY;
    }

    /**
     * 根据选择的日获取能显示的最大小时（1-59）
     *
     * @return
     */
    private int getMaxHour() {
        return (selectYear == endYear
                && selectMonth == endMonth
                && selectDay == endDay) ? endHour : MAX_HOUR;
    }

    /**
     * 根据选择的日获取能显示的最小小时（1-59）
     *
     * @return
     */
    private int getMinHour() {
        return (selectYear == startYear
                && selectMonth == startMonth
                && selectDay == startDay) ? startHour : MIN_HOUR;
    }

    /**
     * 根据选择的小时获取能显示的最大分钟（0-59）
     *
     * @return
     */
    private int getMaxMinute() {
        return (selectYear == endYear
                && selectMonth == endMonth
                && selectDay == endDay
                && selectHour == endHour) ? endMininute : MAX_MINUTE;
    }

    /**
     * 根据选择的小时获取能显示的最小分钟（0-59）
     *
     * @return
     */
    private int getMinMinute() {
        return (selectYear == startYear
                && selectMonth == startMonth
                && selectDay == startDay
                && selectHour == startHour) ? startMininute : MIN_MINUTE;
    }

    /**
     * 根据选择的分钟获取能显示的最大秒数（0-59）
     *
     * @return
     */
    private int getMaxSecond() {
        return (selectYear == endYear
                && selectMonth == endMonth
                && selectDay == endDay
                && selectHour == endHour
                && selectMinute == endMininute) ? endSecond : MAX_SECOND;
    }

    /**
     * 根据选择的分钟获取能显示的最小秒数（0-59）
     *
     * @return
     */
    private int getMinSecond() {
        return (selectYear == startYear
                && selectMonth == startMonth
                && selectDay == startDay
                && selectHour == startHour
                && selectMinute == startMininute) ? startSecond : MIN_SECOND;
    }

    /**
     * 初始化选择空间的值
     */
    private void initTimer() {
        initArrayList();

        for (int i = startYear; i <= endYear; i++) {
            year.add(String.valueOf(i));
        }

        int initMinMonth = getMinMonth();
        int initMaxMonth = getMaxMonth();
        for (int i = initMinMonth; i <= initMaxMonth; i++) {
            month.add(formatTimeUnit(i));
        }

        int initMinDay = getMinDay();
        int initMaxDay = getMaxDay();
        for (int i = initMinDay; i <= initMaxDay; i++) {
            day.add(formatTimeUnit(i));
        }

        int initMinHour = getMinHour();
        int initMaxHour = getMaxHour();
        for (int i = initMinHour; i <= initMaxHour; i++) {
            hour.add(formatTimeUnit(i));
        }

        int initMinMinute = getMinMinute();
        int initMaxMinute = getMaxMinute();
        for (int i = initMinMinute; i <= initMaxMinute; i++) {
            minute.add(formatTimeUnit(i));
        }

        int initMinSecond = getMinSecond();
        int initMaxSecond = getMaxSecond();
        for (int i = initMinSecond; i <= initMaxSecond; i++) {
            second.add(formatTimeUnit(i));
        }

        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        hour_pv.setData(hour);
        minute_pv.setData(minute);
        second_pv.setData(second);

        year_pv.setSelected(formatTimeUnit(selectYear));
        month_pv.setSelected(formatTimeUnit(selectMonth));
        day_pv.setSelected(formatTimeUnit(selectDay));
        hour_pv.setSelected(formatTimeUnit(selectHour));
        minute_pv.setSelected(formatTimeUnit(selectMinute));
        second_pv.setSelected(formatTimeUnit(selectSecond));

        excuteScroll();
    }

    private void initArrayList() {
        if (year == null) year = new ArrayList<>();
        if (month == null) month = new ArrayList<>();
        if (day == null) day = new ArrayList<>();
        if (hour == null) hour = new ArrayList<>();
        if (minute == null) minute = new ArrayList<>();
        if (second == null) second = new ArrayList<>();
        year.clear();
        month.clear();
        day.clear();
        hour.clear();
        minute.clear();
        second.clear();
    }

    private String formatTimeUnit(int unit) {
        return unit < 10 ? "0" + String.valueOf(unit) : String.valueOf(unit);
    }

    private void addListener() {
        year_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectYear = Integer.parseInt(text);
                monthChange();
            }
        });
        month_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectMonth = Integer.parseInt(text);
                dayChange();
            }
        });
        day_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectDay = Integer.parseInt(text);
                hourChange();
            }
        });
        hour_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectHour = Integer.parseInt(text);
                minuteChange();
            }
        });
        minute_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectMinute = Integer.parseInt(text);
                secondChange();
            }
        });
        second_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectSecond = Integer.parseInt(text);
                timeChanged();
            }
        });
    }

    private void excuteScroll() {
        year_pv.setCanScroll(year.size() > 1);
        month_pv.setCanScroll(month.size() > 1);
        day_pv.setCanScroll(day.size() > 1);
        hour_pv.setCanScroll(hour.size() > 1);
        minute_pv.setCanScroll(minute.size() > 1);
        second_pv.setCanScroll(second.size() > 1);
    }

    private String getIndex(int curValue, List<String> valueList) {
        String timeValue = formatTimeUnit(curValue);
        String minValue = "60";
        String maxValue = "00";
        for (int i = 0; i < valueList.size(); i++) {
            String value = valueList.get(i);
            if (value.equals(timeValue)) {
                return value;
            }
            if (minValue.compareTo(value) > 0) {
                minValue = value;
            }
            if (maxValue.compareTo(value) < 0) {
                maxValue = value;
            }
        }

        if (minValue.compareTo(timeValue) > 0) {
            return minValue;
        }
        if (maxValue.compareTo(timeValue) < 0) {
            return maxValue;
        }
        throw new IllegalArgumentException("查找失败!");
    }

    private void monthChange() {
        int minMonth = getMinMonth();
        int maxMonth = getMaxMonth();

        ArrayList<String> tempList = new ArrayList<>();
        for (int i = minMonth; i <= maxMonth; i++) {
            tempList.add(formatTimeUnit(i));
        }

        int size = tempList.size();
        if (size != month.size() || (size == month.size() && tempList.get(0) != month.get(0))) {
            // 数据变化了
            month_pv.setData(month = tempList);
            String monthValue = getIndex(selectMonth, month);
            month_pv.setSelected(monthValue);
            selectMonth = Integer.valueOf(monthValue);

            excuteAnimator(ANIMATOR_DELAY, month_pv);

            month_pv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dayChange();
                }
            }, CHANGE_DELAY);

        }
    }

    private void dayChange() {
        int minDay = getMinDay();
        int maxDay = getMaxDay();

        ArrayList<String> tempList = new ArrayList<>();
        for (int i = minDay; i <= maxDay; i++) {
            tempList.add(formatTimeUnit(i));
        }

        int size = tempList.size();
        if (size != day.size() || (size == day.size() && tempList.get(0) != day.get(0))) {
            // 数据变化了
            day_pv.setData(day = tempList);
            String dayValue = getIndex(selectDay, day);
            day_pv.setSelected(dayValue);
            selectDay = Integer.valueOf(dayValue);

            excuteAnimator(ANIMATOR_DELAY, day_pv);

            day_pv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hourChange();
                }
            }, CHANGE_DELAY);

        }
    }

    private void hourChange() {
        int minHour = getMinHour();
        int maxHour = getMaxHour();

        ArrayList<String> tempList = new ArrayList<>();
        for (int i = minHour; i <= maxHour; i++) {
            tempList.add(formatTimeUnit(i));
        }

        int size = tempList.size();
        if (size != hour.size() || (size == hour.size() && tempList.get(0) != hour.get(0))) {
            // 数据变化了
            hour_pv.setData(hour = tempList);
            String hourValue = getIndex(selectHour, hour);
            hour_pv.setSelected(hourValue);
            selectHour = Integer.valueOf(hourValue);

            excuteAnimator(ANIMATOR_DELAY, hour_pv);

            hour_pv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    minuteChange();
                }
            }, CHANGE_DELAY);
        }
    }

    private void minuteChange() {
        int minMinute = getMinMinute();
        int maxMinute = getMaxMinute();

        ArrayList<String> tempList = new ArrayList<>();
        for (int i = minMinute; i <= maxMinute; i++) {
            tempList.add(formatTimeUnit(i));
        }

        int size = tempList.size();
        if (size != minute.size() || (size == minute.size() && tempList.get(0) != minute.get(0))) {
            // 数据变化了
            minute_pv.setData(minute = tempList);
            String minuteValue = getIndex(selectMinute, minute);
            minute_pv.setSelected(minuteValue);
            selectMinute = Integer.valueOf(minuteValue);

            excuteAnimator(ANIMATOR_DELAY, minute_pv);

            minute_pv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    secondChange();
                }
            }, CHANGE_DELAY);

        }
    }

    private void secondChange() {
        int minSecond = getMinSecond();
        int maxSecond = getMaxSecond();

        ArrayList<String> tempList = new ArrayList<>();
        for (int i = minSecond; i <= maxSecond; i++) {
            tempList.add(formatTimeUnit(i));
        }

        int size = tempList.size();
        if (size != second.size() || (size == second.size() && tempList.get(0) != second.get(0))) {
            // 数据变化了
            second_pv.setData(second = tempList);
            String secondValue = getIndex(selectSecond, second);
            second_pv.setSelected(secondValue);
            selectSecond = Integer.valueOf(secondValue);

            excuteAnimator(ANIMATOR_DELAY, second_pv);
        }

        timeChanged();
    }

    private Calendar getSelectCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectYear);
        calendar.set(Calendar.MONTH, selectMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, selectDay);
        calendar.set(Calendar.HOUR_OF_DAY, selectHour);
        calendar.set(Calendar.MINUTE, selectMinute);
        calendar.set(Calendar.SECOND, selectSecond);

        return calendar;
    }

    private String timeChanged() {
        String time = this.format.format(getSelectCalendar().getTime());
        if (selectDateTimeListener != null) {
            selectDateTimeListener.onSelectScroll(time);
        }

        return time;
    }

    private String timeChanged(boolean isConfirm) {
        String time = this.format.format(getSelectCalendar().getTime());
        if (selectDateTimeListener != null) {
            selectDateTimeListener.onSelectClick(isConfirm, time);
        }

        return time;
    }

    public void dismiss() {
        if (this.seletorDialog != null) {
            this.seletorDialog.dismiss();
        }
    }

    private void excuteAnimator(long time, View view) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f,
                1f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f,
                1f, 1f);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f,
                1f, 1f);
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ).setDuration(time).start();
    }

    public void setIsLoop(boolean isLoop) {
        this.year_pv.setIsLoop(isLoop);
        this.month_pv.setIsLoop(isLoop);
        this.day_pv.setIsLoop(isLoop);
        this.hour_pv.setIsLoop(isLoop);
        this.minute_pv.setIsLoop(isLoop);
        this.second_pv.setIsLoop(isLoop);
    }
}
