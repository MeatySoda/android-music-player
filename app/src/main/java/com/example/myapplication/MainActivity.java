package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.Entry.Song;
import com.example.myapplication.Fragment.MySongRecyclerViewAdapter;
import com.example.myapplication.Fragment.SongFragment;
import com.example.myapplication.Util.MyFileVisitor;
import com.example.myapplication.Util.MyLight;
import com.example.myapplication.Util.MyTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_APPEND;
import static android.view.LayoutInflater.from;

public class MainActivity extends AppCompatActivity implements SongFragment.OnListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ArrayList<Song>> lists = new HashMap<>(2);
    private TextView mTextMessage;
    private RecyclerView recyclerView;
    private Set<String> set=new HashSet<>();
    private TextView tNow, tTotal;
    private Button bNext, bPrev, bPlay,bStar;
    private Button playList, starList;
    private SeekBar seekBar;
    private Timer timer;
    private PopupWindow popupWindow;
    private MusicService musicService;
    private SongFragment song, star;
    private ArrayList<MySongRecyclerViewAdapter> adapters = new ArrayList<>(2);
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            if (musicService != null) {
                musicService.setHandler(new MainHandler());
                musicService.initPlayer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private MainHandler mHandler = new MainHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playList = findViewById(R.id.playlist);
        starList = findViewById(R.id.starlist);
        widgetInit();
        lists.put(0, new ArrayList<>());
        lists.put(1, new ArrayList<>());
        songListInit();

        Intent intent = new Intent(this, MusicService.class);
        intent.putParcelableArrayListExtra("songList", lists.get(0));
        intent.putParcelableArrayListExtra("starList", lists.get(1));
        startService(intent);
        intent = new Intent(this, MusicService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        System.out.println("startAndbindService");
    }

    private void widgetInit() {
        View view = from(this).inflate(R.layout.player_pop, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setOnDismissListener(() -> MyLight.lighton(this)); // 设置背景图片， 必须设置，不然动画没作用
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popupWindow_style);
        tNow = view.findViewById(R.id.nowProgress);
        tTotal = view.findViewById(R.id.totalProgress);
        mTextMessage = view.findViewById(R.id.song_play_title);
        seekBar = view.findViewById(R.id.progress);
        bNext = view.findViewById(R.id.nextSong);
        bPlay = view.findViewById(R.id.play);
        bPrev = view.findViewById(R.id.prevSong);
        bStar =view.findViewById(R.id.bStar);

        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                    Log.d(TAG, "onProgressChanged: ");
                    tNow.setText(MyTime.formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onStart() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            super.onStart();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            super.onStart();
        }
    }

    public void play(View view) {
        if (musicService == null) {
            Log.e(TAG, "play: ", new Exception("ServiceNotFound"));
            return;
        }
        musicService.play();
    }

    public void starSong(View view){


        Map.Entry entry = musicService.getIdAndIndex();
        if ((int)entry.getKey() == 0) {
            Song newSong= lists.get(0).get((Integer) entry.getValue());
            SharedPreferences preferences = getSharedPreferences("starsFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            HashSet<String> songHashSet = new HashSet<>(set);
            songHashSet.add(lists.get(entry.getKey()).get((Integer) entry.getValue()).getPath().toString());
            editor.putStringSet("starList",songHashSet);
            editor.commit();

            adapters.get(1).insertItem(newSong);
            musicService.addSong(1,newSong);
        }
        else {

        }

    }

    public void songListInit() {
        SharedPreferences data = getSharedPreferences("root", MODE_PRIVATE);
        Path path = Paths.get(data.getString("LastPath", "/storage/emulated/0/Music/"));
        data = getSharedPreferences("starsFile",MODE_PRIVATE);
        set = data.getStringSet("starList", new HashSet<>());
        set.forEach(s -> lists.get(1).add(new Song(Paths.get(s))));
        try {
            Files.walkFileTree(path, new MyFileVisitor(lists.get(0)));
            song = SongFragment.newInstance(0, lists.get(0));
            star = SongFragment.newInstance(1, lists.get(1));
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, song).commit();
        } catch (IOException e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    public void showSongList(View view) {
        if (song.isAdded() && song.isHidden())
            getSupportFragmentManager().beginTransaction().hide(star).show(song).commit();
    }

    public void showStarList(View view) {
        if (star.isAdded())
            getSupportFragmentManager().beginTransaction().hide(song).show(star).commit();
        else
            getSupportFragmentManager().beginTransaction().hide(song).add(R.id.fragment, star).commit();
    }

    public void setSongNext(View view) {
        Map.Entry entry = musicService.getIdAndIndex();
        int prevId = (int) entry.getKey();
        int prevValue= (int) entry.getValue();
        musicService.nextSong();
        entry = musicService.getIdAndIndex();
        adapters.get(prevId).setSongIndex((Integer) entry.getValue());
        adapters.get(prevId).notifyItemChanged(prevValue);
        adapters.get((Integer) entry.getKey()).notifyItemChanged((Integer) entry.getValue());

    }

    public void setSongPrev(View view) {
        Map.Entry entry = musicService.getIdAndIndex();
        int prevId = (int) entry.getKey();
        int prevValue= (int) entry.getValue();
        musicService.prevSong();
        entry = musicService.getIdAndIndex();
        adapters.get(prevId).setSongIndex((Integer) entry.getValue());
        adapters.get(prevId).notifyItemChanged(prevValue);
        adapters.get((Integer) entry.getKey()).notifyItemChanged((Integer) entry.getValue());

    }

    public void showPop(View view) {
        MyLight.lightoff(this);
        popupWindow.showAtLocation(this.findViewById(R.id.pop), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
    }

    @Override
    public void onItemClick(int i, int position, View view) {
        Log.d(TAG, "onItemClick: from activity");
        Map.Entry e = musicService.getIdAndIndex();
        if ((int) e.getKey() != i) {
            try {
                adapters.get((Integer) e.getKey()).setSongIndex(-1);
                adapters.get((Integer) e.getKey()).notifyItemChanged((Integer) e.getValue());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

        }
        musicService.play(i, position);
    }

    @Override
    public void setAdapters(int i, MySongRecyclerViewAdapter adapter) {
        adapters.add(adapter);
    }

    @Override
    public void setRecyclerView(RecyclerView view) {
        recyclerView = view;
    }

    @SuppressLint("HandlerLeak")
    public class MainHandler extends Handler {

        static final int SEEKBAR_UPDATE = 1;
        static final int SEEKBAR_RESET = 4;
        static final int SONG_NEXT = 2;
        static final int SONG_PREV = 3;
        static final int SONG_LOOP_SINGLE = 5;
        static final int SONG_LOOP_ALL = 6;
        static final int SONG_PAUSE = 7;
        static final int SONG_RESUME = 8;

        MainHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            int max;
            Map.Entry e;
            switch (msg.what) {
                case SEEKBAR_RESET:
                    max = musicService.getDuration();
                    seekBar.setMax(max);
                    seekBar.setProgress(0);
                    tTotal.setText(MyTime.formatTime(max));
                    e = musicService.getIdAndIndex();
                    mTextMessage.setText(lists.get(e.getKey()).get((Integer) e.getValue()).getTitle());
                    if ((int)e.getKey() == 1) {
                        bStar.setText("💖");
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    seekBarTask();
                    bPlay.setText("||");
                    break;
                case SEEKBAR_UPDATE:
                    seekBar.setProgress(msg.arg1);
                    tNow.setText(MyTime.formatTime(msg.arg1));
                    break;
                case SONG_NEXT:
                    max = musicService.getDuration();
                    seekBar.setMax(max);
                    seekBar.setProgress(0);
                    tTotal.setText(MyTime.formatTime(max));
                    e = musicService.getIdAndIndex();
                    seekBarTask();
                    mTextMessage.setText(lists.get(e.getKey()).get((Integer) e.getValue()).getTitle());
                    break;
                case SONG_PREV:
                    max = musicService.getDuration();
                    seekBar.setMax(max);
                    seekBar.setProgress(0);
                    tTotal.setText(MyTime.formatTime(max));
                    e = musicService.getIdAndIndex();
                    seekBarTask();
                    mTextMessage.setText(lists.get(e.getKey()).get((Integer) e.getValue()).getTitle());
                    break;
                case SONG_PAUSE:
                    timer.cancel();
                    bPlay.setText("▶");
                    break;
                case SONG_RESUME:
                    seekBarTask();
                    bPlay.setText("||");
                    break;
                case SONG_LOOP_SINGLE:
                    break;
                case SONG_LOOP_ALL:
                    break;
                default:
                    break;
            }
        }

        public int getProgress(int what) {
            int progress = musicService.getProgress();
            Log.d(TAG, "getProgress: " + what + " " + progress);
            return progress;
        }

        public void seekBarTask() {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = SEEKBAR_UPDATE;
                    message.arg1 = musicService.getProgress();
                    mHandler.sendMessage(message);
                }
            }, 0, 500);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }
}
