package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.CustomerDTO;
import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CustomerRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public CustomerDTO createMyProfile(String username, CustomerDTO dto) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }

        User user = optionalUser.get();

        Optional<Customer> existing = customerRepository.findByUser_UserId(user.getUserId());
        if (existing.isPresent()) {
            throw new CustomException(409, "Customer profile already exists for this user");
        }

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setUser(user);

        Customer saved = customerRepository.save(customer);


        return new CustomerDTO(saved.getCustomerId(), saved.getName(), saved.getPhoneNumber(), saved.getUser().getUserId());
    }

    @Override
    public CustomerDTO getMyProfile(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }

        Optional<Customer> optionalCustomer = customerRepository.findByUser_UserId(optionalUser.get().getUserId());

        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer profile not found");
        }

        Customer customer = optionalCustomer.get();
        return new CustomerDTO(
                customer.getCustomerId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getUser().getUserId());


    }

    @Override
    public CustomerDTO createWalkIn(CustomerDTO dto) {

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setPhoneNumber(dto.getPhoneNumber());

        Customer save = customerRepository.save(customer);
        return new CustomerDTO(save.getCustomerId(), save.getName(), save.getPhoneNumber(), null);


    }

}
