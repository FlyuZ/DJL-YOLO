package com.zfy.djlyolo.yolo;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.zfy.djlyolo.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;


@Slf4j
@Service
public class YoloService {
    @Resource
    private YoloProperties yolo;

    private ZooModel<Image, DetectedObjects> yoloModel;
    @Resource
    ResourceLoader resourceLoader;

    @Value("${engine}")
    public String ENGINE_ONNX;

    @PostConstruct
    public void init() throws ModelNotFoundException, MalformedModelException, IOException {
        log.info("开始加载YOLO模型");

        Device device = Device.Type.CPU.equalsIgnoreCase(yolo.getDeviceType()) ? Device.cpu() : Device.gpu();
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(yolo.getWidth(), yolo.getHeight())); //调整尺寸
        pipeline.add(new ToTensor()); //处理为tensor类型
        Translator<Image, DetectedObjects> translator = YoloV5Translator
                .builder()
                .setPipeline(pipeline)
                .optThreshold(yolo.getThreshold())
                .optSynsetArtifactName(yolo.getNameList())
                .build();

        YoloRelativeTranslator myTranslator = new YoloRelativeTranslator(translator, yolo);


        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optDevice(device)
                .optModelUrls(resourceLoader.getResource("classpath:" + yolo.getYoloUrl()).getURL().getPath())
                .optModelName(yolo.getModelName())
                .optTranslator(myTranslator)
                .optEngine(ENGINE_ONNX)
                .optProgress(new ProgressBar())//展示加载进度
                .build();

        yoloModel = ModelZoo.loadModel(criteria);

        log.info("YOLO模型加载完成");
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(yoloModel)) {
            yoloModel.close();
        }

        log.info("yolo model closed...");
    }


    /**
     * 对象检测函数
     *
     * @param bufferedImage
     */
    public DetectedObjects detect(BufferedImage bufferedImage) {
        BufferedImage scale = ImageUtils.scale(bufferedImage, yolo.getWidth(), yolo.getHeight());
        Image img = ImageFactory.getInstance().fromImage(scale);
        return detect(img);
    }


    /**
     * 对象检测函数
     *
     * @param image 图片
     */
    public DetectedObjects detect(Image image) {
        //将图片大小设置为网络输入要求的大小
        Image scaledImage = ImageUtils.scale(image, yolo.getWidth(), yolo.getHeight());
        long startTime = System.currentTimeMillis();
        //开始检测图片
        DetectedObjects detections;
        try (Predictor<Image, DetectedObjects> predictor = yoloModel.newPredictor()) {
            detections = predictor.predict(scaledImage);
        } catch (TranslateException e) {
            throw new RuntimeException(e);
        }
        log.info("results: {}", detections);
        log.info("detect cost {}ms", System.currentTimeMillis() - startTime);
        return detections;
    }

    /**
     * 检测并绘制结果
     *
     * @param bufferedImage 原始图片
     * @return 带有绘制结果的图片
     */
    public BufferedImage getResultImage(BufferedImage bufferedImage) {
        DetectedObjects detections = detect(bufferedImage);
        //将结果绘制到图片中
        return ImageUtils.drawDetections(bufferedImage, detections);
    }

}
