package com.ijse.codlink.repository;

import com.ijse.codlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long>{
}
