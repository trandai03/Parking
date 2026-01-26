package com.project.parking.service;

import com.project.parking.dto.CardDTO;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Card;
import com.project.parking.model.User;
import com.project.parking.model.Vehicle;
import com.project.parking.repository.CardRepository;
import com.project.parking.repository.UserRepository;
import com.project.parking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public List<CardDTO> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getCardById(Long id) throws DataNotFoundException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Card not found with id: " + id));
        return convertToDTO(card);
    }

    public CardDTO getCardByCardNumber(String cardNumber) throws DataNotFoundException {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new DataNotFoundException("Card not found with number: " + cardNumber));
        return convertToDTO(card);
    }

    public List<CardDTO> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getCardsByVehicleId(Long vehicleId) {
        return cardRepository.findByVehicleId(vehicleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getCardsByStatus(String status) {
        return cardRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CardDTO createCard(CardDTO cardDTO) throws DataNotFoundException {
        User user = null;
        if (cardDTO.getUserId() != null) {
            user = userRepository.findById(cardDTO.getUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found with id: " + cardDTO.getUserId()));
        }

        Vehicle vehicle = null;
        if (cardDTO.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(cardDTO.getVehicleId())
                    .orElseThrow(() -> new DataNotFoundException("Vehicle not found with id: " + cardDTO.getVehicleId()));
        }

        Card card = new Card();
        card.setCardNumber(cardDTO.getCardNumber() != null ? cardDTO.getCardNumber() : generateCardNumber());
        card.setCardType(cardDTO.getCardType());
        card.setUser(user);
        card.setVehicle(vehicle);
        card.setValidFrom(cardDTO.getValidFrom() != null ? cardDTO.getValidFrom() : LocalDateTime.now());
        card.setValidTo(cardDTO.getValidTo());
        card.setStatus(cardDTO.getStatus() != null ? cardDTO.getStatus() : "ACTIVE");
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    public CardDTO updateCard(Long id, CardDTO cardDTO) throws DataNotFoundException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Card not found with id: " + id));

        if (cardDTO.getCardNumber() != null) {
            card.setCardNumber(cardDTO.getCardNumber());
        }
        if (cardDTO.getCardType() != null) {
            card.setCardType(cardDTO.getCardType());
        }
        if (cardDTO.getUserId() != null) {
            User user = userRepository.findById(cardDTO.getUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found with id: " + cardDTO.getUserId()));
            card.setUser(user);
        }
        if (cardDTO.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(cardDTO.getVehicleId())
                    .orElseThrow(() -> new DataNotFoundException("Vehicle not found with id: " + cardDTO.getVehicleId()));
            card.setVehicle(vehicle);
        }
        if (cardDTO.getValidFrom() != null) {
            card.setValidFrom(cardDTO.getValidFrom());
        }
        if (cardDTO.getValidTo() != null) {
            card.setValidTo(cardDTO.getValidTo());
        }
        if (cardDTO.getStatus() != null) {
            card.setStatus(cardDTO.getStatus());
        }
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        return convertToDTO(updatedCard);
    }

    public void deleteCard(Long id) throws DataNotFoundException {
        if (!cardRepository.existsById(id)) {
            throw new DataNotFoundException("Card not found with id: " + id);
        }
        cardRepository.deleteById(id);
    }

    private CardDTO convertToDTO(Card card) {
        return CardDTO.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .cardType(card.getCardType())
                .userId(card.getUser() != null ? card.getUser().getId() : null)
                .userName(card.getUser() != null ? card.getUser().getUsername() : null)
                .vehicleId(card.getVehicle() != null ? card.getVehicle().getId() : null)
                .licensePlate(card.getVehicle() != null ? card.getVehicle().getLicensePlate() : null)
                .validFrom(card.getValidFrom())
                .validTo(card.getValidTo())
                .status(card.getStatus())
                .build();
    }

    private String generateCardNumber() {
        return "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}