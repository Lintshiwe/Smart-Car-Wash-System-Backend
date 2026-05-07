package za.co.int216d.carwash.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.auth.config.AuthAppProperties;
import za.co.int216d.carwash.auth.domain.EmailOtp;
import za.co.int216d.carwash.auth.repository.EmailOtpRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailOtpRepository repository;
    private final AuthAppProperties props;
    private final PasswordEncoder encoder;

    @Transactional
    public String issue(UUID userId) {
        String otp = generate(props.getOtp().getLength());
        EmailOtp record = EmailOtp.builder()
                .userId(userId)
                .otpHash(encoder.encode(otp))
                .expiresAt(Instant.now().plus(props.getOtp().getTtlMinutes(), ChronoUnit.MINUTES))
                .build();
        repository.save(record);
        return otp;
    }

    @Transactional
    public boolean verify(UUID userId, String submittedOtp) {
        Optional<EmailOtp> match = repository.findByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(userId).stream()
                .filter(o -> o.getExpiresAt().isAfter(Instant.now()))
                .filter(o -> encoder.matches(submittedOtp, o.getOtpHash()))
                .findFirst();
        if (match.isEmpty()) return false;
        EmailOtp otp = match.get();
        otp.setConsumedAt(Instant.now());
        repository.save(otp);
        return true;
    }

    @Transactional
    public boolean verifyAndConsume(UUID userId, String submittedOtp) {
        return verify(userId, submittedOtp);
    }

    private String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }
}
