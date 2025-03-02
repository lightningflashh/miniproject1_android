package hcmute.edu.vn.miniproject1.controllers;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;
import hcmute.edu.vn.miniproject1.models.adapters.SongAdapter;

public class ListSongActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);

        recyclerView = findViewById(R.id.lst_music);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.custom_divider);
        decoration.setDrawable(drawable);
        recyclerView.addItemDecoration(decoration);

        songAdapter = new SongAdapter(getListSong(), this::onClickGoToPlaySong);
        recyclerView.setAdapter(songAdapter);

    }

    private List<Song> getListSong() {
        List<Song> lstSong = new ArrayList<>();
        lstSong.add(new Song("CHÚNG TA CÒN Ở ĐÓ KHÔNG?", "Orange", R.drawable.song1, R.raw.chungtaconodokhong_orange));
        lstSong.add(new Song("DÙ CHO TẬN THẾ", "Erik", R.drawable.song2, R.raw.duchotanthe_erik));
        lstSong.add(new Song("NOKIA", "VCC Left Hand", R.drawable.song3, R.raw.nokia_lefthand));
        lstSong.add(new Song("Some one you loved", "Lewis Capaldi", R.drawable.song4, R.raw.someoneyouloved_lewiscapaldi));
        lstSong.add(new Song("Window Shopper", "Hurrykng", R.drawable.song5, R.raw.windowshopper_hurrykng));
        lstSong.add(new Song("MA NƠ CANH", "Hurrykng", R.drawable.song6, R.raw.manocanh_hurrykng));
        return lstSong;
    }

    private void onClickGoToPlaySong(Song song) {
        Intent intent = new Intent(this, ItemSongActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song", song);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}