package uk.ac.ed.inf.utils;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;

import uk.ac.ed.inf.constant.SystemConstants;

import java.math.BigDecimal;
import java.math.MathContext;

/*
Helper functions to make App.class clean.
 */
public class LngLatHandler implements uk.ac.ed.inf.interfaces.LngLatHandling {
    /*

     */
    @Override
    public double distanceTo(LngLat startPosition, LngLat otherPosition){
        
        BigDecimal lng1 = BigDecimal.valueOf(startPosition.lng());
        BigDecimal lng2 = BigDecimal.valueOf(otherPosition.lng());
        BigDecimal lat1 = BigDecimal.valueOf(startPosition.lat());
        BigDecimal lat2 = BigDecimal.valueOf(otherPosition.lat());
        BigDecimal ac = lng1.subtract(lng2).abs();
        BigDecimal cb = lat1.subtract(lat2).abs();
        //System.out.println("lng1: " + startPosition.lng() + "lng2: " + otherPosition.lng());
        //System.out.println("lat1: " + startPosition.lat() + "lat2: " + otherPosition.lat());
        //System.out.println("ac: " + ac + "cb: " + cb);
        return ac.pow(2).add(cb.pow(2)).sqrt(MathContext.DECIMAL64).doubleValue();
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return this.distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }


    //first do a fast test on point using the min max point
    //next carry out cast-ray algorithm using bigdeciaml to minimise float accuracy problem.
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        BigDecimal plng = BigDecimal.valueOf(position.lng());
        BigDecimal plat = BigDecimal.valueOf(position.lat());
        LngLat[] regionPoints = region.vertices();
        BigDecimal a = BigDecimal.valueOf(0);
        for(LngLat i : regionPoints){

        }
        return false;
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        return null;
    }
}
