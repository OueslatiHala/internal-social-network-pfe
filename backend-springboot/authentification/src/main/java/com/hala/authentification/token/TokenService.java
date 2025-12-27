package com.hala.authentification.token;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    @Transactional
    public void saveToken(Token token) {
        Optional<Token> existingToken = tokenRepository.findByToken(token.getToken());
        if (existingToken.isPresent()) {
            Token existing = existingToken.get();
            existing.setRevoked(token.isRevoked());
            existing.setExpired(token.isExpired());
            tokenRepository.save(existing);
        } else {
            tokenRepository.save(token);
        }
    }

}
