package project.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import project.server.entities.train.TrainNoEntity;
import project.server.entities.train.TrainStationEntity;
import project.server.services.train.TrainService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ServerApplication {
	public static List<String> trainStations;
	public static List<Integer> trainNos;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);
		List<String> trainStations = context.<TrainService>getBean(TrainService.class).getAllTranStations();
		List<Integer> trainNos = context.<TrainService>getBean(TrainService.class).getAllTrainNos();

		ServerApplication.trainStations = Collections.unmodifiableList(trainStations);
		ServerApplication.trainNos = Collections.unmodifiableList(trainNos);

	}




}
