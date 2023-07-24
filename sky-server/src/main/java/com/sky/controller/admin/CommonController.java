package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController
{
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file)
    {
        log.info("上传文件:{}", file);

        String filePath = "D:\\CQ_Nginx\\nginx-1.20.2\\html\\sky\\img\\images";
        String filename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;
        /*创建一个文件对象*/
        File file1 = new File(filePath, newFileName);
        /*保存文件*/
        try
        {
            file.transferTo(file1);
//          直接保存在前端工程内
            return Result.success("img/images/" + newFileName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
