package org.example;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Validator for verifying the correctness of an order.
 */
public class OrderValidator implements OrderValidation {

    private static final Logger LOGGER = Logger.getLogger(OrderValidator.class.getName());

    public OrderValidator() {
        // Constructor for OrderValidator
    }

    /**
     * Validates the provided order against a list of defined restaurants.
     *
     * @param order             The order to be validated.
     * @param definedRestaurants List of defined restaurants.
     * @return The validated order, updated with the appropriate status and validation code.
     */
    public Order validateOrder(Order order, Restaurant[] definedRestaurants) {
        int totalPizzaPriceInPence = SystemConstants.ORDER_CHARGE_IN_PENCE;
        LocalDate localDate = order.getOrderDate();
        Set<Restaurant> restaurantsWithOrderedPizza = new HashSet<>();

        // Check if order exceeds the maximum allowed pizzas
        if (order.getPizzasInOrder().length > SystemConstants.MAX_PIZZAS_PER_ORDER) {
            setInvalidOrder(order, OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            return order;
        }

        // Validate credit card information
        if (!isValidCreditCard(order, order.getCreditCardInformation())) {
            return order;
        }

        // Check if ordered pizzas are available in defined restaurants
        for (Pizza orderedPizza : order.getPizzasInOrder()) {
            boolean pizzaFound = false;
            for (Restaurant restaurant : definedRestaurants) {
                for (Pizza pizzaOnMenu : restaurant.menu()) {
                    if (Objects.equals(pizzaOnMenu.name(), orderedPizza.name())) {
                        pizzaFound = true;
                        // Check if restaurant is open on the order date
                        if (!Arrays.asList(restaurant.openingDays()).contains(localDate.getDayOfWeek())) {
                            setInvalidOrder(order, OrderValidationCode.RESTAURANT_CLOSED);
                            return order;
                        }
                        totalPizzaPriceInPence += pizzaOnMenu.priceInPence();
                        restaurantsWithOrderedPizza.add(restaurant);
                        break;
                    }
                }
            }
            if (!pizzaFound) {
                setInvalidOrder(order, OrderValidationCode.PIZZA_NOT_DEFINED);
                return order;
            }
        }

        // Check if pizzas are ordered from multiple restaurants
        if (restaurantsWithOrderedPizza.size() > 1) {
            setInvalidOrder(order, OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            return order;
        }

        // Check if the total price matches the sum of individual pizza prices
        if (order.getPriceTotalInPence() != totalPizzaPriceInPence) {
            setInvalidOrder(order, OrderValidationCode.TOTAL_INCORRECT);
            return order;
        }

        // If all checks pass, set order as valid
        order.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        order.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        return order;
    }

    /**
     * Sets the order status to INVALID and assigns the given validation code.
     *
     * @param order The order to be updated.
     * @param code  The validation code indicating the reason for invalidation.
     */
    private void setInvalidOrder(Order order, OrderValidationCode code) {
        order.setOrderStatus(OrderStatus.INVALID);
        order.setOrderValidationCode(code);
    }

    /**
     * Validates the provided credit card information.
     *
     * @param order    The order associated with the credit card.
     * @param cardInfo The credit card information to be validated.
     * @return true if the credit card is valid, false otherwise.
     */
    private boolean isValidCreditCard(Order order, CreditCardInformation cardInfo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        // Check if credit card is expired
        try {
            if (LocalDate.parse("01/" + cardInfo.getCreditCardExpiry(), formatter).isBefore(LocalDate.now())) {
                setInvalidOrder(order, OrderValidationCode.EXPIRY_DATE_INVALID);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        // Validate CVV format
        if (!cardInfo.getCvv().matches("^[0-9]{3}$")) {
            setInvalidOrder(order, OrderValidationCode.CVV_INVALID);
            return false;
        }
        // Validate credit card number format
        else if (!cardInfo.getCreditCardNumber().matches("^[0-9]{16}$")) {
            setInvalidOrder(order, OrderValidationCode.CARD_NUMBER_INVALID);
            return false;
        }
        return true;
    }

    /**
     * Validates a set of orders for a given day against defined restaurants.
     *
     * @param orders An array of orders to be validated.
     * @param restaurants The defined restaurants to validate against.
     * @return An array of validated orders.
     */
    public static Order[] validateDailyOrders(Order[] orders, Restaurant[] restaurants) {
        ArrayList<Order> validatedOrders = new ArrayList<>();

        OrderValidator orderValidator = new OrderValidator();
        for (Order order : orders) {
            Order validatedOrder = orderValidator.validateOrder(order, restaurants);
            validatedOrders.add(validatedOrder);
        }

        return validatedOrders.toArray(new Order[0]);
    }
}