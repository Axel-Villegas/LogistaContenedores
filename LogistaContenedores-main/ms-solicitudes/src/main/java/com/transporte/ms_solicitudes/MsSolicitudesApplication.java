package com.transporte.ms_solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
@EnableDiscoveryClient
public class MsSolicitudesApplication {
	public static void main(String[] args) {
		SpringApplication.run(MsSolicitudesApplication.class, args);
	}

}
