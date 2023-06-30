package project.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import project.server.services.train.TrainService;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ServerApplication {
	public static List<String> trainStations;
	public static List<Integer> trainNos;
	public static List<String> premiumSeats;
	public static List<String> standardSeats;


	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);
		List<String> trainStations = context.<TrainService>getBean(TrainService.class).getAllTranStations();
		List<Integer> trainNos = context.<TrainService>getBean(TrainService.class).getAllTrainNos();
		List<String> premiumSeats = context.<TrainService>getBean(TrainService.class).getPremiumSeats();
		List<String> standardSeats = context.<TrainService>getBean(TrainService.class).getStandardSeats();
		System.out.println("premiumSeats = " + premiumSeats.size());
		System.out.println("standardSeats.size() = " + standardSeats.size());

		ServerApplication.premiumSeats = Collections.unmodifiableList(premiumSeats);
		ServerApplication.standardSeats = Collections.unmodifiableList(standardSeats);
		ServerApplication.trainStations = Collections.unmodifiableList(trainStations);
		ServerApplication.trainNos = Collections.unmodifiableList(trainNos);

	}




}
