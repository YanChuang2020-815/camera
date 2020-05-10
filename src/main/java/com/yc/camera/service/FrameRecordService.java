package com.yc.camera.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import javax.swing.*;

/**
 * Created By ChengHao On 2020/5/10
 */
@Data
@Slf4j
public class FrameRecordService implements Runnable{

    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private Boolean status;
    private Thread t;

    public FrameRecordService(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status) {
        this.grabber = grabber;
        this.recorder = recorder;
        this.status = status;
    }

    @Override
    public void run() {
        try {
            recordByFrame(grabber,recorder,status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        log.info("直播开始");
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception {
        try {
            grabber.start();
            recorder.start();
            //直播播放窗口
            CanvasFrame canvasFrame = new CanvasFrame("直播中");
            canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            canvasFrame.setAlwaysOnTop(true);
            Frame frame = null;
            while (status&& (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
                canvasFrame.showImage(frame);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }
}
