package com.universidad.chat.servidor;

import com.universidad.chat.servidor.network.TCPServer; // Import TCPServer
import com.universidad.chat.servidor.service.ReporteService;
import com.universidad.chat.servidor.service.UsuarioService;
import com.universidad.chat.servidor.vista.ServidorVista; // Import the future GUI class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder; // Import Builder
import org.springframework.context.ConfigurableApplicationContext; // Import Context
import org.springframework.context.annotation.Bean;

import javax.swing.*; // Import Swing

@SpringBootApplication
public class ServidorApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(ServidorApplication.class)
				.headless(false)
				.run(args);

		SwingUtilities.invokeLater(() -> {
			TCPServer tcpServer = context.getBean(TCPServer.class);
			ReporteService reporteService = context.getBean(ReporteService.class);
			UsuarioService usuarioService = context.getBean(UsuarioService.class); // <-- Get UsuarioService

			// Pass all three services to the constructor
			ServidorVista vista = new ServidorVista(tcpServer, reporteService, usuarioService);
			vista.setVisible(true);
		});
	}


}