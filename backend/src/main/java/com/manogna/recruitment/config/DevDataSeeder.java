package com.manogna.recruitment.config;

import com.manogna.recruitment.entity.*;
import com.manogna.recruitment.repository.CompanyRepository;
import com.manogna.recruitment.repository.JobRepository;
import com.manogna.recruitment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Runs once on startup (dev profile only) so you can hit the API immediately
 * without manually registering users first. Demo login credentials for all
 * three accounts below are "password123".
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository, CompanyRepository companyRepository,
                          JobRepository jobRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        String password = passwordEncoder.encode("password123");

        Company company = companyRepository.save(new Company("Acme Corp"));

        User recruiter = new User("Priya Recruiter", "recruiter@acme.com", password, Role.RECRUITER);
        recruiter.setCompany(company);
        userRepository.save(recruiter);

        User student = new User("Asha Student", "student@example.com", password, Role.STUDENT);
        userRepository.save(student);

        User admin = new User("Admin User", "admin@example.com", password, Role.ADMIN);
        userRepository.save(admin);

        Job job = new Job();
        job.setTitle("Programmer Analyst Intern");
        job.setDescription("Work on large scale, highly available applications alongside a technical mentor.");
        job.setLocation("Hyderabad, India");
        job.setSkills("Java, Data Structures, Algorithms, AWS");
        job.setApplicationDeadline(LocalDate.now().plusDays(30));
        job.setStatus(JobStatus.OPEN);
        job.setCompany(company);
        job.setPostedBy(recruiter);
        jobRepository.save(job);

        log.info("=== Dev data seeded ===");
        log.info("Recruiter login: recruiter@acme.com / password123");
        log.info("Student login:   student@example.com / password123");
        log.info("Admin login:     admin@example.com / password123");
        log.info("Seeded job id={} '{}'", job.getId(), job.getTitle());
    }
}
