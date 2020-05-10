package com.yc.camera.service;

import com.yc.camera.util.MarkText4J;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created By ChengHao On 2020/5/10
 */
@Slf4j
@Data
public class RecordCameraService implements Runnable {

    private static boolean ISOPEN = true;

    private Integer deviceIndex;
    private String outputFile;
    private Integer frameRate;
    private Thread t;

    public RecordCameraService(Integer deviceIndex, String outputFile, Integer frameRate) {
        this.deviceIndex = deviceIndex;
        this.outputFile = outputFile;
        this.frameRate = frameRate;
    }

    @Override
    public void run() {
        try {
            recordCamera();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        log.info("设备 {} 开启",deviceIndex);
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }


    public void recordCamera() throws FrameGrabber.Exception, FrameRecorder.Exception {
        Loader.load(opencv_objdetect.class);

        //本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码
        FrameGrabber grabber = null;
        grabber = FrameGrabber.createDefault(deviceIndex);

        //开启抓取器,获取摄像头数据
        try {
            grabber.start();
        } catch (Exception e) {
            try {
                grabber.restart();  //一次重启尝试
            } catch (Exception e2) {
                log.info("grabber开启失败");
            }
        }

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器

        Frame grabframe = null;
        grabframe = grabber.grab();

        //抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
        opencv_core.IplImage grabbedImage = converter.convert(grabframe);

        int width = grabbedImage.width();
        int height = grabbedImage.height();

        //获取BufferedImage可以给图像帧添加水印
        Java2DFrameConverter javaconverter = new Java2DFrameConverter();

        //添加水印
        Font font = new Font("宋体", Font.PLAIN, 14);
        BufferedImage buffImgOutput = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        MarkText4J markText4J = new MarkText4J();

        FrameRecorder recorder = null;
        try {
            recorder = FrameRecorder.createDefault(outputFile, width, height);
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
        recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式
        recorder.setFrameRate(frameRate);

        //开启录制器
        try {
            recorder.start();
        } catch (Exception e) {
            log.info("recorder开启失败");
            System.out.println(recorder);
                if (recorder != null) {  //尝试重启录制器
                    recorder.stop();
                    recorder.start();
                }
        }

        long startTime = 0;
        long videoTS = 0;

        //直播屏幕，开启影响推流速度
//        CanvasFrame canvasFrame = new CanvasFrame("本机直播窗口", CanvasFrame.getDefaultGamma() / grabber.getGamma());
//        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        canvasFrame.setAlwaysOnTop(true);

        while (ISOPEN && (grabframe = grabber.grab()) != null) {


            //转换为BufferedImage
            BufferedImage buffImgInput = javaconverter.convert(grabframe);
            //添加水印
            markText4J.mark(buffImgOutput, buffImgInput, "水印效果测试", font, Color.ORANGE, 0, 14);
            //TODO 图像识别

            //转换为frame
            Frame buffFrame = javaconverter.convert(buffImgOutput);
            //添加到本机直播窗口
//            canvasFrame.showImage(buffImgOutput);


            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTS);
            try {
                recorder.record(buffFrame);
                Thread.sleep(40);  // 40毫秒/帧
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //关闭直播窗口
//        canvasFrame.dispose();

        try {
            recorder.stop();
            recorder.release();
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
