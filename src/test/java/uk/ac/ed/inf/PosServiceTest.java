package uk.ac.ed.inf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    //Re-run tests
    @Test
    void validDistanceTo(){
        String test = """
                {
                            "position1": {
                            "lng": -3.192473,
                                    "lat": 55.946233
                        },
                            "position2": {
                            "lng": -3.192473,
                                    "lat": 55.942617
                        }
                        }
                """;
        double result = posController.distanceTo(test);
        Assertions.assertEquals(0.003616, result, 0.0001);
    }

    @Test
    void validIsCloseTo(){
        String test = """
                   {
                          "position1": {
                            "lng": -3.192473,
                            "lat": 55.946233
                          },
                          "position2": {
                            "lng": -3.192473,
                            "lat": 55.946117
                          }
                 }
                """;
        boolean result = posController.isCloseTo(test);
        Assertions.assertTrue(result);
    }

    @Test
    void validNextPosition(){
        String test = """
                    {
                   "start": {
                          "lng": -3.192473,
                          "lat": 55.946233
                        },
                   "angle": 90 }
                """;
        String result = posController.nextPosition(test);
        JsonObject ll = (JsonObject) JsonParser.parseString(result);
        Assertions.assertEquals(-3.192473,ll.get("lng").getAsDouble());
        Assertions.assertEquals(55.946383,ll.get("lat").getAsDouble());
    }

    @Test
    void validIsInRegion(){
        String text = """
                 {
                  "position": {
                    "lng": -3.186000,
                    "lat": 55.944000
                  },
                  "region": {
                    "name": "central",
                    "vertices": [
                      {
                        "lng": -3.192473,
                        "lat": 55.946233
                      },
                      {
                        "lng": -3.192473,
                        "lat": 55.942617
                      },
                      {
                        "lng": -3.184319,
                        "lat": 55.942617
                      },
                      {
                        "lng": -3.184319,
                        "lat": 55.946233
                      },
                		{
                        "lng": -3.192473,
                        "lat": 55.946233
                      }
                    ]
                  }
                }
                """;
        boolean result = posController.isInRegion(text);
        Assertions.assertTrue(result);
    }
}
