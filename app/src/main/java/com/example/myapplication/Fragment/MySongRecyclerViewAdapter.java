package com.example.myapplication.Fragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.Entry.Song;
import com.example.myapplication.Fragment.SongFragment.OnListFragmentInteractionListener;
import com.example.myapplication.R;

import java.text.MessageFormat;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class MySongRecyclerViewAdapter extends RecyclerView.Adapter<MySongRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mValues;
    private final OnListFragmentInteractionListener mListener;
    private int songIndex;
    private int id;

    public MySongRecyclerViewAdapter(int i, List<Song> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        id = i;
        songIndex = i == 0 ? 0 : -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.songId.setText(MessageFormat.format("{0}", position + 1));
        holder.songTitle.setText(mValues.get(position).getTitle());
        holder.songPlaying.setText(position == songIndex ? "播放中" : "");
        holder.mView.setOnClickListener((v) -> {
            if (null != mListener) {
                Log.d(TAG, "OnItemClick: " + holder.toString());
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                if (songIndex != -1){
                    int prev = songIndex;
                    notifyItemChanged(prev);
                }
                //paly song
                mListener.onItemClick(id, position, v);
                songIndex = position;
                notifyItemChanged(songIndex);
            }
        });
    }

    public void insertItem(Song song) {
        mValues.add(song);
        notifyItemInserted(mValues.size()-1);
    }

    @Override
    public int getItemCount() {
        return mValues != null ? mValues.size() : 0;
    }

    public int getSongIndex() {
        return songIndex;
    }

    public void setSongIndex(int songIndex) {
        this.songIndex = songIndex;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView songId;
        final TextView songTitle;
        final TextView songPlaying;
        Song mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            songId = view.findViewById(R.id.song_number);
            songTitle = view.findViewById(R.id.song_title);
            songPlaying = view.findViewById(R.id.song_palying);
        }

        @Override
        public String toString() {
            return "ViewHolder{" +
                    "songId=" + songId +
                    ", songTitle=" + songTitle +
                    ", songPlaying=" + songPlaying +
                    ", mItem=" + mItem +
                    '}';
        }
    }

}
