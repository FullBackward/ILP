package uk.ac.ed.inf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/*
Tests for semantic and syntax errors in order validate.
Since path calculation use validated order, so no test on that will be here.
 */

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Test
    void sementicOrder() throws Exception {
        String test = """
                  {
                    "orderNo": "6B9623EC",
                    "orderDate": "20250208",
                    "orderStatus": "VALID",
                    "orderValidationCode": "NO_ERROR",
                    "priceTotalInPence": 2500,
                    "pizzasInOrder": [
                      {
                        "name": "R1: Margarita",
                        "priceInPence": 1000
                      },
                      {
                        "name": "R1: Calzone",
                        "priceInPence": 1400
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5306310202502475",
                      "creditCardExpiry": "02/26",
                      "cvv": "992"
                    }
                  }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/orderValidate").content(test)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    void syntaxOrder() throws Exception {
        String test = """
                  {
                    "orderNo": "1E5FCE1B",
                    "orderDate": "2025-02-08",
                    "orderStatus1": "VALID",
                    "orderValidationCode": "NO_ERROR",
                    "priceTotalInPence": 2400,
                    "pizzasInOrder": [
                      {
                        "name": "R3: Super Cheese",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R3: All Shrooms",
                        "priceInPence": 900
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5508986912530132",
                      "creditCardExpiry": "12/25",
                      "cvv": "511"
                    }
                  }
        """;
        mockMvc.perform(MockMvcRequestBuilders.post("/orderValidate").content(test)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    void emptyBodyOrder() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/orderValidate").content("")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
