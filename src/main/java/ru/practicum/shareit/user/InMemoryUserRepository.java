package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Set<String> emails = ConcurrentHashMap.newKeySet();
    private long idCounter = 1;

    @Override
    public User save(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        emails.add(user.getEmail().toLowerCase());
        return user;
    }

    @Override
    public User update(User user) {
        User old = users.get(user.getId());
        if (old != null) {
            emails.remove(old.getEmail().toLowerCase());
        }
        users.put(user.getId(), user);
        emails.add(user.getEmail().toLowerCase());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        User removed = users.remove(id);
        if (removed != null) {
            emails.remove(removed.getEmail().toLowerCase());
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return emails.contains(email.toLowerCase());
    }
}
