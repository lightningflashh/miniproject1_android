package hcmute.edu.vn.miniproject1.services;

import static hcmute.edu.vn.miniproject1.utils.Constants.*;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import android.os.Handler;


import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.activities.ListSongActivity;
import hcmute.edu.vn.miniproject1.models.Song;

public class SongService extends Service {
    private MediaPlayer mediaPlayer;
    private Song mSong;
    private boolean isPlaying;

    private Handler handler = new Handler();

    int currentPosition = 0;
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {  // Chỉ gửi broadcast nếu đang phát nhạc
                Intent intent = new Intent(ACTION_SEND_DATA_TO_ACTIVITY);
                intent.putExtra("current_position", mediaPlayer.getCurrentPosition());
                intent.putExtra("total_duration", mediaPlayer.getDuration());
                intent.putExtra(ACTION_STATUS, isPlaying); // Gửi trạng thái phát nhạc
                LocalBroadcastManager.getInstance(SongService.this).sendBroadcast(intent);
                handler.postDelayed(this, 1000); // Lặp lại sau 1 giây
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Notification", "Service onStartCommand");

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Song song = (Song) bundle.getSerializable(OBJECT_SONG);
                boolean status = bundle.getBoolean("ACTION_STATUS", isPlaying);

                if (song != null) {
                    mSong = song;
                    isPlaying = status;
                    startMusic(song);
                    sendNotification(song);
                }
            }

            int actionMusic = intent.getIntExtra(ACTION_SONG, 0);
            if (actionMusic == ACTION_SEEK_TO) {
                int seekPosition = intent.getIntExtra("seek_position", 0);
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekPosition);
                }
            } else {
                handleActionMusic(actionMusic);
            }
        } else {
            Log.e("MyService", "Intent nhận vào bị null!");
        }

        handler.postDelayed(updateSeekBarRunnable, 1000); // Luôn cập nhật SeekBar
        return START_STICKY;
    }

    private void handleActionMusic(int action) {
        if (action == ACTION_PAUSE) {
            pauseMusic();
        } else if (action == ACTION_RESUME) {
            resumeMusic();
        } else if (action == ACTION_CLEAR) {
            stopSelf();
            sendToListSongActivity(ACTION_CLEAR, 0);
        } else if (action == ACTION_NEXT) {
            nextSong();
        } else if (action == ACTION_PREV) {
            prevSong();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPlaying = false;

            sendNotification(mSong);
            sendToListSongActivity(ACTION_PAUSE, currentPosition);
        }
    }

    private void resumeMusic() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
            isPlaying = true;

            sendNotification(mSong);
            sendToListSongActivity(ACTION_RESUME, currentPosition);
        }
    }

    private void startMusic(Song song) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), song.getResource());
        }
        mediaPlayer.start();
        isPlaying = true;
        sendToListSongActivity(ACTION_START, mediaPlayer.getCurrentPosition());
    }



    private void sendNotification(Song song) {
        Intent intent = new Intent(this, ListSongActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), song.getImage());

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteViews.setTextViewText(R.id.tv_title_song, song.getTitle());
        remoteViews.setTextViewText(R.id.tv_single_song, song.getSinger());
        remoteViews.setImageViewBitmap(R.id.img_song, bitmap);

        int playPauseIcon = isPlaying ? R.drawable.ic_pause_primary : R.drawable.ic_play_primary;
        remoteViews.setImageViewResource(R.id.img_play_or_pause, playPauseIcon);
        remoteViews.setOnClickPendingIntent(R.id.img_play_or_pause, getPendingIntent(this, isPlaying ? ACTION_PAUSE : ACTION_RESUME));

        remoteViews.setOnClickPendingIntent(R.id.img_clear, getPendingIntent(this, ACTION_CLEAR));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pending)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .build();

        Log.e("Notification", "Service sendNotification");
        startForeground(1, notification);
    }


    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, SongReceiver.class);
        intent.putExtra(ACTION_SONG, action);

        return PendingIntent.getBroadcast(context.getApplicationContext(),
                action, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void sendToListSongActivity(int action, int currentPosition) {
        Intent intent = new Intent(ACTION_SEND_DATA_TO_ACTIVITY);
        intent.putExtra(ACTION_SONG, action);
        intent.putExtra("current_position", currentPosition);

        if (mediaPlayer != null) {
            intent.putExtra("total_duration", mediaPlayer.getDuration());
        } else {
            intent.putExtra("total_duration", 0);
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(OBJECT_SONG, mSong);
        bundle.putBoolean(ACTION_STATUS, isPlaying);
        bundle.putInt(ACTION_NAME, 0);

        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void nextSong() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        startMusic(mSong);
        sendNotification(mSong);
        sendToListSongActivity(ACTION_NEXT, 0);
    }

    private void prevSong() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        startMusic(mSong);
        sendNotification(mSong);
        sendToListSongActivity(ACTION_PREV, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.e("Notification", "Service onDestroy");
    }
}
