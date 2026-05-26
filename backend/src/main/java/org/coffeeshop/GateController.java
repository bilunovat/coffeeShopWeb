package org.coffeeshop;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simple health-check controller that verifies the application is running. */
@RestController
public class GateController {
    /**
     * Returns a static greeting string to confirm the application is reachable.
     *
     * @return a confirmation message
     */
    @GetMapping("/gate")
    public String gate() {
        return "CoffeeShop app";
    }
}
