package utn.backend.tpi.tpi_flota_viajes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
	exclude = {
		// Excluir auto-configuración de RabbitMQ por defecto
		// Se puede habilitar configurando spring.rabbitmq.host en application.yml
		// y removiendo esta exclusión o usando @ConditionalOnProperty
		RabbitAutoConfiguration.class
	}
)
@EnableDiscoveryClient 
public class TpiFlotaViajesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TpiFlotaViajesApplication.class, args);
	}

}
