package com.ijse.codlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.context.annotation.EnableAspectJAutoProxy
@org.springframework.transaction.annotation.EnableTransactionManagement(order = 0)
public class CodlinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodlinkApplication.class, args);
	}

}
