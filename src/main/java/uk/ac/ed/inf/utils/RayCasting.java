package uk.ac.ed.inf.utils;

import java.math.BigDecimal;
import java.math.MathContext;

//reference: https://rosettacode.org/wiki/Ray-casting_algorithm#Java
//modified to BigDecimal
public class RayCasting {
    static boolean intersects(BigDecimal[] A, BigDecimal[] B, BigDecimal[] P) {
        //System.out.println(A[0].toString() + "," + A[1].toString() + ";" + B[0].toString() + "," + B[1].toString());
        // make Ay < By
        if (A[1].compareTo(B[1]) > 0)
            return intersects(B, A, P);

        // if Py == Ay, Py + Err, make P inside
        if (P[1].compareTo(A[1]) == 0) {
            P[1] = P[1].add(BigDecimal.valueOf(0.000001));
            //System.out.println(P[1]);
        }

        // if Py == By, Py - Err, make P inside
        if (P[1].compareTo(B[1]) == 0) {
            P[1] = P[1].subtract(BigDecimal.valueOf(0.000001));
            //System.out.println(P[1]);
        }

        // if Py > By or Py < Ay or Px > max(Ax, Bx), return false 1
        if (P[1].compareTo(B[1]) > 0 || P[1].compareTo(A[1]) < 0
                || P[0].compareTo(BigDecimal.valueOf(Math.max(A[0].doubleValue(), B[0].doubleValue()))) > 0) {
            //System.out.println("1");
            return false;
        }

        // if P not above and < min(Ax, Bx), return true 2
        if (P[0].compareTo(BigDecimal.valueOf(Math.min(A[0].doubleValue(), B[0].doubleValue()))) < 0) {
            //System.out.println("2");
            return true;
        }

        // compare area 3
        try{
            BigDecimal red = P[1].subtract(A[1]).divide(P[0].subtract(A[0]), MathContext.DECIMAL64);
            BigDecimal blue = B[1].subtract(A[1]).divide(B[0].subtract(A[0]), MathContext.DECIMAL64);
            //System.out.println("3: " + red + "," + blue);
            return red.compareTo(blue) >= 0;
        }catch(ArithmeticException exp){
            System.out.println("[ERROR] SOURCE = RayCasting; " + exp);
            return false;
        }
    }
}
