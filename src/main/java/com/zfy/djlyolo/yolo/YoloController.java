package com.zfy.djlyolo.yolo;
import ai.djl.modality.cv.output.DetectedObjects;
import com.zfy.djlyolo.utils.DetectObjectDTO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("yolo")
@Slf4j
public class YoloController {
    @Resource
    private YoloService yoloService;

    @PostMapping("getImage")
    @ApiOperation("图片目标检测，返回结果图片")
    public void detect(@RequestPart MultipartFile file, HttpServletResponse response) throws IOException {

        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage result = yoloService.getResultImage(image);

        response.setContentType("image/png");
        ServletOutputStream os = response.getOutputStream();
        ImageIO.write(result, "PNG", os);
        os.flush();
    }


    @PostMapping("getResult")
    @ApiOperation("图片目标检测，返回结果对象")
    public List<DetectObjectDTO> detect(@RequestPart MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        DetectedObjects result = yoloService.detect(image);

        return result.items().stream().map(DetectObjectDTO::new).collect(Collectors.toList());
    }

}
