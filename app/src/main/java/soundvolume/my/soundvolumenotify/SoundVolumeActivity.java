package soundvolume.my.soundvolumenotify;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


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
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        seekBar=findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new mySeekBarListener());

        context = this.getApplicationContext();
        SetTimer(context);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.display_help:
//                Toast.makeText(context, "show info...", Toast.LENGTH_LONG).show();
                AlertDialog alertDialog = new AlertDialog.Builder(SoundVolumeActivity.this).create();
                alertDialog.setTitle("Help");
                alertDialog.setMessage(getString(R.string.help));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            case R.id.display_about:
                TextView showText = new TextView(this);
                showText.setText(getString(R.string.about));
                showText.setTextIsSelectable(true);
                showText.setTextSize(14);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
// Build the Dialog
                builder.setView(showText)
                        .setTitle(getString(R.string.app_name))
                        .setCancelable(true)
                        .show();
//                        .getWindow().setLayout(1600, 1000);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    protected void onStart() {
        super.onStart();
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        seekBar.setProgress(audio.getStreamVolume(AudioManager.STREAM_RING));
        seekBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        sn.sendNotification(context,NOTIFICATION_ID);
    }

    public void startTimer(View view) {
        SetTimer(context);
    }

    public void cancelTimer(View view) {
        CancelTimer(context);
    }
   public void SetTimer(Context context)
   {
       AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
       Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
       intent.putExtra("name","main");
       intent.putExtra(ONE_TIME, Boolean.FALSE);
       PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
       am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pi);
       Toast.makeText(context, "STARTING...", Toast.LENGTH_LONG).show();
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
