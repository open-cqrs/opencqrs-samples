package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record ReserveEmailAddressCommand(
        String email,
        String username
) implements Command {

    @Override
    public String getSubject() {
        return "/email-addresses/" + sha256(email);
    }

    static String sha256(String email) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
