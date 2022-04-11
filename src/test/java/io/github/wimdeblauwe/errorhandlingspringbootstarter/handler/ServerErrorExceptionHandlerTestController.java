package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ServerErrorExceptionHandlerTestController {

    @GetMapping("/path-variable")
    public void pathVariable(@PathVariable("id") String id) {
    }
}
