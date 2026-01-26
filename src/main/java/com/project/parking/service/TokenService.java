package com.project.parking.service;
import com.project.parking.model.Token;
import com.project.parking.model.User;
import com.project.parking.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public Token addToken(User user, String token){
        Token userToken = tokenRepository.findByUser(user);

        if(userToken != null){
            log.error( "Token already exists, deleting old token" + userToken.getToken());
            tokenRepository.deleteToken(userToken);
            tokenRepository.flush();
        }

        long expiration = 864000;

        Token newToken = new Token();
        newToken.setUser(user);
        newToken.setToken(token);
        newToken.setExpirationDate(LocalDateTime.now().plusSeconds(expiration));
        return tokenRepository.save(newToken);
    }

    public Token getToken(String token){
        return tokenRepository.findByToken(token);
    }

//    public boolean isValidToken(String token){
//        Token userToken = tokenRepository.findByToken(token);
//        if(userToken.getExpirationDate().isAfter(LocalDateTime.now())){
//            return true;
//        }else {
//            return false;
//        }
//    }
}

