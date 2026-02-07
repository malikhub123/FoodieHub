package com.project.FoodieHub;

import com.project.FoodieHub.email_notification.dtos.NotificationDTO;
import com.project.FoodieHub.email_notification.services.NotificationService;
import com.project.FoodieHub.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class FoodieHubApplication {

//	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FoodieHubApplication.class, args);
	}
//	@Bean
//	CommandLineRunner runner(NotificationService notificationService){
//		return  args ->{
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("malik2002.aditi@gmail.com")
//					.subject("Hello")
//					.body("this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO);
//		};
//	}


}
