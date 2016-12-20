# VideoPlayerNetease
1.基于ijkplayer二次开发,完全自定义播放器UI, ffmpeg解码,支持M3U8,MOV,MP4等格式
2.支持直播,点播,和本地播放
# 使用流程:
1.添加maven库{
    compile 'tv.danmaku.ijk.media:ijkplayer-java:0.5.1'
    compile 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.5.1'
}
2.配置权限
    网络权限
    <uses-permission android:name="android.permission.INTERNET"/>
    休眠唤醒权限
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
3.ViewPlayerUiController UI控制类更具自己的需求增加相关的业务

