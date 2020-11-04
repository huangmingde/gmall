package com.atguigu.gmall.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
public class FileUpload_Controller {

    @Value("${fileServer.url}") //软编码（从配置文件中获取值）
    private String linux_ip;    // linux_ip = http://192.168.202.129

    /**
     *  文件上传的控制器：http://localhost:8082/fileUpload
     */
    @RequestMapping("/fileUpload")
    public String fileUpload(MultipartFile file) throws IOException, MyException {

        String img_LinuxIP = linux_ip;

        //当控制器接收前端文件不为null，进行文件上传
        if(file!=null){
            //读取tracker配置文件（src/main/resources/tracker.conf）
            String tracker_ConfigFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(tracker_ConfigFile);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            //获取上传文件名称
            String uploadFileName = file.getOriginalFilename();
            //获取上传文件的后缀名
            String extName = StringUtils.substringAfterLast(uploadFileName, ".");
            //上传文件
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                img_LinuxIP+="/"+path;
                //拼图片URL【img_LinuxIP=http://192.168.202.129/group1/M00/00/00/wKssejKgV8d.jpg】
            }
        }

        return img_LinuxIP;  //返回图片URL
    }
}
