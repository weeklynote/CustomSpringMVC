package java;

import com.mar.annotation.MySecurity;
import org.junit.Test;

/**
 * @Author: 刘劲
 * @Date: 2020/4/21 12:39
 */
public class MyTest {

    @Test
    public void test1(){
        final MySecurity annotation = TestController.class.getAnnotation(MySecurity.class);
        System.err.println(annotation.value());
    }
}
