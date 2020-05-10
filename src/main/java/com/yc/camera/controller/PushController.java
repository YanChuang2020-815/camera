package com.yc.camera.controller;


import com.yc.camera.service.RecordCameraService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推流器实现，推本地摄像头视频到流媒体服务器以及摄像头录制视频功能实现
 * <p>
 * Created By ChengHao On 2020/5/3
 */
@Slf4j
@RestController
@RequestMapping("/camera")
public class PushController {


    /**
     * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
     *
     * @param deviceIndex -视频设备，本机默认是0
     * @param outputFile  -录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
     * @param frameRate   - 视频帧率
     */
    @PostMapping("/push/")
    public String recordCamera(@RequestParam Integer deviceIndex, @RequestParam String outputFile, @RequestParam Integer frameRate) {
        Loader.load(opencv_objdetect.class);

        RecordCameraService recordCameraService = new RecordCameraService(deviceIndex, outputFile, frameRate);
        recordCameraService.start();

        return "success";
    }


}
