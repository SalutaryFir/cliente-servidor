package com.universidad.chat.cliente;

import com.universidad.chat.cliente.repository.MensajeCacheRepository; // <-- Importar
import com.universidad.chat.cliente.service.NetworkService;
import com.universidad.chat.cliente.vista.LoginVista;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class ClienteApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(ClienteApplication.class);
		ConfigurableApplicationContext context = builder.headless(false).run(args);

		SwingUtilities.invokeLater(() -> {
			// Obtenemos los servicios que necesitamos del contexto de Spring
			NetworkService networkService = context.getBean(NetworkService.class);
			MensajeCacheRepository cacheRepository = context.getBean(MensajeCacheRepository.class); // <-- Obtenemos el repositorio

			// Pasamos AMBOS servicios a la vista de Login
			LoginVista loginVista = new LoginVista(networkService, cacheRepository);
			loginVista.setVisible(true);
		});
	}
}