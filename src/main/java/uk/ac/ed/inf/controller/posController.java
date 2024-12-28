package uk.ac.ed.inf.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;
import uk.ac.ed.inf.utils.LngLatHandler;

import java.util.ArrayList;

@RestController
public class posController {
        private final LngLatHandler llHandler = new LngLatHandler();

        //GET uuid, return student id as a string without formatting
        @GetMapping("/uuid")
        public String uuid() {
            return "s2415809";
        }

        //
        @PostMapping("/distanceTo")
        public double distanceTo(@RequestBody String body){
            try{
                JsonObject o = JsonParser.parseString(body).getAsJsonObject();
                JsonObject temp = o.get("position1").getAsJsonObject();
                LngLat pos1 = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                temp = o.get("position2").getAsJsonObject();
                LngLat pos2 = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                if(!(llHandler.isLngLat(pos1) && llHandler.isLngLat(pos2))){
                    System.err.println("[Error] SOURCE = POS CONTROLLER|Invalid position lng lat value");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                return llHandler.distanceTo(pos1, pos2);
            }catch(JsonParseException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON Parser exception: "+ ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
            }catch(NullPointerException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON mapping error: element not found: " + ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
            }
        }

        @PostMapping("/isCloseTo")
        public boolean isCloseTo(@RequestBody String body){
            try{
                JsonObject o = JsonParser.parseString(body).getAsJsonObject();
                JsonObject temp = o.get("position1").getAsJsonObject();
                LngLat pos1 = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                temp = o.get("position2").getAsJsonObject();
                LngLat pos2 = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                if(!(llHandler.isLngLat(pos1) && llHandler.isLngLat(pos2))){
                    System.err.println("[Error] SOURCE = POS CONTROLLER|Invalid position lng lat value");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                return llHandler.isCloseTo(pos1, pos2);
            }catch(JsonParseException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON Parser exception: "+ ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
            }catch(NullPointerException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON mapping error: element not found: " + ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
            }
        }

        @PostMapping("/nextPosition")
        public String nextPosition(@RequestBody String body){
            try{
                JsonObject o = JsonParser.parseString(body).getAsJsonObject();
                JsonObject temp = o.get("start").getAsJsonObject();
                LngLat start = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                double angle = o.get("angle").getAsInt();
                if(angle % 45 != 0){
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Illegal angle");
                }
                angle = angle % 360;
                if(!llHandler.isLngLat(start)){
                    System.err.println("[Error] SOURCE = POS CONTROLLER|Invalid position lng lat value");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                LngLat nextPos = llHandler.nextPosition(start, angle);
                JsonObject ret = new JsonObject();
                ret.addProperty("lng", nextPos.lng());
                ret.addProperty("lat", nextPos.lat());
                return ret.toString();
            }catch(JsonParseException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON Parser exception: "+ ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
            }catch(NullPointerException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON mapping error: element not found: " + ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
            }
        }

        @PostMapping("/isInRegion")
        public boolean isInRegion(@RequestBody String body){
            try{
                JsonObject o = JsonParser.parseString(body).getAsJsonObject();
                JsonObject temp = o.get("position").getAsJsonObject();
                LngLat pos = new LngLat(temp.get("lng").getAsDouble(), temp.get("lat").getAsDouble());
                if(!llHandler.isLngLat(pos)){
                    System.err.println("[Error] SOURCE = POS CONTROLLER|Invalid position lng lat value");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                temp = o.get("region").getAsJsonObject();
                String name = temp.get("name").getAsString();
                JsonArray list = temp.get("vertices").getAsJsonArray();
                ArrayList<LngLat> vertices = new ArrayList<>(list.asList().stream().map(JsonElement ->
                        new LngLat(JsonElement.getAsJsonObject().get("lng").getAsDouble(),
                                JsonElement.getAsJsonObject().get("lat").getAsDouble())).toList());
                NamedRegion region = llHandler.isNamedRegion(vertices, name);
                if(region.name().isEmpty()){
                    System.err.println("[Error] SOURCE = POS CONTROLLER|Illegal named region");
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Illegal named region");
                }
                return llHandler.isInRegion(pos, region);
            }catch(JsonParseException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON Parser exception: "+ ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
            }catch(NullPointerException ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|JSON mapping error: element not found: " + ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
            }catch(Exception ex){
                System.err.println("[Error] SOURCE = POS CONTROLLER|Other error: element not found: " + ex.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Error: ", ex);
            }
        }

    }

