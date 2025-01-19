package uk.ac.ed.inf.utils;

import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.data.Pizza;
import uk.ac.ed.inf.data.Restaurant;
import uk.ac.ed.inf.constant.SystemConstants;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OrderHandler implements uk.ac.ed.inf.interfaces.OrderValidation {
    private final int maximumPizzas = 4;

    public OrderHandler() {
    }

    //reference: https://rosettacode.org/wiki/Luhn_test_of_credit_card_numbers#Java
    private static boolean luhnTest(String number) {
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {//this is for odd digits, they are 1-indexed in the algorithm
                s1 += digit;
            } else {//add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        return (s1 + s2) % 10 == 0;
    }

    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        if (orderToValidate.getPizzasInOrder().length == 0) {
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.EMPTY_ORDER);
            return orderToValidate;
        }
        if (orderToValidate.getPizzasInOrder().length > this.maximumPizzas) {
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            return orderToValidate;
        }
        String cn = orderToValidate.getCreditCardInformation().getCreditCardNumber();
        if (!luhnTest(cn) || !(cn.length() == 16)) {
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            return orderToValidate;
        }
        String expD = orderToValidate.getCreditCardInformation().getCreditCardExpiry();
        YearMonth today = YearMonth.now();
        if (!Pattern.matches("(0?[1-9]|1?[0-2])/\\d{2}\\b", expD)
                || today.isAfter(YearMonth.parse(expD, DateTimeFormatter.ofPattern("MM/yy")))) {
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            return orderToValidate;
        }
        if (!Pattern.matches("\\d{3}", orderToValidate.getCreditCardInformation().getCvv())) {
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            return orderToValidate;
        }
        Restaurant restaurant = null;
        for(Pizza pizza: orderToValidate.getPizzasInOrder()) {
            boolean found = false;
            for(Restaurant r: definedRestaurants) {
                for (Pizza pizzaM : r.menu()) {
                    if (pizzaM.name().equals(pizza.name())) {
                        found = true;
                        if(restaurant == null) {
                            restaurant = r;
                        }else{
                            if(restaurant != r){
                                orderToValidate.setOrderStatus(OrderStatus.INVALID);
                                orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
                                return orderToValidate;
                            }
                        }
                        if(pizza.priceInPence() != pizzaM.priceInPence()) {
                            orderToValidate.setOrderStatus(OrderStatus.INVALID);
                            orderToValidate.setOrderValidationCode(OrderValidationCode.PRICE_FOR_PIZZA_INVALID);
                            return orderToValidate;
                        }
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                orderToValidate.setOrderStatus(OrderStatus.INVALID);
                orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
                return orderToValidate;
            }
        }
        if(restaurant == null){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            return orderToValidate;
        }
        if(Stream
                .of(restaurant.openingDays())
                .noneMatch(day -> orderToValidate.getOrderDate().getDayOfWeek() == day)){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
            return orderToValidate;
        }
        int total = Stream.of(orderToValidate.getPizzasInOrder())
                .mapToInt(Pizza::priceInPence).sum() + SystemConstants.ORDER_CHARGE_IN_PENCE;
        if(total != orderToValidate.getPriceTotalInPence()){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            return orderToValidate;
        }
        orderToValidate.setOrderStatus(OrderStatus.VALID);
        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        return orderToValidate;
    }
}
