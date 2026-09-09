package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.CustomerDTO;
import com.ijse.codlink.dto.CustomerLinkDTO;
import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CustomerRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public CustomerDTO updateMyProfile(String username, CustomerDTO dto) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }

        Optional<Customer> optionalCustomer = customerRepository.findByUser_UserId(optionalUser.get().getUserId());

        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer profile not found");
        }
        Customer customer = optionalCustomer.get();

        if (dto.getName() != null)
            customer.setName(dto.getName());

        if (dto.getPhoneNumber() != null)
            customer.setPhoneNumber(dto.getPhoneNumber());

        Customer saved = customerRepository.save(customer);

        return new CustomerDTO(
                saved.getCustomerId(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getUser().getUserId());

    }

    @Override
    public CustomerDTO getById(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer not found");
        }
        Customer c = optionalCustomer.get();
        return new CustomerDTO(c.getCustomerId(), c.getName(), c.getPhoneNumber(),
                c.getUser() != null ? c.getUser().getUserId() : null);
    }

    @Override
    public List<CustomerDTO> getAll() {
        List<CustomerDTO> list = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {

            list.add(new CustomerDTO(c.getCustomerId(), c.getName(), c.getPhoneNumber(),
                    c.getUser() != null ? c.getUser().getUserId() : null));
        }
        return list;
    }

    @Override
    public void delete(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer not found");
        }
        try {
            customerRepository.delete(optionalCustomer.get());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(409, "Cannot delete — this customer is still referenced by parcels or shipments. Remove or reassign those first.");
        }
    }

    @Override
    public CustomerDTO linkToMyAccount(String username, CustomerLinkDTO dto) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }
        User user = optionalUser.get();

        Optional<Customer> alreadyLinked = customerRepository.findByUser_UserId(user.getUserId());
        if (alreadyLinked.isPresent()) {
            throw new CustomException(409, "This account already has a customer profile");
        }

        if (dto.getCustomerId() == null || dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
            throw new CustomException(400, "Customer ID and phone number are both required");
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(dto.getCustomerId());


        boolean matchFound = optionalCustomer.isPresent()
                && optionalCustomer.get().getUser() == null
                && dto.getPhoneNumber().trim().equals(
                optionalCustomer.get().getPhoneNumber() == null ? "" : optionalCustomer.get().getPhoneNumber().trim());

        if (!matchFound) {
            throw new CustomException(404, "No unlinked customer record matches that ID and phone number");
        }

        Customer customer = optionalCustomer.get();
        customer.setUser(user);
        Customer saved = customerRepository.save(customer);

        return new CustomerDTO(saved.getCustomerId(), saved.getName(), saved.getPhoneNumber(), saved.getUser().getUserId());
    }

}
