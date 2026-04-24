package com.sky.subscription.service;

import com.sky.subscription.dto.PaymentDto;
import com.sky.subscription.entity.Payment;
import com.sky.subscription.entity.PaymentStatus;
import com.sky.subscription.entity.Subscription;
import com.sky.subscription.exception.ResourceNotFoundException;
import com.sky.subscription.repository.PaymentRepository;
import com.sky.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PaymentDto getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return toDto(payment);
    }

    public List<PaymentDto> getPaymentsBySubscriptionId(Integer subscriptionId) {
        return paymentRepository.findBySubscriptionId(subscriptionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getOverduePayments() {
        return paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.overdue, LocalDate.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByDueDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PaymentDto createPayment(PaymentDto dto) {
        Subscription subscription = subscriptionRepository.findById(dto.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", dto.getSubscriptionId()));
        
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(dto.getAmount())
                .dueDate(dto.getDueDate())
                .paidAt(dto.getPaidAt())
                .status(dto.getStatus())
                .build();
        
        Payment saved = paymentRepository.save(payment);
        return toDto(saved);
    }

    public PaymentDto updatePayment(Integer id, PaymentDto dto) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        if (dto.getAmount() != null) {
            existing.setAmount(dto.getAmount());
        }
        if (dto.getDueDate() != null) {
            existing.setDueDate(dto.getDueDate());
        }
        existing.setPaidAt(dto.getPaidAt());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        
        Payment saved = paymentRepository.save(existing);
        return toDto(saved);
    }

    public PaymentDto markAsPaid(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        payment.setStatus(PaymentStatus.paid);
        payment.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));
        
        Payment saved = paymentRepository.save(payment);
        return toDto(saved);
    }

    public void deletePayment(Integer id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        paymentRepository.deleteById(id);
    }

    private PaymentDto toDto(Payment entity) {
        return PaymentDto.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscription().getId())
                .amount(entity.getAmount())
                .dueDate(entity.getDueDate())
                .paidAt(entity.getPaidAt())
                .status(entity.getStatus())
                .build();
    }
}
