package com.zfy.djlyolo.yolo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class YoloProperties {

    @Value("${device.type:cpu}")
    private String deviceType;

    @Value("${yolo.url}")
    private String yoloUrl;

    @Value("${yolo.modelname}")
    private String modelName;

    @Value("${yolo.namelist}")
    private String nameList;

    private Float threshold = 0.5f;

    private Integer width = 640;
    private Integer height = 640;

}
