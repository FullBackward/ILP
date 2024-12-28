package uk.ac.ed.inf.utils;

import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.data.OrderValidationResult;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.data.Restaurant;

public class OrderHandler implements uk.ac.ed.inf.interfaces.OrderValidation {
    private int maximumPizzas = 4;
    public OrderHandler(){}
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        return null;
    }
}
