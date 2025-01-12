package uk.ac.ed.inf.utils;

import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;
import uk.ac.ed.inf.utils.LngLatHandler;

import java.util.*;
import java.util.stream.Stream;

public class PathCalculator {
    private final LngLatHandler lngLatHandler = new LngLatHandler();
    public PathCalculator(){}
    private static class jumpReturn{
        private pP point;
        private NamedRegion region;
        public jumpReturn(){}

        public void setPoint(pP point) {
            this.point = point;
        }
        public void setRegion(NamedRegion region) {
            this.region = region;
        }
        public NamedRegion getRegion() {
            return region;
        }
        public pP getPoint() {
            return point;
        }
    }
    private static class pP implements Comparable<pP> {
        private final LngLat point;
        private double g;
        private double h;
        private double f;
        private pP parent;
        public pP(LngLat point){
            this.point = point;
            this.g = 0;
            this.h = 0;
            this.f = 0;
            this.parent = null;
        }
        public LngLat getPoint(){
            return this.point;
        }
        public double getG(){
            return this.g;
        }
        public double getH(){
            return this.h;
        }
        public double getF(){
            return this.f;
        }
        public pP getParent(){
            return this.parent;
        }
        public void setF(double f) {
            this.f = f;
        }
        public void setG(double g) {
            this.g = g;
        }
        public void setH(double h) {
            this.h = h;
        }
        public void setParent(pP parent) {
            this.parent = parent;
        }
        @Override
        public int compareTo(pP o) {
            return Double.compare(this.getF(), o.getF());
        }
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(!(obj instanceof pP)) return false;
            pP other = (pP) obj;
            return Double.compare(this.point.lng(), other.getPoint().lng()) == 0
                    && Double.compare(this.point.lat(), other.getPoint().lat()) == 0;
        }
        @Override
        public int hashCode() {
            // Basic hash combining lat/lng
            long tempLng = Double.doubleToLongBits(point.lng());
            long tempLat = Double.doubleToLongBits(point.lat());
            return (int)(tempLng ^ (tempLng >>> 32)) ^ (int)(tempLat ^ (tempLat >>> 32));
        }
    }
    private jumpReturn jump(pP currentPoint, LngLat end, NamedRegion[] noFlyZones){
        jumpReturn jumpReturn = new jumpReturn();
        while(true){
            LngLat currentLL = currentPoint.getPoint();
            pP[] neighbours = new pP[16];
            int n = 0;
            for(double i = 0; i < 360; i += 22.5){
                LngLat npos = this.lngLatHandler.nextPosition(currentLL, i);
                neighbours[n] = new pP(npos);
                neighbours[n].setParent(currentPoint);
                neighbours[n].setF(lngLatHandler.distanceTo(npos, end));
                n++;
            }
            Arrays.sort(neighbours);
            pP nextPos = neighbours[0];
            for(NamedRegion region: noFlyZones){
                if(lngLatHandler.isInRegion(nextPos.getPoint(), region)){
                    jumpReturn.setRegion(region);
                    jumpReturn.setPoint(currentPoint);
                    return jumpReturn;
                }
            }
            if(!lngLatHandler.isLngLat(nextPos.getPoint())){
                return null;
            }
            if(lngLatHandler.isCloseTo(nextPos.getPoint(), end)){
                jumpReturn.setPoint(nextPos);
                return jumpReturn;
            }
            currentPoint = nextPos;
        }
    }
    private void prettyPrintPath(ArrayList<LngLat> path){
        double[][] printPath= new double[path.size()][2];
        for(int i = 0; i < path.size(); i++){
            printPath[i][0] = path.get(i).lng();
            printPath[i][1] = path.get(i).lat();
        }
        System.out.println(path.size());
        System.out.println(Arrays.deepToString(printPath));
    }
    private double getAngle(LngLat p1, LngLat p2) {
        double angle = Math.toDegrees(Math.atan2(p1.lat() - p2.lat(), p1.lng()) - p2.lng());

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }
    private ArrayList<LngLat> reconstructPathASTAR(HashMap<LngLat, LngLat> cameFrom, LngLat end, LngLat start){
        ArrayList<LngLat> path = new ArrayList<>();
        LngLat point = end;
        while(!point.equals(start)){
            path.add(point);
            point = cameFrom.get(point);
        }
        return path;
    }
    private LngLat[] reconstructPathJPS(pP end, pP start, int maxSteps) throws Exception {
        ArrayList<LngLat> pathAL = new ArrayList<>();
        pP p = end;
        while(p != start){
            pathAL.add(p.point);
            if(p.getParent() == null){
                break;
            }
            p = p.getParent();
        }
        Collections.reverse(pathAL);
        if(pathAL.size() > maxSteps) throw new Exception("Max steps meets");
        this.prettyPrintPath(pathAL);
        return pathAL.toArray(new LngLat[pathAL.size()]);
    }
    public LngLat[] calculatePathWHILE(LngLat start, LngLat end, NamedRegion[] noFlyZones, NamedRegion stayInZone, int maxSteps){
        ArrayList<NamedRegion> newZones = new ArrayList<>();
        double minLng = Double.min(start.lng(), end.lng());
        double maxLng = Double.max(start.lng(), end.lng());
        double minLat = Double.min(start.lat(), end.lat());
        double maxLat = Double.max(start.lat(), end.lat());
        for(NamedRegion zone: noFlyZones){
            boolean added = false;
            for(LngLat v: zone.vertices()){
                if(minLng <= v.lng() && maxLng >= v.lng() && minLat <= v.lat() && maxLat >= v.lat()){
                    newZones.add(zone);
                    added = true;
                    break;
                }
            }
            if(added) break;
        }
        //WHILE loop search
        ArrayList<LngLat> path = new ArrayList<>();
        int step = 0;
        path.add(start);
        LngLat currentPos = start;
        boolean inZone = this.lngLatHandler.isInRegion(currentPos, stayInZone);
        boolean touched = false;
        while(!lngLatHandler.isCloseTo(currentPos, end) && step < maxSteps){
            pP[] list = new pP[16];
            int n = 0;
            for(double i = 0; i < 360; i += 22.5){
                //i = i % 360;
                LngLat npos = this.lngLatHandler.nextPosition(currentPos, i);
                if(Stream.of(newZones.toArray()).anyMatch(zone -> this.lngLatHandler.isInRegion(npos, (NamedRegion) zone))){
                    list[n] = new pP(npos);
                    list[n].setF(Double.MAX_VALUE);
                    touched = true;
                }else if(inZone){
                    if(!this.lngLatHandler.isInRegion(npos, stayInZone)){
                        list[n] = new pP(npos);
                        list[n].setF(Double.MAX_VALUE);
                    }else{
                        list[n] = new pP(npos);
                        list[n].setF( this.lngLatHandler.distanceTo(end, npos));
                    }
                }else{
                    list[n] = new pP(npos);
                    list[n].setF( this.lngLatHandler.distanceTo(end, npos));
                }
                if(path.contains(npos)){
                    list[n] = new pP(npos);
                    list[n].setF(Double.MAX_VALUE);
                }
                n += 1;
            }
            Arrays.sort(list);
            currentPos = list[0].getPoint();
            path.add(currentPos);
            step ++;
        }
        if(step >= maxSteps){
            System.out.println("No path found");
        }
        this.prettyPrintPath(path);
        //Collections.reverse(path);
        return path.toArray(new LngLat[path.size()]);
    }
    public LngLat[] calculatePathASTAR(LngLat start, LngLat end, NamedRegion[] noFlyZones, NamedRegion stayInZone, int maxSteps) {
        ArrayList<NamedRegion> newZones = new ArrayList<>();
        double minLng = Double.min(start.lng(), end.lng());
        double maxLng = Double.max(start.lng(), end.lng());
        double minLat = Double.min(start.lat(), end.lat());
        double maxLat = Double.max(start.lat(), end.lat());
        for (NamedRegion zone : noFlyZones) {
            boolean added = false;
            for (LngLat v : zone.vertices()) {
                if (minLng <= v.lng() && maxLng >= v.lng() && minLat <= v.lat() && maxLat >= v.lat()) {
                    newZones.add(zone);
                    added = true;
                    break;
                }
            }
            if (added) break;
        }
        //double angle = this.getAngle(start, end) - 90;
        //if((angle - 90) < 0) angle += 360;
        //A-star, not working
        PriorityQueue<pP> frontier = new PriorityQueue<>();
        ArrayList<LngLat> visited = new ArrayList<>();
        HashMap<LngLat, LngLat> cameFrom = new HashMap<>();
        HashMap<LngLat, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        ArrayList<LngLat> path = new ArrayList<>();
        frontier.add(new pP(start));
        while (!frontier.isEmpty()) {
            pP p = frontier.poll();
            if (lngLatHandler.isCloseTo(p.point, end)) {
                System.out.println(p.point.toString());
                System.out.println(cameFrom);
                path = this.reconstructPathASTAR(cameFrom, p.point, start);
                break;
            }
            visited.add(p.point);
            if (frontier.size() == maxSteps) {
                System.out.println("No path found");
                return null;
            }
            boolean inZone = this.lngLatHandler.isInRegion(p.point, stayInZone);
            for (double a = 0; a < 360; a += 22.5) {
                LngLat neighbour = lngLatHandler.nextPosition(p.point, a);
                if (Stream.of(noFlyZones).anyMatch(zone -> this.lngLatHandler.isInRegion(neighbour, (NamedRegion) zone))) {
                    break;
                } else if (inZone) {
                    if (!this.lngLatHandler.isInRegion(neighbour, stayInZone)) {
                        break;
                    }
                }
                double newG = gScore.get(p.point) + SystemConstants.DRONE_MOVE_DISTANCE;
                double oldG = Double.MAX_VALUE;
                if (gScore.containsKey(neighbour)) {
                    oldG = gScore.get(neighbour);
                }
                if (!visited.contains(neighbour) || newG < oldG) {
                    cameFrom.put(neighbour, p.point);
                    if (gScore.containsKey(neighbour)) {
                        gScore.replace(neighbour, newG);
                    } else {
                        gScore.put(neighbour, newG);
                    }
                    //frontier.add(new pP(neighbour, newG + lngLatHandler.distanceTo(neighbour, end)));
                }
            }

        }
        //Collections.reverse(path);
        double[][] printPath= new double[path.size()][2];
        for(int i = 0; i < path.size(); i++){
            printPath[i][0] = path.get(i).lng();
            printPath[i][1] = path.get(i).lat();
        }
        System.out.println(Arrays.deepToString(printPath));
        return path.toArray(new LngLat[path.size()]);
    }
    public LngLat[] calculatePathJPS(LngLat start, LngLat end, NamedRegion[] noFlyZones, NamedRegion stayInZone, int maxSteps) throws Exception {
        ArrayList<NamedRegion> newZones = new ArrayList<>();
        double minLng = Double.min(start.lng(), end.lng());
        double maxLng = Double.max(start.lng(), end.lng());
        double minLat = Double.min(start.lat(), end.lat());
        double maxLat = Double.max(start.lat(), end.lat());
        for(NamedRegion zone: noFlyZones){
            boolean added = false;
            for(LngLat v: zone.vertices()){
                if(minLng <= v.lng() && maxLng >= v.lng() && minLat <= v.lat() && maxLat >= v.lat()){
                    newZones.add(zone);
                    added = true;
                    break;
                }
            }
            if(added) break;
        }
        //JPS
        PriorityQueue<pP> frontier = new PriorityQueue<>();
        Set<pP> closedSet = new HashSet<>();
        pP startpP = new pP(start);
        if(lngLatHandler.isCloseTo(start, end)){
            return new LngLat[]{startpP.point};
        }
        if(Stream.of(newZones.toArray()).anyMatch(zone -> this.lngLatHandler.isInRegion(start, (NamedRegion) zone))){
            throw new Exception("Start blocked");
        }
        if(Stream.of(newZones.toArray()).anyMatch(zone -> this.lngLatHandler.isInRegion(end, (NamedRegion) zone))){
            throw new Exception("End blocked");
        }
        startpP.setG(0);
        startpP.setH(lngLatHandler.distanceTo(start, end));
        startpP.setF(startpP.getG() + startpP.getH());
        frontier.add(startpP);
        while(!frontier.isEmpty()){
            pP p = frontier.poll();
            closedSet.add(p);
            if(frontier.size() >= maxSteps){
                throw new Exception("Max steps reached");
            }
            if(lngLatHandler.isCloseTo(p.getPoint(), end)){
                return reconstructPathJPS(p, startpP, maxSteps);
            }
            jumpReturn jp = jump(p, end, newZones.toArray(new NamedRegion[0]));
            if(jp == null ||(jp.getPoint() == null && jp.getRegion() == null)){
                throw new Exception("Path not found");
            }
            if(jp.getPoint() == null){
                throw new Exception("Path not found");
            }
            if(jp.getRegion() == null){
                return reconstructPathJPS(jp.getPoint(), startpP, maxSteps);
            }
            p = jp.getPoint();
            for(double i = 0; i < 360; i += 22.5){
                pP nextP = new pP(lngLatHandler.nextPosition(p.getPoint(), i));
                if(!lngLatHandler.isLngLat(nextP.getPoint())){
                    throw new Exception("Next point is not a lng lat");
                }
                if(lngLatHandler.isInRegion(nextP.getPoint(), jp.getRegion())) continue;
                System.out.println("visiting:" + nextP.getPoint().toString());
                if(closedSet.contains(nextP)) continue;
                double tentativeG = p.getG() + SystemConstants.DRONE_MOVE_DISTANCE;
                if(tentativeG < nextP.getG() || !frontier.contains(nextP)){
                    nextP.setG(tentativeG);
                    nextP.setH(lngLatHandler.distanceTo(nextP.getPoint(), end));
                    nextP.setF(nextP.getG() + nextP.getH());
                    nextP.setParent(p);
                    if(!frontier.contains(nextP)){
                        frontier.add(nextP);
                    }
                }
            }
        }
        throw new Exception("No path found");
    }
}
