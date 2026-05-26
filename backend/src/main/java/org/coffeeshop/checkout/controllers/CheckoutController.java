package org.coffeeshop.checkout.controllers;

import org.coffeeshop.checkout.dtos.HorsePayRequestDto;
import org.coffeeshop.checkout.dtos.HorsePayResponseDto;
import org.coffeeshop.checkout.services.HorsePayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for checkout and payment operations. */
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final HorsePayService horsePayService;

    /** Creates a CheckoutController with the required HorsePay service. */
    public CheckoutController(HorsePayService horsePayService) {
        this.horsePayService = horsePayService;
    }

    /** Submits a payment request to the HorsePay gateway. */
    @PostMapping("/pay")
    public ResponseEntity<HorsePayResponseDto> pay(@RequestBody HorsePayRequestDto requestDto) {
        HorsePayResponseDto response = horsePayService.processPayment(requestDto);
        return ResponseEntity.ok(response);
    }
}
