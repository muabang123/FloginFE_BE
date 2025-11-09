// java code để chạy thử backend với route /, sau khi update code sẽ bỏ....

package com.flogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
                <html>
                <head>
                    <title>Backend Test</title>
                    <style>
                        body {
                            background-color: #4CAF50; /* Màu xanh lá */
                            color: white;
                            font-family: Arial, sans-serif;
                            text-align: center;
                            margin-top: 20%;
                        }
                    </style>
                </head>
                <body>
                    <h1>✅ Backend đang chạy thành công!</h1>
                    <p>Trang này được trả về từ Spring Boot Controller.</p>
                </body>
                </html>
                """;
    }
}
