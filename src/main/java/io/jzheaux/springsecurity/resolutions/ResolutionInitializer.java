package io.jzheaux.springsecurity.resolutions;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResolutionInitializer implements SmartInitializingSingleton {
	private final ResolutionRepository resolutions;
	private final UserRepository userRepository;

	public ResolutionInitializer(ResolutionRepository resolutions, UserRepository userRepositoryin) {
		this.resolutions = resolutions;
		this.userRepository = userRepositoryin;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.resolutions.save(new Resolution("Read War and Peace", "user"));
		this.resolutions.save(new Resolution("Free Solo the Eiffel Tower", "user"));
		this.resolutions.save(new Resolution("Hang Christmas Lights", "user"));
//		User user = new User("user", "{bcrypt}$2a$10$gapS/EuYW0GtvZ8e3wcpguwffeOL1Fq1dX.wEY72n/mzpM3KcifKW");
		User user = new User();
		user.setUsername("user");
		user.setPassword("{bcrypt}$2a$10$gapS/EuYW0GtvZ8e3wcpguwffeOL1Fq1dX.wEY72n/mzpM3KcifKW");
		user.grantAuthority("resolution:read");
		user.grantAuthority("resolution:write");
		this.userRepository.save(user);

		User hasread = new User();
		hasread.setUsername("hasread");
		hasread.setPassword("{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
		hasread.grantAuthority("resolution:read");
		this.userRepository.save(hasread);
		User haswrite = new User();
		haswrite.setUsername("haswrite");
		haswrite.setPassword("{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
		haswrite.grantAuthority("resolution:write");
		this.userRepository.save(haswrite);
	}

}
