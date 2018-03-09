package xjy.smartcommunity.calendardecorator;

import android.graphics.Color;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Date;
import java.util.List;

/**
 * Created by yangjie on 2017/6/4.
 */

public class SpecialDayDecorator implements DayViewDecorator {
    private List<Date> dates = null;
    private int color = Color.GRAY;

    public SpecialDayDecorator(List<Date> dates){this.dates = dates;}
    public SpecialDayDecorator(List<Date> dates, int color){
        this.dates = dates;
        this.color = color;
    }

    @Override
    public void decorate(DayViewFacade view) {
        BackgroundColorSpan span = new BackgroundColorSpan(color);
        //ForegroundColorSpan span = new ForegroundColorSpan(color);
        view.addSpan(span);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if(dates.contains(day.getDate()))
            return true;
        return false;
    }
}
