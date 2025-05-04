//package com.example.plagiarism1.util;
//
//import com.example.plagiarism1.TypeDeRole;
//import com.example.plagiarism1.model.Role;
//import com.example.plagiarism1.repository.RoleRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RoleInitializer implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//
//    public RoleInitializer(RoleRepository roleRepository) {
//        this.roleRepository = roleRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        for (TypeDeRole type : TypeDeRole.values()) {
//            roleRepository.findByLibelle(type).orElseGet(() -> {
//                Role role = new Role();
//                role.setLibelle(type);
//                return roleRepository.save(role);
//            });
//        }
//    }
//}
