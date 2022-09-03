package com.su.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Resource
    private OSSClient ossClient;

    @Test
    void testUpload() {
        try (InputStream inputStream = new FileInputStream("C:/Users/14772/Documents/资料/谷粒商城/pics/0d40c24b264aa511.jpg")) {
            ossClient.putObject("gulimall-oss-su", "0d40c24b264aa511.jpg", inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
