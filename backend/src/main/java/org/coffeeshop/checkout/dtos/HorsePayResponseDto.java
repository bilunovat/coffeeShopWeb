package org.coffeeshop.checkout.dtos;

/** Response payload received from the HorsePay payment gateway. */
public record HorsePayResponseDto(
        String storeId,
        String customerId,
        String date,
        String time,
        String timeZone,
        double transactionAmount,
        String currencyCode,
        @com.fasterxml.jackson.annotation.JsonProperty("paymentSuccess")
                PaymentSuccess paymentSuccess) {
    public record PaymentSuccess(Boolean status, String reason) {}
}
