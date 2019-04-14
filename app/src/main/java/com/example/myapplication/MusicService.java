package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Entry.Song;
import com.example.myapplication.Util.MyTime;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.myapplication.MainActivity.MainHandler.SEEKBAR_RESET;
import static com.example.myapplication.MainActivity.MainHandler.SONG_NEXT;
import static com.example.myapplication.MainActivity.MainHandler.SONG_PAUSE;
import static com.example.myapplication.MainActivity.MainHandler.SONG_PREV;
import static com.example.myapplication.MainActivity.MainHandler.SONG_RESUME;

public class MusicService extends Service {

    private static final String TAG = MusicService.class.getSimpleName();

    private MediaPlayer player;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ArrayList<Song>> lists = new HashMap<>(2);
    private int index = 0;
    private int sId = 0;
    private MainActivity.MainHandler handler;


    public MusicService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: MusicService");
        lists.put(0,new ArrayList<>());
        lists.put(1,new ArrayList<>());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        lists.put(0, intent.getParcelableArrayListExtra("songList"));
        lists.put(1, intent.getParcelableArrayListExtra("starList"));
        return super.onStartCommand(intent, flags, startId);
    }

    public void nextSong() {
        if (player.isPlaying())
            player.pause();
        index = (index + 1) % lists.get(sId).size();
        play(sId,index);
        Message msg = Message.obtain();
        msg.what = SONG_NEXT;
        msg.arg1 = index;
        handler.sendMessage(msg);
    }

    public void prevSong() {
        if (player.isPlaying())
            player.pause();
        index--;
        if (index == -1) {
            index = lists.get(sId).size() - 1;
        }
        play(sId,index);
        Message msg = Message.obtain();
        msg.what = SONG_PREV;
        msg.arg1 = index;
        handler.sendMessage(msg);
    }

    //指定歌曲播放
    public void play(int id, int i) {
        player.pause();
        player.reset();
        if (i < 0 | id < 0 | id > 1) {
            Log.e(TAG, "play: i < 0 or sId is not in {1,0}", new IndexOutOfBoundsException());
            return;
        }
        if (i > Objects.requireNonNull(lists.get(id)).size() -1)
            return;
        Log.d(TAG, "play: " + lists.get(id).get(i).toString());
        String song = lists.get(id).get(i).getPath().toString();
        try {
            player.setDataSource(song);
            player.prepareAsync();
            sId = id;
//            player.setOnCompletionListener(player -> nextSong());
            Message msg = Message.obtain();
            msg.arg1 = i;
            msg.what = SEEKBAR_RESET;
            handler.sendMessage(msg);
            index = i;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), lists.get(id).get(i).getTitle() + "文件不可用", Toast.LENGTH_SHORT).show();
            lists.get(id).remove(i);
            play(id,i % lists.get(id).size());
            Log.e(TAG, "play: ", e);
        }
    }


    //播放暂停
    public void play() {
        if (handler == null) {
            Log.e(TAG, "play: service", new Exception("HandlerNotFound"));
            return;
        }
        if (player.isPlaying()) {
            player.pause();
            handler.sendEmptyMessage(SONG_PAUSE);
            Log.d(TAG, "play: pause");
            return;
        }
        player.start();
        handler.sendEmptyMessage(SONG_RESUME);
        Log.d(TAG, "play: resume");
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
        Log.d(TAG, "seekTo: " + MyTime.formatTime(progress));
    }

    //收藏
    public void starSong(Song song) {
        if (song == null)
            return;
        lists.get(1).add(song);
        Log.d(TAG, "starSong: ");
    }

    public int getDuration() {
        Log.d(TAG, "getDuration: " + MyTime.formatTime(player.getDuration()));
        return player.getDuration();
    }

    public int getProgress() {
        return player.getCurrentPosition();
    }

    public int getDataCount(int id) {
        Log.d(TAG, "getDataCount: " + lists.get(id).size());
        return lists.get(id).size();
    }

    public void addSong(int id, Song newSong) {
        lists.get(id).add(newSong);
    }

    public void setHandler(MainActivity.MainHandler handler) {
        this.handler = handler;
    }

    //初始化player
    public void initPlayer(){
        try {
            player = new MediaPlayer();
            player.setLooping(true);
            player.setOnPreparedListener(player -> {
                Message msg = Message.obtain();
                msg.what = SEEKBAR_RESET;
                msg.arg1 = index;
                player.start();
                handler.sendMessage(msg);
            });
            player.setOnErrorListener((MediaPlayer mp, int what, int extra) -> {
                return false;
            });
            if (lists.get(0).size() > 0) {
                player.setDataSource(lists.get(0).get(0).getPath().toString());
                player.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    class LocalBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }

    public Map.Entry getIdAndIndex() {
        Map.Entry e = new Map.Entry() {
            @Override
            public Object getKey() {
                return sId;
            }

            @Override
            public Object getValue() {
                return index;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
        return e;
    }

    @Override
    public void onDestroy() {
        player.reset();
        player.release();
        super.onDestroy();
    }
}
