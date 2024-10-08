package uk.ac.ed.inf.utils;

import java.math.BigDecimal;
import java.math.MathContext;

//reference: https://rosettacode.org/wiki/Ray-casting_algorithm#Java
//modified to BigDecimal
public class RayCasting {
    static boolean intersects(BigDecimal[] A, BigDecimal[] B, BigDecimal[] P) {
        if (A[1].compareTo(B[1]) > 0)
            return intersects(B, A, P);

        if (P[1].compareTo(A[1]) == 0 || P[1].compareTo(B[1]) == 0)
            P[1] = P[1].add(BigDecimal.valueOf(0.0001));

        if (P[1].compareTo(B[1]) > 0 || P[1].compareTo(A[1]) < 0
                || P[0].compareTo(BigDecimal.valueOf(Math.max(A[0].doubleValue(), B[0].doubleValue()))) >= 0)
            return false;

        if (P[0].compareTo(BigDecimal.valueOf(Math.min(A[0].doubleValue(), B[0].doubleValue()))) < 0)
            return true;
        try{
            BigDecimal red = P[1].subtract(A[1]).divide(P[0].subtract(A[0]), MathContext.DECIMAL64);
            BigDecimal blue = B[1].subtract(A[1]).divide(B[0].subtract(A[0]), MathContext.DECIMAL64);
            return red.compareTo(blue) >= 0;
        }catch(ArithmeticException exp){
            System.out.println("[ERROR] SOURCE = RayCasting; " + exp);
            return false;
        }
    }
}
