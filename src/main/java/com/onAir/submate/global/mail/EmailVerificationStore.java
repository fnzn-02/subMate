package com.onAir.submate.global.mail;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailVerificationStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void save(String email, String code) {
        store.put(email, new Entry(code, LocalDateTime.now().plusMinutes(10)));
    }

    public boolean verify(String email, String code) {
        Entry entry = store.get(email);
        if (entry == null || entry.isExpired()) {
            store.remove(email);
            return false;
        }
        return entry.code().equals(code);
    }

    public void remove(String email) {
        store.remove(email);
    }

    public boolean exists(String email) {
        Entry entry = store.get(email);
        if (entry == null || entry.isExpired()) { store.remove(email); return false; }
        return true;
    }

    record Entry(String code, LocalDateTime expiresAt) {
        boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }
}
