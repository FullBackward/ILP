package uk.ac.ed.inf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.controller.orderController;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.data.OrderValidationResult;
import uk.ac.ed.inf.data.Restaurant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

@SpringBootTest
public class OrderServiceTest {
    private String[] testOrders;
    private final String restaurantsURI = "https://ilp-rest-2024.azurewebsites.net/restaurants";
    private HttpClient httpClient = HttpClient.newHttpClient();
    private final String testOrdersURI = "https://ilp-rest-2024.azurewebsites.net/orders";
    private HttpRequest testOrdersRequest = HttpRequest.newBuilder()
            .uri(URI.create(this.testOrdersURI))
            .build();
    private HttpRequest restraurantsRequest = HttpRequest.newBuilder()
            .uri(URI.create(this.restaurantsURI))
            .build();
    @Autowired
    private orderController orderController;

    @BeforeEach
    public void setUpBeforeEach() throws Exception {
        HttpResponse<String> response = this.httpClient.send(this.testOrdersRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new HttpConnectTimeoutException("Request fail");
        }
        JsonArray r = JsonParser.parseString(response.body()).getAsJsonArray();
        //System.out.println(r.size());
        this.testOrders = new String[r.size()];
        for(int i = 0; i < r.size(); i++){
            JsonObject je = (JsonObject) r.get(i);
            this.testOrders[i] = je.toString();
        }
    }

    @Test
    public void testOrderStatus() throws Exception {
        String result = this.orderController.liveStatus();
        Assertions.assertEquals("Order: live", result);
    }

    @Test
    public void testOrder1() throws Exception {
        String order = this.testOrders[0];
        System.out.println(order);
        Order o = orderController.stringParseOrder(order);
        OrderValidationResult v = orderController.validateOrder(order);
        System.out.println(v.getOrderValidationCode().toString());
        Assertions.assertEquals(o.getOrderStatus(), v.getOrderStatus());
        Assertions.assertEquals(o.getOrderValidationCode(), v.getOrderValidationCode());
    }
    @Test
    public void testOrders() throws Exception {
        for(String order : this.testOrders){
            Order o = orderController.stringParseOrder(order);
            OrderValidationResult v = orderController.validateOrder(order);
            System.out.println(order);
            System.out.println(v.getOrderValidationCode().toString());
            Assertions.assertEquals(o.getOrderStatus(), v.getOrderStatus());
            Assertions.assertEquals(o.getOrderValidationCode(), v.getOrderValidationCode());
        }
    }

    @Test
    public void testRestaurantPath() throws Exception {
        HttpResponse<String> response = this.httpClient.send(this.restraurantsRequest, HttpResponse.BodyHandlers.ofString());
        Restaurant[] restaurants = orderController.stringParseRestaurants(response.body());
        for(Restaurant restaurant : restaurants){
            System.out.println(restaurant.name());
            for(String order: testOrders){
                Order o = orderController.stringParseOrder(order);
                if(o.getOrderStatus() == OrderStatus.VALID && Stream.of(restaurant.menu()).anyMatch(pizza -> pizza.name().equals(o.getPizzasInOrder()[0].name()))){
                    long start = System.nanoTime();
                    LngLat[] path = orderController.calcDeliveryPath(order);
                    long end = System.nanoTime();
                    System.out.println((end - start) / 1_000_000.0 + "ms");
                    Assertions.assertNotNull(path);
                    Assertions.assertTrue(path.length > 0);
                    break;
                }
            }
            System.out.println("-------------------------|");
        }
    }
}
