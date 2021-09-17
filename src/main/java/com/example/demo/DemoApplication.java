package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Joiner;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
@Slf4j
public class DemoApplication {

	@Value("${env:terway}")
	private String env = "app_default";

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



	@GetMapping(value = "/slow/{latency}")
	public String slowLatency(@PathVariable Long latency) {
		try {
			Thread.sleep(latency);
		} catch (InterruptedException e) {
			System.out.println("interrupted by something" + e.getStackTrace());
		}
		return "slow latency after ["+latency+"] mills.";
	}


	private static String getLinuxLocalIp()  {
		String ip = "";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String name = intf.getName();
				if (!name.contains("docker") && !name.contains("lo")) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							String ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::") && !ipaddress.contains("0:0:")
									&& !ipaddress.contains("fe80")) {
								ip = ipaddress;
							}
						}
					}
				}
			}
		} catch (SocketException ex) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public static String getRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		final String[] arr = ip.split(",");
		for (final String str : arr) {
			if (!"unknown".equalsIgnoreCase(str)) {
				ip = str;
				break;
			}
		}
		return ip;
	}

	@GetMapping(value = "/ip")
	public String ip(HttpServletRequest request) {
		return "pod ip: ["+getLinuxLocalIp()+"] <br/> client ip: ["+getRemoteIp(request)+"]";
	}


	@Value("${podName:unknown_pod_name}")
	private String podName = "";

	@GetMapping(value = "/pod/name")
	public String podName() {
		return "pod name: ["+podName+"]";
	}



	@RequestMapping("/headers")
	public String headers(@RequestHeader HttpHeaders headers){
		StringBuilder sbuilder = new StringBuilder();
		for(Map.Entry<String, List<String>> entry : headers.entrySet()){
			sbuilder.append(entry.getKey()).append(":").append( Joiner.on(",").join(entry.getValue())).append("---------------\n<br/>");
		}
		return sbuilder.toString();
	}

	private Map<Long, Byte[]> leakData = new HashMap<>();

	@RequestMapping("/leak")
	public String leak(){
		long current = System.nanoTime();
		Byte[] value = new Byte[1024*1024*8*2];
		leakData.put(current, value);
		log.info("leaking");
		return "leak once";
	}

	@RequestMapping("/")
	public String index(){
//        //no 生产环境禁止systemout
//        System.out.println("hello. log to stdout");
		//打印日志到文件,同时到标准输出
		log.info("hello.log to file. info");

		//设计异常信息,方便在arms里观察
		Long now = System.currentTimeMillis();
		if(now%10 == 0){
			throw new RuntimeException("10 nanos exception. this is by designed");
		}
		log.info("greeting from spring cloud. message in env:"+env);
		return "greeting from spring cloud. message in env:"+env+"\n";
	}

	@RequestMapping("/readiness/check")
	public String readinessCheck(){
		String msg = "readiness check it success!\n";
		log.info(msg);
		return msg;
	}

	@RequestMapping("/liveness/check")
	public String livenessCheck(){
		String msg = "liveness check it success!\n";
		log.info(msg);
		return msg;
	}

}
