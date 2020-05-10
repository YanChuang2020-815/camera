package com.yc.camera.controller;

/**
 * Created By ChengHao On 2020/5/7
 */

import com.yc.camera.service.FrameRecordService;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收流器实现，录制流媒体服务器的rtsp/rtmp视频文件
 *
 * Created By ChengHao On 2020/5/3
 */
@RestController
@RequestMapping("/camera")
public class PullController {

    /**
     * 按帧录制视频
     *
     * @param inputFile  -该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @param audioChannel -1
     */
    @PostMapping("/pull")
    public String frameRecord(@RequestParam String inputFile, @RequestParam String outputFile, @RequestParam Integer audioChannel) {

        boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel);
        // 开始取视频源
        FrameRecordService frameRecordService = new FrameRecordService(grabber, recorder, isStart);
        frameRecordService.start();

        return "success";
    }

}

