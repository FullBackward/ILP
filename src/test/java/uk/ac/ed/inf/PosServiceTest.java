package uk.ac.ed.inf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ed.inf.controller.posController;
@SpringBootTest
public class PosServiceTest {
    @Autowired
    private posController posController;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {

    }

    @Test
    void statusTest(){
        String result = posController.liveStatus();
        Assertions.assertEquals("live", result);
    }

}
