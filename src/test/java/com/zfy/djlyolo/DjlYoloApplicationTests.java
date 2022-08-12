package com.zfy.djlyolo;

import com.zfy.djlyolo.yolo.YoloService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;


@Slf4j
@SpringBootTest
class DjlYoloApplicationTests {
    @Resource
    private YoloService yoloService;
    @Test
    void test() throws IOException {
        String imgPath = "C:\\Users\\52289\\Downloads\\test.jpeg";
        BufferedImage image = ImageIO.read( new FileInputStream(imgPath) );
        long startTime = System.currentTimeMillis();
        for(int i=0; i<200; i++) {
            BufferedImage result = yoloService.getResultImage(image);
        }
        log.info("ALL cost {}ms", System.currentTimeMillis() - startTime);
    }
}
