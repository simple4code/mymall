package com.hzc.mymall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 15:49
 */
@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page) {
        return page;
    }
}
