package com.booking.app.services.impl;

import com.booking.app.dto.CodeConfirmationDto;
import com.booking.app.dto.HistoryDto;
import com.booking.app.dto.RegistrationDTO;
import com.booking.app.dto.RequestTicketsDto;
import com.booking.app.entity.ConfirmationCode;
import com.booking.app.entity.Role;
import com.booking.app.entity.SearchHistory;
import com.booking.app.entity.User;
import com.booking.app.enums.SocialProvider;
import com.booking.app.enums.TransportType;
import com.booking.app.exception.exception.InvalidConfirmationCodeException;
import com.booking.app.exception.exception.UserNotConfirmedException;
import com.booking.app.exception.exception.UserNotFoundException;
import com.booking.app.mapper.HistoryMapper;
import com.booking.app.mapper.UserMapper;
import com.booking.app.mapper.model.ArrivalCity;
import com.booking.app.mapper.model.DepartureCity;
import com.booking.app.repositories.HistoryRepository;
import com.booking.app.repositories.RoleRepository;
import com.booking.app.repositories.UserRepository;
import com.booking.app.services.ConfirmationCodeService;
import com.booking.app.services.TypeAheadService;
import com.booking.app.services.UkrainianCitiesService;
import com.booking.app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service implementation for managing user operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UkrainianCitiesService ukrainianCitiesService;
    private final TypeAheadService typeAheadService;
    private final ConfirmationCodeService confirmationCodeService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HistoryRepository historyRepository;

    private final PasswordEncoder passwordEncoder;

    private final HistoryMapper historyMapper;
    private final UserMapper userMapper;

    @Override
    public void confirmCode(CodeConfirmationDto dto) {
        findByEmail(dto.getEmail())
                .ifPresentOrElse(user -> {
                    if (!user.isEnabled()) {
                        verifyUser(dto, user);
                    }
                }, () -> {
                    throw new UserNotFoundException();
                });
    }

    @Override
    public void updateNotification(UUID uuid, boolean notification) {
        userRepository.updateByNotification(uuid, notification);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean isEnabled(User user) throws UserNotConfirmedException {
        if (user.isEnabled()) {
            return true;
        } else {
            throw new UserNotConfirmedException();
        }
    }

    @Override
    public void updatePassword(UUID uuid, String encodedPassword) {
        userRepository.updatePassword(uuid, encodedPassword);
        log.info("User with ID: {} has successfully updated password", uuid.toString());
    }

    @Override
    public void enableUser(User user) {
        userRepository.enableUser(user.getId());
        log.info("User with ID: {} has been enabled", user);
    }

    @Override
    public User saveUser(RegistrationDTO dto) {
        Role role = roleRepository.findByType(Role.RoleType.USER);
        ConfirmationCode confirmationCode = ConfirmationCode.createCode();
        User user = User.createBasicUser(
                role,
                confirmationCode, dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getUsername(), dto.getNotification()
        );
        user.setProvider(SocialProvider.LOCAL);
        return save(user);
    }

    @Override
    public User updateUser(User user, RegistrationDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        updatePassword(user.getId(), encodedPassword);

        ConfirmationCode newCode = ConfirmationCode.createCode();
        ConfirmationCode existingCode = user.getConfirmationCode();
        confirmationCodeService.updateConfirmationCode(newCode, existingCode);

        updateNotification(user.getId(), dto.getNotification());
        return user;
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public void addHistory(RequestTicketsDto dto, String language, @Nullable User user) {
        Optional.ofNullable(user).ifPresent(u -> {
            Set<TransportType> types = TransportType.getTypes(dto.getBus(), dto.getTrain(), dto.getAirplane(), dto.getFerry());
            historyRepository.save(SearchHistory.builder()
                    .user(u)
                    .departureCityId(typeAheadService.getCityId(dto.getDepartureCity(), language))
                    .arrivalCityId(typeAheadService.getCityId(dto.getArrivalCity(), language))
                    .departureDate(dto.getDepartureDate())
                    .typeTransport(types)
                    .build());
        });
    }

    @Override
    public List<HistoryDto> getHistory(@Nullable User user, String language) {
        return Optional.ofNullable(user)
                .map(User::getHistory)
                .orElse(Collections.emptyList())
                .stream()
                .map(history -> {
                    String departureCity = ukrainianCitiesService.getCity(history.getArrivalCityId(), language);
                    String arrivalCity = ukrainianCitiesService.getCity(history.getDepartureCityId(), language);
                    return historyMapper.historyToDto(history, new DepartureCity(arrivalCity), new ArrivalCity(departureCity));
                })
                .toList().reversed();
    }

    /**
     * Verifies the user using the provided code.
     *
     * @param dto  the data transfer object containing the code and email for confirmation
     * @param user the user to be verified
     */
    private void verifyUser(CodeConfirmationDto dto, User user) {
        if (confirmationCodeService.verifyCode(user.getConfirmationCode(), dto.getCode())) {
            enableUser(user);
            log.info("User with ID: {} has successfully confirmed email.", user.getId());
        } else {
            throw new InvalidConfirmationCodeException();
        }
    }

}
