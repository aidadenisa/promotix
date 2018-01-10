package blog.aida.promotixproject.util;



import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

import blog.aida.promotixproject.R;

/**
 * Created by aida on 07-Jan-18.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static int year;
    private static int month;
    private static int day;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePicker dialogPicker = datePickerDialog.getDatePicker();

        dialogPicker.setMinDate(c.getTimeInMillis());

        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user

        TextView dateChosenView = (TextView) getActivity().findViewById(R.id.add_promotion_show_date);
        this.day=day;
        this.month=month;
        this.year = year;
        dateChosenView.setText(day + "-" + (month + 1) + "-" + year);
    }

    public static int getDay(){return day;}

    public static int getMonth(){return month;}

    public static int getYear(){return year;}
}
