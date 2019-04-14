package com.example.myapplication.Entry;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Song implements Parcelable {
    @NonNull
    private String title;
    @NonNull
    private Path path;

    public Song(@NonNull String title, @NonNull Path path) {
        this.title = title;
        this.path = path;
    }

    public Song(Path path){
        String[] strings = path.toString().split("/");
        this.title = strings[strings.length-1];
        this.path = path;
    }
    protected Song(Parcel in) {
        title = in.readString();
        path = Paths.get(in.readString());
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            Song song = new Song(in.readString(), Paths.get(in.readString()));
            return song;
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(@NonNull Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", path=" + path +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path.toString());
    }
}

