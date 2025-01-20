package uk.ac.ed.inf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class PosControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testControllerStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/live"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //Re-run tests
    @Test
    public void semanticDistanceTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/distanceTo").content("""
                 {
                         "position1": {
                           "lng": -300.192473,
                           "lat": 550.946233
                         },
                         "position2": {
                           "lng": -3202.192473,
                           "lat": 5533.942617
                         }
                }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void syntaxDistanceTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/distanceTo").content("""
                  {
                          "position1": {
                            "lng": -3.192473,
                          },
                          "position2": {
                            "lng": -3.192473,
                            "lat_Pos2": 55.942617
                          }
                 }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void emptyBodyDistanceTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/distanceTo").content("")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void semanticIsCloseTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isCloseTo").content("""
                  {
                         "position1": {
                           "lng": -3004.192473,
                           "lat": 550.946233
                         },
                         "position2": {
                           "lng": -390.192473,
                           "lat": 551.942617
                         }
                }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void syntaxIsCloseTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isCloseTo").content("""
                 {
                         "position1": {
                           "lng": -3.192473,
                           "lat": 55.946233
                         },
                         "position3": {
                           "lng": -3.192473,
                           "lat": 55.942617
                         }
                }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void emptyBodyIsCloseTo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isCloseTo").content("")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void semanticNextPosition() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/nextPosition").content("""
                 {
                "start": {
                       "lng": -3.192473,
                       "lat": 55.946233
                     },
                "angle": 900 }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void syntaxNextPosition() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/nextPosition").content("""
                {
                "startPosition": {
                       "lng": -3.192473,
                       "lat": 55.946233
                     },
                "angle": 90 }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void emptyBodyNextPosition() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/nextPosition").content("")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void semanticIsInRegion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isInRegion").content("""
                 {
                  "position": {
                    "lng": -390.186000,
                    "lat": 550.944000
                  },
                  "region": {
                    "name": "central",
                    "vertices": [
                      {
                        "lng": -3.192473,
                        "lat": 558.946233
                      },
                      {
                        "lng": -367.192473,
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
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void syntaxIsInRegion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isInRegion").content("""
                {
                  "currentPosition": {
                    "lng": 1.234,
                    "lat": 1.222
                  },
                  "region": {
                    "names": "central",
                    "verticesList": [
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
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void emptyBodyIsInRegion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isInRegion").content("")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void openVerticesIsInRegion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/isInRegion").content("""
                {
                  "position": {
                    "lng": 398.234,
                    "lat": 500.222
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
                      }
                    ]
                  }
                }
                """)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
