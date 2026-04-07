package com.villavredestein.config;

import com.villavredestein.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Sets contract file names for known residents on every startup (idempotent).
 * Only updates rows where contract_file is not yet set.
 */
@Component
@Order(10)
public class ContractSeeder implements ApplicationRunner {

    private static final Map<String, String> CONTRACTS = Map.of(
        "medocstaal@gmail.com",   "Woonafspraken 2023-Frankrijk-Medoc.pdf",
        "desmondstaal@gmail.com", "Woonafspraken 2022-Thailand-Desmond.pdf",
        "ikheetalvar@gmail.com",  "Woonafspraken 2024-Oekraine-Alvar.pdf",
        "arwenleonor@gmail.com",  "Woonafspraken 2026-Italie-Arwen.pdf",
        "simontalsma2@gmail.com", "PENSIONOVEREENKOMST 2024-Japan-SimonTalsma.pdf"
    );

    private final UserRepository userRepository;

    public ContractSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        CONTRACTS.forEach((email, filename) ->
            userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                if (user.getContractFile() == null) {
                    user.setContractFile(filename);
                    userRepository.save(user);
                }
            })
        );
    }
}