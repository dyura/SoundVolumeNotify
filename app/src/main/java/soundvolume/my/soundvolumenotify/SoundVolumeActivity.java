package soundvolume.my.soundvolumenotify;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import static android.widget.SeekBar.*;

public class SoundVolumeActivity extends AppCompatActivity {
    public static NotificationFunc sn = new NotificationFunc() ;
    public static final int NOTIFICATION_ID = 1;
    final public static String ONE_TIME = "onetime";
    private AlarmManagerBroadcastReceiver checkSounds;
    private Button button;
    SeekBar seekBar;
    Context context;
    AudioManager audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_volume);
        checkSounds = new AlarmManagerBroadcastReceiver();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new mySeekBarListener());

//        Context context = this.getApplicationContext();
        context = this.getApplicationContext();
        SetTimer(context);

//        button = (Button) findViewById(R.id.btQuit);
//        button.setText(Html.fromHtml("Quit application<br><font size=\"10\">(by Force Stop)</font>"));
//        button.setText("Quit application\n(by Force Stop)");

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.display_info:
                // User chose the "Settings" item, show the app settings UI...
//                Context context = this.getApplicationContext();
                Toast.makeText(context, "show info...", Toast.LENGTH_LONG).show();
                AlertDialog alertDialog = new AlertDialog.Builder(SoundVolumeActivity.this).create();
                alertDialog.setTitle("What is this app for:");
                alertDialog.setMessage(getString(R.string.info));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    protected void onStart() {
        super.onStart();
//            button = (Button) findViewById(R.id.btStart);
//            button.setEnabled(true);
//        Context context = this.getApplicationContext();
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //SeekBar mySeekBar=(SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(audio.getStreamVolume(AudioManager.STREAM_RING));
        //audio.getStreamVolume(AudioManager.STREAM_RING);
        //audio.setStreamVolume(STREAM_RING);
    }

    public void startTimer(View view) {
//        Context context = this.getApplicationContext();
        SetTimer(context);
    }

    public void cancelTimer(View view) {
//        Context context = this.getApplicationContext();
        CancelTimer(context);
//        button = (Button) findViewById(R.id.btStart);
//        button.setEnabled(true);
    }
   public void SetTimer(Context context)
   {
       AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
       Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
       intent.putExtra("name","main".toString());
       intent.putExtra(ONE_TIME, Boolean.FALSE);
       PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
       am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pi);
       Toast.makeText(context, "STARTING...", Toast.LENGTH_LONG).show();
       sn.sendNotification(context,NOTIFICATION_ID);
   }

   public void CancelTimer(Context context)
   {
       Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
       PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
       AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
       alarmManager.cancel(sender);
       NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
       StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
       if (activeNotifications.length !=0)
               sn.cancelNotification(context,activeNotifications[0].getTag(),NOTIFICATION_ID);
   }

    public void quitApp(View view){
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private class mySeekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d("DEBUG", "Progress is: "+seekBar.getProgress());
            audio.setStreamVolume(AudioManager.STREAM_RING,seekBar.getProgress(),0);
            sn.sendNotification(context,NOTIFICATION_ID);
        }

    }
}
