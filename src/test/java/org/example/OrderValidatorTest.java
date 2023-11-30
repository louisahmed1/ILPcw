package org.example;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class OrderValidatorTest extends TestCase {

    public OrderValidatorTest (String testName) {
        super (testName);
    }

    public static Test suite() { return new TestSuite( OrderValidatorTest.class); }

    private static JsonParser jsonParser = new JsonParser();
    Pizza pizza1 = new Pizza("Calzone",1400);
    Pizza pizza2 = new Pizza("Margarita", 1000);
    Pizza pizza3 = new Pizza("Meat Lover", 1400);
    Pizza pizza4 = new Pizza("Vegan Delight", 1100);

    CreditCardInformation cardValid = new CreditCardInformation("1234123412341234", "09/25", "123");
    CreditCardInformation cardInvalidCvv = new CreditCardInformation("4321432143214321", "04/26", "30");
    CreditCardInformation cardInvalidCardNumber = new CreditCardInformation("098709870987098", "03/25", "235");
    CreditCardInformation cardInvalidExpDate = new CreditCardInformation("3334333433343334", "01/10", "305");

    Restaurant restaurantCiverinos = new Restaurant("Civerinos Slice", new LngLat(-3.1912869215011597,55.945535152517735), new DayOfWeek[] { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}, new Pizza[]{pizza1, pizza2});
    Restaurant restaurantSoraLella = new Restaurant("Sora Lella Vegan Restaurant", new LngLat(-3.202541470527649, 55.943284737579376), new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}, new Pizza[]{pizza3, pizza4});

    private static Order validOrder;
    private static OrderValidator orderValidator;
    private static Restaurant[] restaurants;

    @Override
    protected void setUp() throws Exception {
        //day is friday
        //2 pizzas from restaurantCiverinos {pizza1, pizza2}
        //total price = 2500 (1400 + 1000 + 100)
        validOrder = new Order("123", LocalDate.of(2023,12,15), OrderStatus.UNDEFINED, OrderValidationCode.UNDEFINED, 2500, new Pizza[]{pizza1, pizza2}, cardValid);
        orderValidator = new OrderValidator();
        Path path = Paths.get("restaurants");
        //if (Files.exists(path)) {
        //    restaurants = JsonParser.parseRestaurant("restaurants");
        //} else {
        restaurants = new Restaurant[]{restaurantCiverinos, restaurantSoraLella};
        //}

    }

    public void testValid() {
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.NO_ERROR);
        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.VALID_BUT_NOT_DELIVERED);
    }

    public void testRestaurantClosed() {
        validOrder.setOrderDate(LocalDate.of(2023,12,14));
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.RESTAURANT_CLOSED);
        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
    }

    public void testCardCvv() {
        validOrder.setCreditCardInformation(cardInvalidCvv);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.CVV_INVALID);
        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
    }

    public void testCardNumber() {
        validOrder.setCreditCardInformation(cardInvalidCardNumber);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.CARD_NUMBER_INVALID);
    }

    public void testCardExpDate() {
        validOrder.setCreditCardInformation(cardInvalidExpDate);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.EXPIRY_DATE_INVALID);

    }

    public void testTotalIncorrect() {
        validOrder.setPriceTotalInPence(2499);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.TOTAL_INCORRECT);
    }

    public void testTotalIncorrect2() {
        Pizza pizza1Fake = new Pizza("Calzone", 1100);
        validOrder.setPizzasInOrder(new Pizza[]{pizza1Fake, pizza2});

        validOrder.setPriceTotalInPence(2200);
        Order orderValidated2 = orderValidator.validateOrder(validOrder, restaurants);
        //validOrder.setPriceTotalInPence(2500);
        //Order orderValidated3 = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated2.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated2.getOrderValidationCode(), OrderValidationCode.TOTAL_INCORRECT);
        //Assert.assertSame(orderValidated3.getOrderStatus(), OrderStatus.INVALID);
        //Assert.assertSame(orderValidated3.getOrderValidationCode(), OrderValidationCode.TOTAL_INCORRECT);
    }

    public void testTotalCorrectMaybe() {
        Pizza pizza1Fake = new Pizza("Calzone", 1100);
        validOrder.setPizzasInOrder(new Pizza[]{pizza1Fake, pizza2});

        validOrder.setPriceTotalInPence(2500);
        Order orderValidated3 = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated3.getOrderValidationCode(), OrderValidationCode.NO_ERROR);
        Assert.assertSame(orderValidated3.getOrderStatus(), OrderStatus.VALID_BUT_NOT_DELIVERED);
    }

    public void testPizzaNotDefined() {
        Pizza fakePizza = new Pizza("Cheesy",2000);
        validOrder.setPizzasInOrder(new Pizza[]{ fakePizza });
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        validOrder.setPizzasInOrder(new Pizza[]{fakePizza, pizza1, pizza2});
        Order orderValidated2 = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.PIZZA_NOT_DEFINED);

        Assert.assertSame(orderValidated2.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated2.getOrderValidationCode(), OrderValidationCode.PIZZA_NOT_DEFINED);
    }

    public void testMaxPizzasExceeded() {
        validOrder.setPizzasInOrder(new Pizza[]{pizza1, pizza2, pizza1, pizza2, pizza1});
        validOrder.setPriceTotalInPence(6300);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
    }

    public void testMultipleRestaurants() {
        validOrder.setPizzasInOrder(new Pizza[]{pizza1, pizza3});
        validOrder.setPriceTotalInPence(2900);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);

        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
    }


    private Order[] dailyOrders;

    public void validateAll() throws IOException {
        LocalDate date1 = LocalDate.of(2023, 10, 10);
        this.dailyOrders = jsonParser.parseOrders(date1.toString());
    }

    public void testCardInfoDaily() throws IOException {
        //Invalid Card Number
        validOrder.setCreditCardInformation(cardInvalidCardNumber);
        Order orderValidated = orderValidator.validateOrder(validOrder, restaurants);
        Assert.assertSame(orderValidated.getOrderValidationCode(), OrderValidationCode.CARD_NUMBER_INVALID);
        Assert.assertSame(orderValidated.getOrderStatus(), OrderStatus.INVALID);

        //Invalid Cvv
        validOrder.setCreditCardInformation(cardInvalidCvv);
        Order orderValidated2 = orderValidator.validateOrder(validOrder, restaurants);
        Assert.assertSame(orderValidated2.getOrderValidationCode(), OrderValidationCode.CVV_INVALID);
        Assert.assertSame(orderValidated2.getOrderStatus(), OrderStatus.INVALID);

        //Invalid Expiry
        validOrder.setCreditCardInformation(cardInvalidExpDate);
        Order orderValidated3 = orderValidator.validateOrder(validOrder, restaurants);
        Assert.assertSame(orderValidated3.getOrderValidationCode(), OrderValidationCode.EXPIRY_DATE_INVALID);
        Assert.assertSame(orderValidated3.getOrderStatus(), OrderStatus.INVALID);
    }
}
