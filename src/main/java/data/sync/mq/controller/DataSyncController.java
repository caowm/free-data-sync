package data.sync.mq.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class DataSyncController {
	
	@RequestMapping("")
	public String status() {
		return "I'm OK";
	}
}
