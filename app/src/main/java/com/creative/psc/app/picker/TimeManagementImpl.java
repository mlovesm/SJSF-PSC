package com.creative.psc.app.picker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by GS on 2017-09-16.
 */

public class TimeManagementImpl implements TimeManagement {
    private static final String DATE_PICKER = "mDatePicker";
    private static final String DAY_FIELD = "day";
    private static final String ID = "id";
    private static final String ANDROID = "android";
    private Context mContext;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private Calendar mCalendar;

    public TimeManagementImpl(Context context) {
        mContext = context;
        mCalendar = Calendar.getInstance();
    }

    @Override
    public TimeManagement dialogDatePicker(DatePickerDialog.OnDateSetListener onDateSetListener) {
        mDatePickerDialog =
                new DatePickerDialog(mContext, onDateSetListener, mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        return this;
    }

    @Override
    public void showDatePickerDialog() {
        if (mDatePickerDialog == null) {
            return;
        }
        mDatePickerDialog.show();
    }

    @Override
    public TimeManagement dialogTimePicker(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        mTimePickerDialog = new TimePickerDialog(mContext, onTimeSetListener,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
        return this;
    }

    @Override
    public void showTimePickerDialog() {
        if (mTimePickerDialog == null) {
            return;
        }
        mTimePickerDialog.show();
    }
}
