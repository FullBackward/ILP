package uk.ac.ed.inf.data;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.constant.OrderStatus;

public class OrderValidationResult {
    private OrderValidationCode orderValidationCode;
    private OrderStatus orderStatus;
    public OrderValidationResult(OrderValidationCode orderValidationCode, OrderStatus orderStatus){}
    public OrderValidationCode getOrderValidationCode() {return this.orderValidationCode;}
    public OrderStatus getOrderStatus() {return this.orderStatus;}
    public void setOrderValidationCode(OrderValidationCode orderValidationCode){this.orderValidationCode = orderValidationCode;}
    public void setOrderStatus(OrderStatus orderStatus){this.orderStatus = orderStatus;}
}
