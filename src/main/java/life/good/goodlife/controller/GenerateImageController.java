package life.good.goodlife.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@RestController
public class GenerateImageController {
    @RequestMapping(path = "/generate", method = RequestMethod.GET)
    public ResponseEntity <?> generate(@RequestParam String cart, @RequestParam Integer balance) throws IOException {
        InputStream inputStream = Objects.requireNonNull(TestController.class.getClassLoader().getResource("static/index.jsp")).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer html = new StringBuffer();
        while (reader.ready()) {
            html.append(reader.readLine());
        }
        return ResponseEntity.ok(html);
    }
}