package org.coffeeshop.checkout.dtos;

/** Request payload sent to the HorsePay payment gateway. */
public record HorsePayRequestDto(
        String storeID,
        String customerID,
        String date,
        String time,
        String timeZone,
        double transactionAmount,
        String currencyCode,
        Boolean forcePaymentStatusReturnType) {}
