package ru.practicum.shareit.user;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Set<String> emails = ConcurrentHashMap.newKeySet();
    private long idCounter = 1;

    public User save(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        emails.add(user.getEmail().toLowerCase());
        return user;
    }

    public User update(User user) {
        User old = users.get(user.getId());
        if (old != null) {
            emails.remove(old.getEmail().toLowerCase());
        }
        users.put(user.getId(), user);
        emails.add(user.getEmail().toLowerCase());
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void deleteById(Long id) {
        User removed = users.remove(id);
        if (removed != null) {
            emails.remove(removed.getEmail().toLowerCase());
        }
    }

    public boolean existsByEmail(String email) {
        return emails.contains(email.toLowerCase());
    }
}
