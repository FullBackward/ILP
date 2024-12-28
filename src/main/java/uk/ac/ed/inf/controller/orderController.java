package uk.ac.ed.inf.controller;

import com.google.gson.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.inf.data.*;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.utils.OrderHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@RestController
public class orderController {
    private final OrderHandler orderHandler = new OrderHandler();
    private final LngLat AT = new LngLat(Double.valueOf("−3.186874"), Double.valueOf("55.944494"));
    private Order stringParseOrder(String body) throws JsonParseException, NullPointerException{
        JsonObject o = JsonParser.parseString(body).getAsJsonObject();
        JsonArray arr = o.get("pizzasInOrder").getAsJsonArray();
        Pizza[] pizzas = new Pizza[arr.size()];
        for(int i = 0; i < arr.size(); i++){
            JsonObject e = arr.get(i).getAsJsonObject();
            pizzas[0] = new Pizza(e.get("name").getAsString(), e.get("priceInPence").getAsInt());
        }
        JsonObject c = o.get("creditCardInformation").getAsJsonObject();
        CreditCardInformation creditCardInformation = new CreditCardInformation(
                c.get("creditCardNumber").getAsString(),
                c.get("creditCardExpiry").getAsString(),
                c.get("ccv").getAsString()
        );
        Order order = new Order(o.get("orderNo").getAsString(),
                LocalDate.parse(o.get("orderDate").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                o.get("priceTotalInPence").getAsInt(),
                pizzas,
                creditCardInformation
        );
        return order;
    }
    private Restaurant[] stringParseRestaurants(String body) throws JsonParseException, NullPointerException{
        Restaurant[] restaurants = Stream
                .of(JsonParser.parseString(body).getAsJsonArray())
                .map(restaurant -> new Restaurant(
                        restaurant.getAsJsonObject().get("name").getAsString(),
                        new LngLat(
                                restaurant.getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsDouble(),
                                restaurant.getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble()),
                        Stream.of(restaurant.getAsJsonObject().get("openingDays").getAsJsonArray())
                                .map(day -> DayOfWeek.valueOf(day.getAsString()))
                                .toArray(DayOfWeek[]::new),
                        Stream.of(restaurant.getAsJsonObject().get("openingDays").getAsJsonArray())
                                .map(pizza -> new Pizza(pizza.getAsJsonObject().get("name").getAsString(),
                                        pizza.getAsJsonObject().get("priceInPence").getAsInt()))
                                .toArray(Pizza[]::new)))
                .toArray(Restaurant[]::new);
            /*
            JsonArray r = JsonParser.parseString(response.body()).getAsJsonArray();
            Restaurant[] restaurants = new Restaurant[r.size()];
            for(int i = 0; i < r.size(); i++){
                JsonObject restaurant = r.get(i).getAsJsonObject();
                restaurants[i] = new Restaurant(
                        restaurant.get("name").getAsString(),
                        new LngLat(
                                restaurant.get("location").getAsJsonObject().get("lng").getAsDouble(),
                                restaurant.get("location").getAsJsonObject().get("lat").getAsDouble()),
                        Stream.of(restaurant.get("openingDays").getAsJsonArray())
                                .map(day -> DayOfWeek.valueOf(day.getAsString()))
                                .toArray(DayOfWeek[]::new),
                        Stream.of(restaurant.get("openingDays").getAsJsonArray())
                                .map(pizza -> new Pizza(pizza.getAsJsonObject().get("name").getAsString(),
                                        pizza.getAsJsonObject().get("priceInPence").getAsInt()))
                                .toArray(Pizza[]::new)
                );
            }
             */
        return restaurants;
    }
    @PostMapping("/orderValidate")
    public OrderValidationResult validateOrder(@RequestBody String body){
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ilp-rest-2024.azurewebsites.net/restaurants"))
                    .build();
            HttpResponse<String> response =  client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            if(response.statusCode() != HttpStatus.OK.value()) {
                throw new HttpConnectTimeoutException("Request fail");
            }
            Restaurant[] restaurants = stringParseRestaurants(response.body());
            Order order = stringParseOrder(body);
            order = this.orderHandler.validateOrder(order, restaurants);
            return new OrderValidationResult(order.getOrderValidationCode(), order.getOrderStatus());
        }catch(JsonParseException ex){
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|JSON Parser exception: "+ ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
        }catch(NullPointerException ex){
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|JSON mapping error: element not found: " + ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
        }catch(HttpConnectTimeoutException sce){
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|IO Exception, fail when reading restaurants: " + sce.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "IO error: read restaurants fail: ", sce);
        }catch(Exception ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|Other error: element not found: " + ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error: ", ex);
        }
    }
    @PostMapping("/calcDeliveryPath")
    public LngLat[] calcDeliveryPath(@RequestBody String body){
        Order order = stringParseOrder(body);
        //order = this.orderHandler.validateOrder(order, restaurants);
        
    }
}
