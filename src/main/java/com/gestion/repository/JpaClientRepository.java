//package com.gestion.repository;
//
//import com.gestion.model.Client;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface JpaClientRepository extends JpaRepository<Client, Long> {
//
//    boolean existsClientByDni(String dni);
//
//    Optional<Client> findByDni(String dni);
//}
