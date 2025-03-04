package hcmute.edu.vn.miniproject1.controllers;

import static hcmute.edu.vn.miniproject1.utils.Constants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;
import hcmute.edu.vn.miniproject1.models.adapters.SongAdapter;
import hcmute.edu.vn.miniproject1.services.SongService;

public class ListSongActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    private RelativeLayout layoutMusicBar;
    private ImageView imgSong, imgPlayOrPause, imgClear, imgNext;
    private TextView tvTitleSong, tvSingerSong;

    private Song mSong;

    List<Song> lstSong = new ArrayList<>();

    private boolean isPlaying;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            mSong = (Song) bundle.getSerializable(OBJECT_SONG);
            isPlaying = bundle.getBoolean(ACTION_STATUS);
            int action = bundle.getInt(ACTION_SONG, 0);
            handlePlayerMusic(action);
        }
    };

    private void handlePlayerMusic(int action) {
        switch (action) {
            case ACTION_PAUSE:
            case ACTION_RESUME:
                setStatusButtonPlayOrPause();
                break;
            case ACTION_CLEAR:
                layoutMusicBar.setVisibility(View.GONE);
                break;
            case ACTION_START:
                layoutMusicBar.setVisibility(View.VISIBLE);
                displaySongInfo();
                setStatusButtonPlayOrPause();
                break;
            case ACTION_NEXT:
                tvTitleSong.setText(mSong.getTitle());
                tvSingerSong.setText(mSong.getSinger());
                imgSong.setImageResource(mSong.getImage());
                setStatusButtonPlayOrPause();
                break;
        }
    }

    public void displaySongInfo() {
        if (mSong != null) {
            tvTitleSong.setText(mSong.getTitle());
            tvSingerSong.setText(mSong.getSinger());
            imgSong.setImageResource(mSong.getImage());
        }
    }

    private void sendActionToService (int action) {
        Intent intent = new Intent(ListSongActivity.this, SongService.class);
        intent.putExtra(ACTION_SONG, action);
        if (action == ACTION_NEXT) {
            onClickNext();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OBJECT_SONG, mSong);
            intent.putExtras(bundle);
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void setStatusButtonPlayOrPause() {
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.ic_pause);
        } else {
            imgPlayOrPause.setImageResource(R.drawable.ic_play);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);

        mapping();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.custom_divider);
        decoration.setDrawable(drawable);
        recyclerView.addItemDecoration(decoration);

        songAdapter = new SongAdapter(getListSong(), this::onClickGoToPlaySong);
        recyclerView.setAdapter(songAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_SEND_DATA_TO_ACTIVITY));

        imgPlayOrPause.setOnClickListener(v -> {
            if (isPlaying) {
                sendActionToService(ACTION_PAUSE);
            } else {
                sendActionToService(ACTION_RESUME);
            }
        });

        imgClear.setOnClickListener(v -> sendActionToService(ACTION_CLEAR));

        imgNext.setOnClickListener(v -> sendActionToService(ACTION_NEXT));

    }

    private List<Song> getListSong() {
        lstSong.add(new Song("CHÚNG TA CÒN Ở ĐÓ KHÔNG?", "Orange", R.drawable.song1, R.raw.chungtaconodokhong_orange));
        lstSong.add(new Song("DÙ CHO TẬN THẾ", "Erik", R.drawable.song2, R.raw.duchotanthe_erik));
        lstSong.add(new Song("NOKIA", "VCC Left Hand", R.drawable.song3, R.raw.nokia_lefthand));
        lstSong.add(new Song("Some one you loved", "Lewis Capaldi", R.drawable.song4, R.raw.someoneyouloved_lewiscapaldi));
        lstSong.add(new Song("Window Shopper", "Hurrykng", R.drawable.song5, R.raw.windowshopper_hurrykng));
        lstSong.add(new Song("MA NƠ CANH", "Hurrykng", R.drawable.song6, R.raw.manocanh_hurrykng));
        return lstSong;
    }

    private void onClickGoToPlaySong(Song song) {
        clickStopService();
        mSong = song;
        tvTitleSong.setText(song.getTitle());
        tvSingerSong.setText(song.getSinger());
        imgSong.setImageResource(song.getImage());
        clickStartService(song);
    }

    private void clickStopService() {
        sendActionToService(ACTION_CLEAR);
        Intent intent = new Intent(this, SongService.class);
        stopService(intent);
    }


    private void clickStartService(Song song) {
        Intent intent = new Intent(this, SongService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(OBJECT_SONG, song);
        intent.putExtras(bundle);

        startForegroundService(intent);
    }

    private void onClickNext() {
        if (lstSong == null || lstSong.isEmpty()) return;

        for (int i = 0; i < lstSong.size(); i++) {
            if (lstSong.get(i).getTitle().equals(mSong.getTitle())) {
                mSong = (i == lstSong.size() - 1) ? lstSong.get(0) : lstSong.get(i + 1);
                break;
            }
        }
    }

    private void mapping() {
        layoutMusicBar = findViewById(R.id.layout_music_bar);
        imgSong = findViewById(R.id.img_song);
        imgPlayOrPause = findViewById(R.id.img_play_or_pause);
        imgClear = findViewById(R.id.img_clear);
        imgNext = findViewById(R.id.img_next);
        tvTitleSong = findViewById(R.id.tv_title_song);
        tvSingerSong = findViewById(R.id.tv_singer_song);
        imgSong = findViewById(R.id.img_song);
        recyclerView = findViewById(R.id.lst_music);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}