package com.player.netease.videoplayernetease.widget.media;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by MrDong on 2016/12/15.
 */

public class ViewPlayerUiController {

    private Context context;
    private VideoView videoView;

    private String currentTime;
    private String durationTime;
    private long secondaryProgress;
    private long progress;
    private long newPosition;
    public boolean isPlayerComplet;
    private boolean isLive;
    private boolean isDragging;
    private View topView;
    private View bottomView;
    private boolean isShowTopBottomBar = false;


    private static final int MESSAGE_SEEK_NEW_POSITION = 0;
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_RESTART_PLAY = 2;
    private static final int SHOW_TOP_AND_BOTTOM_BAR = 3;
    private static final int HIDE_TOP_AND_BOTTOM_BAR = 4;
    // settable by the client

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**滑动完成，设置播放进度*/
                case MESSAGE_SEEK_NEW_POSITION:
                    if (!isLive && newPosition >= 0) {
                        videoView.seekTo((int) videoView.getDuration());
                        newPosition = -1;
                    }
                    break;
                /**滑动中，同步播放进度*/
                case MESSAGE_SHOW_PROGRESS:
                    long position = videoView.getCurrentPosition();
                    long duration = videoView.getDuration();
                    if (!videoView.isLive()) {
//                    if (!isDragging && isShowControlPanl) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (position % 1000));
                        long pos = 1000L * position / duration;
//                    Log.e("yd", "pos" + pos);
//                    currentTime = videoView.generateTime(position);
//                    progress =  pos;
                        listener.updateCurrentTime(videoView.generateTime(position));
                        listener.updateProgress(pos);
                        listener.updateBufferPercentage(videoView.getBufferPercentage() * 10);
//                    Log.e("yd","videoView.getBufferPercentage()"+videoView.getBufferPercentage());
//                    }
                    }
                    break;
                /**重新去播放*/
                case MESSAGE_RESTART_PLAY:
                    break;

                case SHOW_TOP_AND_BOTTOM_BAR:
                    topView.setVisibility(View.VISIBLE);
                    bottomView.setVisibility(View.VISIBLE);
                    isShowTopBottomBar = true;
                    this.removeMessages(HIDE_TOP_AND_BOTTOM_BAR);
                    this.sendEmptyMessageDelayed(HIDE_TOP_AND_BOTTOM_BAR, 5000);
                    break;
                case HIDE_TOP_AND_BOTTOM_BAR:
                    topView.setVisibility(View.GONE);
                    bottomView.setVisibility(View.GONE);
                    isShowTopBottomBar = false;
                    break;
            }
        }
    };

    public ViewPlayerUiController(Context context, VideoView videoView) {
        this.context = context;
        this.videoView = videoView;
        initPlayerData();

    }

    private void initPlayerData() {
        //准备播放
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                listener.progressDefult(videoView.getCurrentPosition());
                listener.currentTimeDefult(videoView.generateTime(videoView.getCurrentPosition()));
                listener.durationTimeDefult(videoView.generateTime(videoView.getDuration()));
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
            }
        });

        //播放完成
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
                isPlayerComplet = true;
                listener.updatePlayerComplet(isPlayerComplet);
                listener.updatePlayerStatus(isPlayerComplet);

            }
        });

        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                listener.setOnErrorListener(what, extra);
                return true;
            }
        });

        setShowTopAndBottomBar();
    }

    SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        /**数值的改变*/
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                /**不是用户拖动的，自动播放滑动的情况*/
                return;
            } else {
                long duration = videoView.getDuration();
                int position = (int) ((duration * progress * 1.0) / 1000);
                newPosition = position;
                listener.updateCurrentTime(videoView.generateTime(position));
//                Log.e("yd",""+videoView.generateTime(position));
            }

        }

        /**开始拖动*/
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        }

        /**停止拖动*/
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            long duration = videoView.getDuration();
            videoView.seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
            isDragging = false;
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };


    public void setPlayerStatus(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    listener.updatePlayerStatus(videoView.isPlaying());
                } else {
                    if (isPlayerComplet) {
                        isPlayerComplet = false;
                        listener.updatePlayerComplet(false);
                        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
                    }
                    listener.updatePlayerStatus(videoView.isPlaying());
                    videoView.start();
                }
            }
        });

    }



    public void toggleFullScreen() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            hideStatusUI(false);
            listener.updatePlayerFullScreen(false);
        } else {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            hideStatusUI(true);
            listener.updatePlayerFullScreen(true);
        }
    }



    public int getScreenOrientation() {
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    /**
     * 设置界面方向带隐藏actionbar
     */
    public void hideActionBar(boolean isHided) {
        if (context != null) {
            ActionBar supportActionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (supportActionBar != null) {
                if (isHided) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
    }


    /**
     * 隐藏状态栏
     */
    public void hideStatusUI(boolean isHided) {
        if (isHided) {
            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


    public void setTopAndBottomBar(View topView, View bottomView) {
        this.topView = topView;
        this.bottomView = bottomView;
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            setShowTopAndBottomBar();
            return false;
        }
    };

    private void setShowTopAndBottomBar() {
        if (isShowTopBottomBar) {

            mHandler.removeMessages(SHOW_TOP_AND_BOTTOM_BAR);
            mHandler.removeMessages(HIDE_TOP_AND_BOTTOM_BAR);
            mHandler.sendEmptyMessage(HIDE_TOP_AND_BOTTOM_BAR);

        } else {
            mHandler.removeMessages(SHOW_TOP_AND_BOTTOM_BAR);
            mHandler.removeMessages(HIDE_TOP_AND_BOTTOM_BAR);
            mHandler.sendEmptyMessage(SHOW_TOP_AND_BOTTOM_BAR);
        }
    }


    private UpdatePlayerUiListener listener;

    public void setListener(UpdatePlayerUiListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.setTouchListener(touchListener);
            listener.setSeekListener(mSeekListener);
        }
    }

    public interface UpdatePlayerUiListener {
        void currentTimeDefult(String currentTimeDefult);

        void durationTimeDefult(String durationTimeDefult);

        void updateCurrentTime(String currentTime);

        void progressDefult(int progressDefult);

        void updateProgress(long progress);

        void updateBufferPercentage(long bufferPercentage);

        void setSeekListener(SeekBar.OnSeekBarChangeListener mSeekListener);

        void updatePlayerStatus(boolean isplaying);

        void updatePlayerComplet(boolean isPlayerComplet);

        void updatePlayerFullScreen(boolean isFullScreen);

        void setTouchListener(View.OnTouchListener listener);

        void setOnErrorListener(int what, int extra);

    }

    public void setBackgroundProcesses() {
        isBackgroundUpdatePlayerStatus(context);
    }

    public void videoOnDistory() {
        videoView.releaseWithoutStop();
        videoView.release(true);
    }


    public void isBackgroundUpdatePlayerStatus(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    Log.e("yd", "处于后台"+ appProcess.processName);
                    videoView.pause();
                } else {
                    videoView.start();
//                    Log.e("yd", "处于前台"+ appProcess.processName);
                }
            }
        }
    }

    /**
     * 是否设置常亮
     */
    public void setScreenWake(boolean isWakeLock) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        if (isWakeLock) {
            wakeLock.acquire();
        } else {
            wakeLock.release();
        }
    }

    public float pixel2dip(Context context, float n) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = n / (metrics.densityDpi / 160f);
        return dp;

    }

    public int dip2pixel(Context context, float n) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
        return value;
    }
}
