package team.kaleni.notificationservice.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserUUIDMapping {

    private final HashMap<Long, UUID> userUUIDMapping;
    private final HashMap<UUID, Long> uuidUserMapping;

    public UserUUIDMapping() {
        userUUIDMapping = new HashMap<>();
        uuidUserMapping = new HashMap<>();
    }

    public Long getUserIdByUUID(UUID uuid) {
        Optional<Long> optUserId = Optional.ofNullable(uuidUserMapping.get(uuid));
        if (optUserId.isPresent()) {
            return optUserId.get();
        } else {
            throw new RuntimeException("No such user mapped with provided UUID");
        }
    }

    public UUID generateUUIDForUserWithId(Long userId) {
        UUID uuid = UUID.randomUUID();

        if (userUUIDMapping.containsKey(userId)) {
            UUID oldUUID = userUUIDMapping.get(userId);
            uuidUserMapping.remove(oldUUID);
        }

        userUUIDMapping.put(userId, uuid);
        uuidUserMapping.put(uuid, userId);
        return userUUIDMapping.get(userId);
    }

    public void removeMapping(UUID uuid) {
        Long userId = uuidUserMapping.get(uuid);
        userUUIDMapping.remove(userId);
        uuidUserMapping.remove(uuid);
    }

    public void removeMappingIfExists(Long userId) {
        if (userUUIDMapping.containsKey(userId)){
            removeMapping(userUUIDMapping.get(userId));
        }
    }
}
