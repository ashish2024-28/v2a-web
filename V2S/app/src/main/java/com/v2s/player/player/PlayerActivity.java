package com.v2s.player.player;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.v2s.player.R;
import com.v2s.player.core.ui.V2SUi;
import com.v2s.player.core.utils.ViewUtils;

/**
 * PlayerActivity is the V2S full-screen playback surface.
 * Role: provides a dark player layout with title, artwork/video, progress, transport controls and favorite-style actions.
 */
public final class PlayerActivity extends Activity {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IS_VIDEO = "is_video";

    private VideoView videoView;
    private MediaPlayer audioPlayer;
    private SeekBar audioSeek;
    private final Handler handler = new Handler();

    /** Reads the selected URI and builds either the video or audio version of the reference-style player. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(V2SUi.BG); getWindow().setNavigationBarColor(V2SUi.BG);
        Uri uri=getIntent().getData(); String title=getIntent().getStringExtra(EXTRA_TITLE); boolean isVideo=getIntent().getBooleanExtra(EXTRA_IS_VIDEO,true);
        if(uri==null){finish();return;}
        if(isVideo) setContentView(buildVideoPlayer(uri,title)); else setContentView(buildAudioPlayer(uri,title));
    }

    /** Builds the video player with top title bar, VideoView, progress area and custom quick actions. */
    private View buildVideoPlayer(Uri uri,String title){
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(V2SUi.BG);
        LinearLayout column=V2SUi.column(this);root.addView(column,new FrameLayout.LayoutParams(-1,-1));
        column.addView(buildTopBar(title),new LinearLayout.LayoutParams(-1,58));
        FrameLayout videoHost=new FrameLayout(this);videoView=new VideoView(this);videoView.setVideoURI(uri);videoView.setMediaController(new MediaController(this));videoView.setOnPreparedListener(mp->{mp.start();});videoHost.addView(videoView,new FrameLayout.LayoutParams(-1,-1));column.addView(videoHost,new LinearLayout.LayoutParams(-1,0,1));
        column.addView(buildVideoControls(),new LinearLayout.LayoutParams(-1,178));
        return root;
    }

    /** Builds the audio player with album-art panel, track information and a functional MediaPlayer progress bar. */
    private View buildAudioPlayer(Uri uri,String title){
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(V2SUi.BG);LinearLayout column=V2SUi.column(this);column.setGravity(Gravity.CENTER_HORIZONTAL);column.setPadding(14,8,14,12);root.addView(column,new FrameLayout.LayoutParams(-1,-1));
        column.addView(buildTopBar(title),new LinearLayout.LayoutParams(-1,58));
        ImageView cover=new ImageView(this);cover.setImageResource(R.drawable.cover_perfect);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);column.addView(cover,new LinearLayout.LayoutParams(-1,0,1));
        column.addView(V2SUi.heading(this,title==null?"V2S Song":title,20),new LinearLayout.LayoutParams(-1,38));column.addView(V2SUi.text(this,"V2S Demo • Local Audio",12,V2SUi.MUTED),new LinearLayout.LayoutParams(-1,28));
        audioSeek=new SeekBar(this);audioSeek.setMax(1000);audioSeek.setProgress(0);column.addView(audioSeek,new LinearLayout.LayoutParams(-1,42));
        column.addView(buildTransportControls(),new LinearLayout.LayoutParams(-1,70));
        column.addView(buildVideoControls(),new LinearLayout.LayoutParams(-1,112));
        prepareAudio(uri);
        return root;
    }

    /** Builds the common player header with back, title and overflow actions. */
    private View buildTopBar(String title){LinearLayout bar=V2SUi.row(this);bar.setPadding(4,0,4,0);TextView back=V2SUi.iconButton(this,"‹","Back");back.setOnClickListener(v->finish());bar.addView(back,new LinearLayout.LayoutParams(44,44));LinearLayout info=V2SUi.column(this);info.setPadding(8,0,8,0);info.addView(V2SUi.heading(this,title==null?"V2S Player":title,15));info.addView(V2SUi.text(this,"V2S • Player",10,V2SUi.MUTED));bar.addView(info,new LinearLayout.LayoutParams(0,58,1));TextView cast=V2SUi.iconButton(this,"◉","Player options");bar.addView(cast,new LinearLayout.LayoutParams(44,44));TextView more=V2SUi.iconButton(this,"⋮","More player options");bar.addView(more,new LinearLayout.LayoutParams(44,44));return bar;}

    /** Builds video player actions for rotate, lock, subtitle, PiP-style and playback options. */
    private View buildVideoControls(){LinearLayout controls=V2SUi.column(this);controls.setPadding(8,4,8,8);LinearLayout row=V2SUi.row(this);String[] actions={"↺","◉","CC","▣","☾","⌁"};String[] names={"Rotate","Lock","Subtitles","Mini player","Night mode","More"};for(int i=0;i<actions.length;i++){TextView b=V2SUi.iconButton(this,actions[i],names[i]);if(i==5)b.setOnClickListener(v->finish());row.addView(b,new LinearLayout.LayoutParams(0,46,1));}controls.addView(row);controls.addView(V2SUi.text(this,"All Format Support   •   Smart Playback   •   Gesture Control",11,V2SUi.MUTED));return controls;}

    /** Builds audio transport controls for previous, play/pause and next behavior. */
    private View buildTransportControls(){LinearLayout row=V2SUi.row(this);row.setGravity(Gravity.CENTER);TextView previous=V2SUi.iconButton(this,"|‹","Previous");TextView play=V2SUi.orangeButton(this,"▶","Play / Pause");TextView next=V2SUi.iconButton(this,"›|","Next");row.addView(previous,new LinearLayout.LayoutParams(52,52));row.addView(play,new LinearLayout.LayoutParams(74,52));row.addView(next,new LinearLayout.LayoutParams(52,52));play.setOnClickListener(v->{if(audioPlayer==null)return;if(audioPlayer.isPlaying()){audioPlayer.pause();play.setText("▶");}else{audioPlayer.start();play.setText("Ⅱ");}});return row;}

    /** Prepares MediaPlayer audio and starts the progress synchronization loop. */
    private void prepareAudio(Uri uri){try{audioPlayer=new MediaPlayer();audioPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());audioPlayer.setDataSource(this,uri);audioPlayer.setOnPreparedListener(mp->{audioSeek.setMax(Math.max(1,mp.getDuration()));mp.start();syncSeek();});audioPlayer.setOnErrorListener((mp,what,extra)->true);audioPlayer.prepareAsync();}catch(Exception ignored){}}

    /** Synchronizes the audio seek bar with the currently playing MediaPlayer. */
    private void syncSeek(){if(audioPlayer==null||audioSeek==null)return;if(audioPlayer.isPlaying())audioSeek.setProgress(audioPlayer.getCurrentPosition());handler.postDelayed(this::syncSeek,500);}

    /** Releases playback resources when the player leaves the foreground. */
    @Override protected void onStop(){super.onStop();handler.removeCallbacksAndMessages(null);if(videoView!=null)videoView.stopPlayback();if(audioPlayer!=null){audioPlayer.release();audioPlayer=null;}}
}
