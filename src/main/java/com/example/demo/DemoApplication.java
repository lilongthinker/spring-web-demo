package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping(value = "/hello/{toWho}")
    public String block(@PathVariable String toWho) {
        return "hello to [" + toWho+"]";
    }
	@GetMapping(value = "/cpu/count")
	public String cpuCount() {
		int  coreCnt = Runtime.getRuntime().availableProcessors();
		return "core count  [" + coreCnt+"]。 from Runtime.getRuntime().availableProcessors()";
	}
        @GetMapping(value = "/")
        public String cpuCount() {
                return "/hello/lilong<br/> /cpu/count";
        }

	@GetMapping(value = "/cpu/count")
	public String cpuCount() {
		int  coreCnt = Runtime.getRuntime().availableProcessors();
		return "core count  [" + coreCnt+"]。 from Runtime.getRuntime().availableProcessors()";
	}

	@GetMapping(value = "/slow/{latency}")
	public String slowLatency(Long latency) {
		try {
			Thread.sleep(latency);
		} catch (InterruptedException e) {
			System.out.println("interrupted by something" + e.getStackTrace());
		}
		return "slow latency after ["+latency+"] mills.";
	}

}
