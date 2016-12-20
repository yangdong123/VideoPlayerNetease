package com.player.netease.videoplayernetease;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.player.netease.videoplayernetease.widget.media.VideoView;
import com.player.netease.videoplayernetease.widget.media.ViewPlayerUiController;

public class MainActivity extends AppCompatActivity {
    private String url1 = "rtmp://203.207.99.19:1935/live/CCTV5";
    private String url2 = "http://zv.3gv.ifeng.com/live/zhongwen800k.m3u8";
    private String url3 = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
    private String url4 = "rtmp://114.112.58.107:1935/liveorigin/inyuetai";
    private String url5 = "http://bobolive.nosdn.127.net/prpr_1481626691354_31069376.mp4";
    private String url6 = "http://bobolive.nosdn.127.net/prpr_1481024215812_36903672.mp4";


    private VideoView videoView;
    private SeekBar seekbar;
    private TextView timeCurrent;
    private TextView timeDuration;
    private ImageButton puse;
    private ImageButton fullScreen;
    private boolean playComplet;
    private RelativeLayout videoviewUpBar;
    private RelativeLayout videoviewBottomBar;
    private RelativeLayout rootView;
    private ViewPlayerUiController controller;
    private RelativeLayout videoPlayerRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPlayer();


    }

    private void initView() {
        rootView = (RelativeLayout) findViewById(R.id.activity_main);
        videoView = (VideoView) findViewById(R.id.video_view);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        timeCurrent = (TextView) findViewById(R.id.time_current);
        timeDuration = (TextView) findViewById(R.id.time_duration);
        videoviewUpBar = (RelativeLayout) findViewById(R.id.videoview_up_bar);
        videoviewBottomBar = (RelativeLayout) findViewById(R.id.videoview_bottom_bar);
        videoPlayerRootView = (RelativeLayout) findViewById(R.id.video_player_root_view);
        videoView.setVideoPath(url6);
        videoView.start();

        puse = (ImageButton) findViewById(R.id.play_pause);
        fullScreen = (ImageButton) findViewById(R.id.zoom_in_out);

    }


    private void initPlayer() {
        controller = new ViewPlayerUiController(this,videoView);
       controller.setListener(new ViewPlayerUiController.UpdatePlayerUiListener() {
           @Override
           public void currentTimeDefult(String currentTimeDefult) {

               if(videoView.isLive()) {
                   timeCurrent.setText("直播中...");
                   timeDuration.setVisibility(View.INVISIBLE);
               }else {
                   timeCurrent.setText(currentTimeDefult);
               }
           }

           //设置默认总时长
           @Override
           public void durationTimeDefult(String durationTimeDefult) {
               timeDuration.setText(durationTimeDefult);
           }

           //时间更新回调
           @Override
           public void updateCurrentTime(String currentTime) {
               timeCurrent.setText(currentTime);
           }

           //设置默认进度值回调
           @Override
           public void progressDefult(int progressDefult) {
               seekbar.setProgress(progressDefult);
           }

           //进度回调显示监听
           @Override
           public void updateProgress(long progress) {
               seekbar.setProgress((int) progress);

           }

           //缓冲进度监听
           @Override
           public void updateBufferPercentage(long bufferPercentage) {
               seekbar.setSecondaryProgress((int) bufferPercentage);
           }

           //进度回调监听
           @Override
           public void setSeekListener(SeekBar.OnSeekBarChangeListener mSeekListener) {
               seekbar.setOnSeekBarChangeListener(mSeekListener);
           }

           //改变播放状态
           @Override
           public void updatePlayerStatus(boolean isplaying) {
               updatePlayIcon(isplaying);
               if(playComplet && !isplaying) {
                       videoView.seekTo(0);
                       seekbar.setProgress(0);
                       timeCurrent.setText(videoView.generateTime(0));
               }
           }

           //播放完成
           @Override
           public void updatePlayerComplet(boolean isPlayerComplet) {

               playComplet = isPlayerComplet;
               updatePlayIcon(isPlayerComplet);
           }

           //是否全屏回调
           @Override
           public void updatePlayerFullScreen(boolean isFullScreen) {
               updateFullScreenIcon(isFullScreen);
           }

           //触摸事件监听
           @Override
           public void setTouchListener(View.OnTouchListener listener) {
               rootView.setOnTouchListener(listener);
           }

           //播放出错回调
           @Override
           public void setOnErrorListener(int what, int extra) {
               Toast.makeText(MainActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
           }
       });
        //是否设置常亮
        controller.setScreenWake(true);

        controller.setPlayerStatus(puse);
        controller.hideActionBar(true);

        if(videoView.isLive()) {
            timeCurrent.setText("直播中...");
            timeDuration.setVisibility(View.INVISIBLE);
        }
        seekbar.setEnabled(videoView.isLive() ? false : true);
        controller.setTopAndBottomBar(videoviewUpBar,videoviewBottomBar);

        fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.toggleFullScreen();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        controller.setBackgroundProcesses();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //后台唤醒播放
        controller.setBackgroundProcesses();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.videoOnDistory();
    }

    private void updatePlayIcon(boolean isPlaying ) {
        if(isPlaying) {
            puse.setBackgroundResource(R.drawable.icon_mediacontroller_play);

        }else {
            puse.setBackgroundResource(R.drawable.icon_mediacontroller_pause);
        }
    }

    private void updateFullScreenIcon(boolean isFullScreen ) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoPlayerRootView.getLayoutParams();
        if(isFullScreen) {
            fullScreen.setBackgroundResource(R.drawable.video_player_zoom_in);
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }else {
            fullScreen.setBackgroundResource(R.drawable.video_player_zoom_out);
            params.height = controller.dip2pixel(this,220);
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
    }


}
