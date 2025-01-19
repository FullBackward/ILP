package uk.ac.ed.inf.utils;

import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;

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
    private jumpReturn jump(pP currentPoint, LngLat end, NamedRegion[] noFlyZones, NamedRegion stayInZone){
        jumpReturn jumpReturn = new jumpReturn();
        boolean in = false;
        while(true){
            //System.out.println("jumping");
            //System.out.println(currentPoint.getPoint());
            //System.out.println(end);
            //System.out.println(noFlyZones.length);
            LngLat currentLL = currentPoint.getPoint();
            if(lngLatHandler.isInRegion(currentLL, stayInZone)) in = true;
            PriorityQueue<pP> neighbours = new PriorityQueue<>();
            double angle = getAngle(currentLL, end);
            //System.out.println(angle);
            for(double i = angle + 45; i >= angle - 45; i -= 22.5){
                double a = i;
                if(a > 360){
                    a -= 360;
                }
                if(a < 0){
                    a += 360;
                }
                //System.out.println(a);
                LngLat npos = this.lngLatHandler.nextPosition(currentLL, i);
                pP newP = new pP(npos);
                newP.setParent(currentPoint);
                newP.setF(lngLatHandler.distanceTo(npos, end));
                neighbours.add(newP);
            }
            pP nextPos = neighbours.poll();
            if(nextPos == null){
                return null;
            }
            //System.out.println(nextPos.getPoint());
            if(lngLatHandler.isCloseTo(nextPos.getPoint(), end)){
                jumpReturn.setPoint(nextPos);
                return jumpReturn;
            }
            if((in && !lngLatHandler.isInRegion(nextPos.getPoint(), stayInZone))){
                jumpReturn.setRegion(stayInZone);
                jumpReturn.setPoint(currentPoint);
                return jumpReturn;
            }
            for(NamedRegion region: noFlyZones){
                double[] ds = new double[region.vertices().length];
                for(int i = 0; i < ds.length; i++){
                    ds[i] = lngLatHandler.distanceTo(region.vertices()[i], nextPos.getPoint());
                }
                Arrays.sort(ds);
                if((ds[0] + ds[1]) < SystemConstants.DRONE_MOVE_DISTANCE * 10 || lngLatHandler.isInRegion(nextPos.getPoint(), region)){
                    jumpReturn.setRegion(region);
                    jumpReturn.setPoint(currentPoint);
                    return jumpReturn;
                }
            }
            if(!lngLatHandler.isLngLat(nextPos.getPoint())){
                return null;
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
        double angle = Math.toDegrees(Math.atan2(p2.lat() - p1.lat(), p2.lng() - p1.lng()));
        angle = (angle + 360) % 360;

        // 2) Each of the 16 directions covers 360 / 16 = 22.5 degrees
        double increment = 22.5; // 22.5

        // 3) Divide, round to nearest integer, then multiply back
        double steps = Math.round(angle / increment);
        double snappedAngle = steps * increment;

        // Optional: if we want [0, 360) strictly, handle the edge case:
        // e.g., 360 => wrap back to 0
        if (snappedAngle >= 360) {
            snappedAngle -= 360.0;
        }
        if (snappedAngle < 0){
            snappedAngle += 360;
        }
        return snappedAngle;
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
            pathAL.add(p.getPoint());
            if(p.getParent() == null){
                break;
            }
            p = p.getParent();
        }
        pathAL.add(start.getPoint());
        Collections.reverse(pathAL);
        if(pathAL.size() > maxSteps) throw new Exception("Max steps meets");
        this.prettyPrintPath(pathAL);
        return pathAL.toArray(new LngLat[pathAL.size()]);
    }
    private static int orientation(LngLat p, LngLat q, LngLat r) {
        // (q.lat - p.lat)*(r.lng - q.lng) - (q.lng - p.lng)*(r.lat - q.lat)
        double val = (q.lat() - p.lat()) * (r.lng() - q.lng())
                - (q.lng() - p.lng()) * (r.lat() - q.lat());

        // If using doubles, consider a small epsilon
        if (Math.abs(val) < 1e-12) {
            return 0; // collinear
        }
        return (val > 0) ? 1 : 2;
    }
    private static boolean onSegment(LngLat p, LngLat q, LngLat r) {
        return (Math.min(p.lng(), r.lng()) <= q.lng() && q.lng() <= Math.max(p.lng(), r.lng())
                && Math.min(p.lat(), r.lat()) <= q.lat() && q.lat() <= Math.max(p.lat(), r.lat()));
    }

    private boolean intercept(LngLat p1, LngLat p2, LngLat e1, LngLat e2){
        // 1) Find the four orientations needed for general and special cases
        int o1 = orientation(p1, p2, e1);
        int o2 = orientation(p1, p2, e2);
        int o3 = orientation(e1, e2, p1);
        int o4 = orientation(e1, e2, p2);

        // 2) General case: If the two segments share different orientations
        if (o1 != o2 && o3 != o4) {
            return true;
        }

        // 3) Special case: p1, p2, p3 are collinear and p3 lies on p1->p2
        if (o1 == 0 && onSegment(p1, e1, p2)) return true;
        // p1, p2, p4 are collinear and p4 lies on p1->p2
        if (o2 == 0 && onSegment(p1, e2, p2)) return true;
        // p3, p4, p1 are collinear and p1 lies on p3->p4
        if (o3 == 0 && onSegment(e1, p1, e2)) return true;
        // p3, p4, p2 are collinear and p2 lies on p3->p4
        if (o4 == 0 && onSegment(e1, p2, e2)) return true;

        // Otherwise, they don't intersect
        return false;
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
            //System.out.println("Astar");
            pP p = frontier.poll();
            closedSet.add(p);
            if(frontier.size() >= maxSteps * 2){
                throw new Exception("Max steps reached");
            }
            if(p.getPoint() == null){
                throw new Exception("Point is null");
            }
            if(lngLatHandler.isCloseTo(p.getPoint(), end)){
                return reconstructPathJPS(p, startpP, maxSteps);
            }
            jumpReturn jp = jump(p, end, newZones.toArray(new NamedRegion[0]), stayInZone);
            if(jp == null ||(jp.getPoint() == null && jp.getRegion() == null)){
                throw new Exception("Path not found");
            }
            if(jp.getPoint() == null){
                throw new Exception("Path not found");
            }
            p = jp.getPoint();
            if(lngLatHandler.isCloseTo(p.getPoint(), end)){
                return reconstructPathJPS(p, startpP, maxSteps);
            }
            NamedRegion blocked = null;
            if(jp.getRegion() != stayInZone){
                blocked = jp.getRegion();
            }
            double angle = getAngle(p.getPoint(), end);
            boolean in = lngLatHandler.isInRegion(p.getPoint(), stayInZone);
            for(double i = angle - 112.5; i <= angle + 112.5; i += 22.5){
            //for(double i = 0; i < 360; i += 22.5){
                pP nextP = new pP(lngLatHandler.nextPosition(p.getPoint(), i));
                if(!lngLatHandler.isLngLat(nextP.getPoint())){
                    throw new Exception("Next point is not a lng lat");
                }
                if(blocked != null && lngLatHandler.isInRegion(nextP.getPoint(), blocked)) continue;
                if(in && !lngLatHandler.isInRegion(nextP.getPoint(), stayInZone)) continue;
                if(blocked != null){
                    boolean intercept = false;
                    for(int j = 0; j < blocked.vertices().length - 1; j++){
                        if(this.intercept(p.getPoint(), nextP.getPoint(), blocked.vertices()[j], blocked.vertices()[j+1])) {
                            intercept = true;
                            break;
                        }
                    }
                    if(intercept) continue;
                }
                //System.out.println("visiting:" + nextP.getPoint().toString());
                if(closedSet.contains(nextP)) continue;
                double tentativeG = p.getG() + SystemConstants.DRONE_MOVE_DISTANCE;
                if(tentativeG < nextP.getG() || !frontier.contains(nextP)){
                    nextP.setG(tentativeG);
                    nextP.setH(lngLatHandler.distanceTo(nextP.getPoint(), end) * 1.5);
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
