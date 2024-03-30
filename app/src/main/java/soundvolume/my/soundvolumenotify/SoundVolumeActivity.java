package soundvolume.my.soundvolumenotify;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
//import android.util.Base64;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_MUTABLE;


public class SoundVolumeActivity extends AppCompatActivity {
    public static NotificationFunc sn = new NotificationFunc() ;
    public static final int NOTIFICATION_ID = 1;
    final public static String ONE_TIME = "onetime";
    private static boolean afterStart=false;
    private  boolean silenceMode =false;
    private  boolean zenMode=false;

    SeekBar seekBarMedia;
    SeekBar seekBarAlarm;
    SeekBar seekBarNotif;
    SeekBar seekBarRing;
    Context context;
    AudioManager audio;
    TextView note;
    TextView note2;
    TextView note3;
    Button btCancel;
    Button btStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sound_volume);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        seekBarMedia =findViewById(R.id.seekBarMedia);
        seekBarAlarm=findViewById(R.id.seekBarAlarm);
        seekBarNotif=findViewById(R.id.seekBarNotif);
        seekBarRing=findViewById(R.id.seekBarRing);

        note=findViewById(R.id.textViewSText);
        note2=findViewById(R.id.textViewSText2);
        note3=findViewById(R.id.textViewSText3);

        btCancel=findViewById(R.id.btCancel);
        btStart=findViewById(R.id.btStart);

        seekBarMedia.setOnSeekBarChangeListener(new mySeekBarMediaListener());
        seekBarAlarm.setOnSeekBarChangeListener(new mySeekBarAlarmListener());
        seekBarNotif.setOnSeekBarChangeListener(new mySeekBarNotifListener());
        seekBarRing.setOnSeekBarChangeListener(new mySeekBarRingListener());

        context = this.getApplicationContext();

        SetTimer(context);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result;
        switch( event.getKeyCode() ) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;
            default:
                result= super.dispatchKeyEvent(event);
                break;
        }
        return result;
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
            case R.id.timer_diagnostic:
                Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
                PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0,intent,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_NO_CREATE) ;
                if  (pendingintent != null)  {Toast.makeText(context, "Timer is up", Toast.LENGTH_LONG).show();}
                else {Toast.makeText(context, "Timer is down", Toast.LENGTH_LONG).show();}
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
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onStart() {
        super.onStart();

        afterStart=true;
        populateBars();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!afterStart) {
                        populateBars();
                    }
                    else {
                        afterStart=false;
                    }
                }
            }, 1000);
        }

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
//       PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);  // stopped working after API 28
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pi);
//  If you don't want to use setRepeating, you can use a chain of single timers.  Either AlarmManager.set  or  AlarmManager. setExact
//   am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60, pi);
//   am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60, pi);
//                     setExact doesn't show any difference in this app comparing to set. It just requires starting with Android 14 to add  the following permissions in Manifest
//         <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
//          <uses-permission android:name="android.permission.USE_EXACT_ALARM"/>
//  To use the chain of timers you need to uncomment SoundVolumeActivity.SetTimer in BroadcustReceiver .
//  To call SoundVolumeActivity.SetTimer, you  need to make SetTimer static
//   Also you need to replace  setRepeating in BootReceiver

        Toast.makeText(context, "STARTING...", Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        if (activeNotifications.length == 0)
            sn.sendNotification(context, NOTIFICATION_ID);
    }

    public void CancelTimer(Context context)
    {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

        Toast.makeText(context, "Timer Canceled", Toast.LENGTH_LONG).show();

        if (activeNotifications.length !=0)
            sn.cancelNotification(context,activeNotifications[0].getTag(),NOTIFICATION_ID);
    }

    public void quitApp(View view){
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    protected void populateBars() {

        int zenValue = 0;
        try {
            zenValue = Global.getInt(getContentResolver(), "zen_mode");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            note2.setTextColor(Color.RED);
            note2.setText("Allow notifications in App Info!");
        } else note2.setText("");
//            Toast.makeText(context, "no permissions", Toast.LENGTH_LONG).show();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)  && !pm.isIgnoringBatteryOptimizations(getPackageName()))   {
            note3.setTextColor(Color.RED);
            note3.setText("It is preferable to set unrestricted battery mode in App Info!. See Help");
//                Toast.makeText(context, "isIgnoringBatteryOptimizations", Toast.LENGTH_LONG).show();
        }
        else
            note3.setText("");


        if (zenValue > 0) zenMode = true;
        else zenMode = false;
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT) silenceMode = false;
        else silenceMode = true;

        seekBarRing.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_RING));
        seekBarRing.setProgress(audio.getStreamVolume(AudioManager.STREAM_RING));
        seekBarRing.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        seekBarMedia.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarMedia.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBarMedia.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        seekBarAlarm.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        seekBarAlarm.setProgress(audio.getStreamVolume(AudioManager.STREAM_ALARM));
        seekBarAlarm.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        seekBarNotif.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        seekBarNotif.setProgress(audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        seekBarNotif.setProgressTintList(ColorStateList.valueOf(Color.BLUE));

        if (zenMode) {
            seekBarRing.setEnabled(false);
            seekBarMedia.setEnabled(false);
            seekBarAlarm.setEnabled(false);
            seekBarNotif.setEnabled(false);
            note.setText("\"Do Not Disturb\" mode. Open \"Sound Settings\" and try there");
        } else if (silenceMode) {
            seekBarRing.setEnabled(false);
            seekBarNotif.setEnabled(false);
            seekBarMedia.setEnabled(true);
            seekBarAlarm.setEnabled(true);
            note.setText("Ringer is silenced. Open \"Sound Settings\" to change");
        } else {
            seekBarRing.setEnabled(true);
            seekBarMedia.setEnabled(true);
            seekBarAlarm.setEnabled(true);
            seekBarNotif.setEnabled(true);
            note.setText("");
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0,intent,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_NO_CREATE) ;
        if  (pendingintent == null)
            btCancel.setBackgroundColor(Color.GRAY);
        else
            btCancel.setBackgroundResource(android.R.drawable.btn_default);

        if (activeNotifications.length != 0)
            sn.sendNotification(context, NOTIFICATION_ID);

    }

    public void openVolumes(View vie) {  // Android API 26 Platform >
        Intent intent =new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
        startActivityForResult(intent, 0);
    }

    private class mySeekBarRingListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            boolean checkNotif = false;
            if (audio.getStreamVolume(AudioManager.STREAM_RING) == audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION)) {
                checkNotif = true;
            }     // It could be set  (the Ring value) by system

            try {
                audio.setStreamVolume(AudioManager.STREAM_RING, seekBar.getProgress(), 0);
            } catch (Exception e) {
                Toast.makeText(context, "ring: " + e, Toast.LENGTH_LONG).show();
                seekBarRing.setProgress(audio.getStreamVolume(AudioManager.STREAM_RING));
                return;
            }

            if (checkNotif)
                seekBarNotif.setProgress(audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION));

            sn.sendNotification(context, NOTIFICATION_ID);
        }


    }

    private class mySeekBarMediaListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
            } catch (Exception e) {
                Toast.makeText(context, "Media: " + e, Toast.LENGTH_LONG).show();
                seekBarMedia.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
            sn.sendNotification(context, NOTIFICATION_ID);
        }

    }

    private class mySeekBarAlarmListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                audio.setStreamVolume(AudioManager.STREAM_ALARM, seekBar.getProgress(), 0);
            } catch (Exception e) {
                Toast.makeText(context, "Alarm: " + e, Toast.LENGTH_LONG).show();
            }
            seekBarAlarm.setProgress(audio.getStreamVolume(AudioManager.STREAM_ALARM)); // workaround for weird Android behaviour and bug)
            sn.sendNotification(context, NOTIFICATION_ID);
        }

    }

    private class mySeekBarNotifListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            boolean checkRing = false;
            if (audio.getStreamVolume(AudioManager.STREAM_RING) == audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION))
                checkRing = true;

            try {
                audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, seekBar.getProgress(), 0);
            } catch (Exception e) {
                Toast.makeText(context, "notification: " + e, Toast.LENGTH_LONG).show();
                seekBarNotif.setProgress(audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                return;
            }
            if (checkRing)
                seekBarRing.setProgress(audio.getStreamVolume(AudioManager.STREAM_RING));

            sn.sendNotification(context, NOTIFICATION_ID);
        }
    }
}