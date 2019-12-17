package android.com.syncrow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG ="SettingsActivity" ;
    private static final String SYNCRO_TIME_KEY = "SYNCRO_TIME";
    private static final String TIME_PREFS ="TIME_PREFS" ;
    private TextView mSyncTimebtn;
    private Button mSyncHistorybtn;
    private TextView mDirectoryPath;
    private String filePath;
    int hrOfday;
    int mintOfday;
    String amPm=" ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDirectoryPath=findViewById(R.id.setting_filepath);
        mSyncTimebtn=findViewById(R.id.setting_sync_time);

        mSyncHistorybtn=findViewById(R.id.settings_sync_history);
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            filePath=extras.getString("FilePath");
        }else{
            filePath="Path not received";
        }
        mDirectoryPath.setText(filePath);

        mSyncTimebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int currentHour= calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute= calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog=new TimePickerDialog(SettingsActivity.this,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hrOfday=hourOfDay;
                        mintOfday=minute;
                        if(hourOfDay > 12){
                            amPm="PM";
                        }else{
                            amPm="AM";
                        }
                        mSyncTimebtn.setText(String.format("%02d : %02d", hourOfDay, minute)+amPm);
                        saveTime();

                    }
                },currentHour,currentMinute,false);
                timePickerDialog.show();

            }
        });







    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs=getSharedPreferences(TIME_PREFS,MODE_PRIVATE);
        String mDisplayTime = prefs.getString(SYNCRO_TIME_KEY,null);
        mSyncTimebtn.setText(mDisplayTime);
    }
    private void saveTime(){
        String time=mSyncTimebtn.getText().toString();
        SharedPreferences prefs=getSharedPreferences(TIME_PREFS,0);
        prefs.edit().putString(SYNCRO_TIME_KEY,time).apply();
    }

}
