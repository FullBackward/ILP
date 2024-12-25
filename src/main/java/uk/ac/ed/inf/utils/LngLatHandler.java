package uk.ac.ed.inf.utils;

import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;


/*
Helper functions to make App.class clean.
 */
public class LngLatHandler implements uk.ac.ed.inf.interfaces.LngLatHandling {
    /*

     */
    @Override
    public double distanceTo(LngLat startPosition, LngLat otherPosition){
        try{
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
        }catch(Exception exp){
            return -1;
        }
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return this.distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }


    //carry out cast-ray algorithm using bigdeciaml to minimise float accuracy problem.
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        BigDecimal plng = BigDecimal.valueOf(position.lng());
        BigDecimal plat = BigDecimal.valueOf(position.lat());
        LngLat[] regionPoints = region.vertices();
        boolean inside = false;
        int len = regionPoints.length;
        //System.out.println("Point: " + position.lng() + ";" + position.lat());
        for (int i = 0; i < len - 1; i++) {
            //System.out.println("Vertice 1: " + regionPoints[i].lng() + ";" + regionPoints[i].lat());
            //System.out.println("Vertice 2: " + regionPoints[i + 1].lng() + ";" + regionPoints[i + 1].lat());
            if(inLine(regionPoints[i], position, regionPoints[i + 1])){
                //System.out.println("inline");
                return true;
            }
            if (RayCasting.intersects(
                    new BigDecimal[] {BigDecimal.valueOf(regionPoints[i].lng()),
                            BigDecimal.valueOf(regionPoints[i].lat())},
                    new BigDecimal[] {BigDecimal.valueOf(regionPoints[i + 1].lng()),
                            BigDecimal.valueOf(regionPoints[i + 1].lat())},
                    new BigDecimal[]{plng, plat})) {
                //System.out.println("intersect");
                inside = !inside;
            }//else{
                //System.out.println("not intersect");
            //}
        }
        return inside;
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        BigDecimal sinValue = BigDecimal.valueOf(Math.sin(Math.toRadians(angle)));
        BigDecimal cosValue = BigDecimal.valueOf(Math.cos(Math.toRadians(angle)));
        BigDecimal slng = BigDecimal.valueOf(startPosition.lng());
        BigDecimal slat = BigDecimal.valueOf(startPosition.lat());
        double nlng = slng.add(cosValue.multiply(BigDecimal.valueOf(SystemConstants.DRONE_MOVE_DISTANCE))).doubleValue();
        double nlat = slat.add(sinValue.multiply(BigDecimal.valueOf(SystemConstants.DRONE_MOVE_DISTANCE))).doubleValue();
        if((nlng > 180)){
            nlng = nlng - 360;
        }
        if((nlng < -180)){
            nlng = nlng + 360;
        }
        if(nlat > 90){
            nlat = 180 - nlat;
        }
        if(nlat < -90){
            nlat = -180 - nlat;
        }
        return new LngLat(nlng, nlat);
    }

    @Override
    public NamedRegion isNamedRegion(List<LngLat> vertices, String name){
        for(int i = 0; i < vertices.size() - 2; i++){
            LngLat v1 = vertices.get(i);
            LngLat v2 = vertices.get(i + 1);
            LngLat v3 = vertices.get(i + 2);
            if(inLine(v1, v2, v3)){
                vertices.remove(i + 1);
            }
            if(v1.equals(v2) || v2.equals(v3)){
                return new NamedRegion("", new LngLat[] {});
            }
        }
        for(LngLat i : vertices){
            if(!this.isLngLat(i)){
                return new NamedRegion("", new LngLat[] {});
            }
        }
        if(vertices.size() < 4 || !vertices.get(0).equals(vertices.get(vertices.size() - 1))){
            return new NamedRegion("", new LngLat[] {});
        }
        return new NamedRegion(name, vertices.toArray(new LngLat[0]));
    }

    @Override
    public boolean inLine(LngLat v1, LngLat v2, LngLat v3){
        BigDecimal x1 = BigDecimal.valueOf(v1.lng());
        BigDecimal x2 = BigDecimal.valueOf(v2.lng());
        BigDecimal x3 = BigDecimal.valueOf(v3.lng());
        BigDecimal y1 = BigDecimal.valueOf(v1.lat());
        BigDecimal y2 = BigDecimal.valueOf(v2.lat());
        BigDecimal y3 = BigDecimal.valueOf(v3.lat());
        double area = x1.multiply(y2.subtract(y3))
                .add(x2.multiply(y3.subtract(y1))).add(x3.multiply(y1.subtract(y2))).doubleValue();
        boolean order = ((x1.compareTo(x2) <= 0 && x2.compareTo(x3) <= 0)
                || (x3.compareTo(x2) <= 0 && x2.compareTo(x1) <= 0))
                && ((y1.compareTo(y2) <= 0 && y2.compareTo(y3) <= 0)
                || (y3.compareTo(y2) <= 0 && y2.compareTo(y1) <= 0));
        return (area == 0 || BigDecimal.valueOf(area).compareTo(BigDecimal.valueOf(0.000000000001)) < 0) && order;
    }

    @Override
    public boolean isLngLat(LngLat pos){
        return pos.lng() <= 180 && pos.lng() >= -180 && pos.lat() <= 90 && pos.lat() >= -90;
    }
}
