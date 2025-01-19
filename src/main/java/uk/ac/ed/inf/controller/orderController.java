package uk.ac.ed.inf.controller;

import com.google.gson.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.inf.data.*;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.utils.OrderHandler;
import uk.ac.ed.inf.utils.PathCalculator;
import uk.ac.ed.inf.constant.SystemConstants;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
public class orderController {
    private final OrderHandler orderHandler = new OrderHandler();
    private final PathCalculator pathCalculator = new PathCalculator();
    private final String restaurantsURI = "https://ilp-rest-2024.azurewebsites.net/restaurants";
    private final String centralAreaURI = "https://ilp-rest-2024.azurewebsites.net/centralArea";
    private final String noFlyZoneURI = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpRequest restrauntRequest = HttpRequest.newBuilder()
            .uri(URI.create(restaurantsURI))
            .build();
    private final HttpRequest noFlyZoneRequest = HttpRequest.newBuilder()
            .uri(URI.create(noFlyZoneURI))
            .build();
    private final HttpRequest centralAreaRequest = HttpRequest.newBuilder()
            .uri(URI.create(centralAreaURI))
            .build();

    /*
    private HttpResponse<String> httpRequest() throws IOException, InterruptedException {
        HttpResponse<String> response = this.httpClient.send(restrauntRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new HttpConnectTimeoutException("Request fail");
        }
        return response;
    }

     */

    public Order stringParseOrder(String body) throws JsonParseException, NullPointerException {
        JsonObject o = JsonParser.parseString(body).getAsJsonObject();
        JsonArray arr = o.get("pizzasInOrder").getAsJsonArray();
        Pizza[] pizzas = new Pizza[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            JsonObject e = arr.get(i).getAsJsonObject();
            pizzas[i] = new Pizza(e.get("name").getAsString(), e.get("priceInPence").getAsInt());
        }
        JsonObject c = o.get("creditCardInformation").getAsJsonObject();
        CreditCardInformation creditCardInformation = new CreditCardInformation(
                c.get("creditCardNumber").getAsString(),
                c.get("creditCardExpiry").getAsString(),
                c.get("cvv").getAsString()
        );
        return new Order(o.get("orderNo").getAsString(),
                LocalDate.parse(o.get("orderDate").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                OrderStatus.valueOf(o.get("orderStatus").getAsString()),
                OrderValidationCode.valueOf(o.get("orderValidationCode").getAsString()),
                o.get("priceTotalInPence").getAsInt(),
                pizzas,
                creditCardInformation
        );
    }

    public Restaurant[] stringParseRestaurants(String body) throws JsonParseException, NullPointerException, IOException, InterruptedException {
        JsonArray r = JsonParser.parseString(body).getAsJsonArray();
        Restaurant[] restaurants = new Restaurant[r.size()];
        for (int i = 0; i < r.size(); i++) {
            JsonObject restaurant = r.get(i).getAsJsonObject();
            JsonArray openingDays = restaurant.get("openingDays").getAsJsonArray();
            JsonArray pizzas = restaurant.get("menu").getAsJsonArray();
            restaurants[i] = new Restaurant(
                    restaurant.get("name").getAsString(),
                    new LngLat(
                            restaurant.get("location").getAsJsonObject().get("lng").getAsDouble(),
                            restaurant.get("location").getAsJsonObject().get("lat").getAsDouble()),
                    IntStream.range(0, openingDays.size())
                            .mapToObj(n -> DayOfWeek.valueOf(openingDays.get(n).getAsString()))
                            .toArray(DayOfWeek[]::new),
                    IntStream.range(0, pizzas.size())
                            .mapToObj(n -> new Pizza(
                                    pizzas.get(n).getAsJsonObject().get("name").getAsString(),
                                    pizzas.get(n).getAsJsonObject().get("priceInPence").getAsInt()))
                            .toArray(Pizza[]::new)
            );
        }
        return restaurants;
    }
    private Restaurant[] uriParseRestaurants(String uri) throws JsonParseException, NullPointerException, IOException, InterruptedException {
        HttpResponse<String> response = this.httpClient.send(restrauntRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new HttpConnectTimeoutException("Request fail");
        }
        JsonArray r = JsonParser.parseString(response.body()).getAsJsonArray();
        Restaurant[] restaurants = new Restaurant[r.size()];
        for (int i = 0; i < r.size(); i++) {
            JsonObject restaurant = r.get(i).getAsJsonObject();
            JsonArray openingDays = restaurant.get("openingDays").getAsJsonArray();
            JsonArray pizzas = restaurant.get("menu").getAsJsonArray();
            restaurants[i] = new Restaurant(
                    restaurant.get("name").getAsString(),
                    new LngLat(
                            restaurant.get("location").getAsJsonObject().get("lng").getAsDouble(),
                            restaurant.get("location").getAsJsonObject().get("lat").getAsDouble()),
                    IntStream.range(0, openingDays.size())
                            .mapToObj(n -> DayOfWeek.valueOf(openingDays.get(n).getAsString()))
                            .toArray(DayOfWeek[]::new),
                    IntStream.range(0, pizzas.size())
                            .mapToObj(n -> new Pizza(
                                    pizzas.get(n).getAsJsonObject().get("name").getAsString(),
                                    pizzas.get(n).getAsJsonObject().get("priceInPence").getAsInt()))
                            .toArray(Pizza[]::new)
            );
        }
        return restaurants;
        /*
        return Stream
                .of(JsonParser.parseString("{\"restaurants\":" +response.body()+"}").getAsJsonObject().get("restaurants").getAsJsonArray())
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

         */
    }

    private NamedRegion stringParseNamedRegion(String body) throws JsonParseException, NullPointerException {
        JsonObject o = JsonParser.parseString(body).getAsJsonObject();
        JsonArray v = o.get("vertices").getAsJsonArray();
        return new NamedRegion(o.get("name").getAsString(),
                IntStream
                        .range(0, v.size())
                        .mapToObj(n -> new LngLat(
                                v.get(n).getAsJsonObject().get("lng").getAsDouble(),
                                v.get(n).getAsJsonObject().get("lat").getAsDouble())
                        )
                        .toArray(LngLat[]::new));
    }

    private NamedRegion[] uriParseNoFlyZones(String uri) throws JsonParseException, NullPointerException, IOException, InterruptedException {
        HttpResponse<String> response = this.httpClient.send(noFlyZoneRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new HttpConnectTimeoutException("Request fail");
        }
        JsonArray ns = JsonParser.parseString(response.body()).getAsJsonArray();
        return IntStream
                .range(0, ns.size())
                .mapToObj(n -> stringParseNamedRegion(ns.get(n).toString()))
                .toArray(NamedRegion[]::new);
    }

    private LngLat[] calPathPlain(String body) throws Exception{
        Order order = stringParseOrder(body);
        Restaurant[] restaurants = this.uriParseRestaurants(this.restaurantsURI);
        order = this.orderHandler.validateOrder(order, restaurants);
        if (order.getOrderStatus() == OrderStatus.INVALID) {
            throw new Exception("Invalid order");
        }
        HttpResponse<String> response = this.httpClient.send(centralAreaRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new HttpConnectTimeoutException("Request fail");
        }
        NamedRegion centralArea = this.stringParseNamedRegion(response.body());
        NamedRegion[] noFlyZones = this.uriParseNoFlyZones(this.noFlyZoneURI);
        Restaurant restaurant = null;
        String res1 = order.getPizzasInOrder()[0].name();
        for (Restaurant r : restaurants) {
            if (Stream.of(r.menu()).anyMatch(pizza -> pizza.name().equals(res1))) {
                restaurant = r;
                break;
            }
        }
        if (restaurant == null) {
            throw new Exception("Restaurant not found");
        }
        return this.pathCalculator.calculatePathJPS(
                restaurant.location(),
                new LngLat(SystemConstants.APPLETON_LNG, SystemConstants.APPLETON_LAT),
                noFlyZones,
                centralArea,
                SystemConstants.DRONE_MAX_MOVES);
    }
    @PostMapping("/orderValidate")
    public OrderValidationResult validateOrder(@RequestBody String body) {
        try {
            Restaurant[] restaurants = uriParseRestaurants(this.restaurantsURI);
            Order order = stringParseOrder(body);
            order = this.orderHandler.validateOrder(order, restaurants);
            //if(order.getOrderStatus() != OrderStatus.VALID){
            //    throw new Exception("Invalid order");
            //}
            return new OrderValidationResult(order.getOrderValidationCode(), order.getOrderStatus());
        } catch (JsonParseException ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|JSON Parser exception: " + ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
        } catch (NullPointerException ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|JSON mapping error: element not found: " + ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
        } catch (HttpConnectTimeoutException sce) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|HTTP Exception, fail when reading restaurants: " + sce);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "HTTP error: read restaurants fail: ", sce);
        } catch (IOException io) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|IO Exception, fail when reading restaurants: " + io);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "IO error: read restaurants fail: ", io);
        } catch (Exception ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|Other error: element not found: " + ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error: ", ex);
        }
    }

    @PostMapping("/calcDeliveryPath")
    public LngLat[] calcDeliveryPath(@RequestBody String body) {
        try {
            return this.calPathPlain(body);
        } catch (Exception ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|Other error: element not found: " + ex);
            System.err.println(Arrays.toString(ex.getStackTrace()));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error: ", ex);
        }
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public String calcDeliveryPathAsGeoJson(@RequestBody String body) {
        try {
            LngLat[] path = this.calPathPlain(body);
            JsonObject result = new JsonObject();
            JsonArray pathJson = new JsonArray();
            for(LngLat p : path) {
                JsonArray pJson = new JsonArray();
                pJson.add(p.lng());
                pJson.add(p.lat());
                pathJson.add(pJson);
            }
            JsonObject corr = new JsonObject();
            result.addProperty("type", "Feature");
            corr.add("coordinates", pathJson);
            corr.addProperty("type", "LineString");
            result.add("geometry", corr);
            JsonObject properties = new JsonObject();
            properties.addProperty("name", "path");
            result.add("properties", properties);
            System.out.println(result.toString());
            return result.toString();
        } catch (Exception ex) {
            System.err.println("[Error] SOURCE = ORDER CONTROLLER|Other error: element not found: " + ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error: ", ex);
        }
    }
    @GetMapping("/status")
    public String liveStatus(){
        return "Order: live";
    }

}

