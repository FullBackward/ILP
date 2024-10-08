package uk.ac.ed.inf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.utils.LngLatHandler;

@SpringBootApplication
@RestController
public class App {
    private final LngLatHandler llHandler = new LngLatHandler();
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

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
            return llHandler.distanceTo(pos1, pos2);
        }catch(JsonParseException ex){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
        }catch(NullPointerException ex){
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
            return llHandler.isCloseTo(pos1, pos2);
        }catch(JsonParseException ex){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
        }catch(NullPointerException ex){
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
            double angle = o.get("angle").getAsDouble();
            if(angle % 45 != 0){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Illegal angle");
            }
            LngLat nextPos = llHandler.nextPosition(start, angle);
            JsonObject ret = new JsonObject();
            ret.addProperty("lng", nextPos.lng());
            ret.addProperty("lat", nextPos.lat());
            return ret.toString();
        }catch(JsonParseException ex){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON Parser exception", ex);
        }catch(NullPointerException ex){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "JSON mapping error: element not found", ex);
        }
    }

    @PostMapping("/isInRegion")
    public boolean isInRegion(@RequestBody String body){
        return false;
    }

}
