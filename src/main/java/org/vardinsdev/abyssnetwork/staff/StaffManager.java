package org.vardinsdev.abyssnetwork.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StaffManager {
    private final File dataFile = new File("config/staff.json");
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<UUID, StaffMember> staffCache = new HashMap<>();

    public void loadStaff() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveStaffAsync();
            return;
        }

        try {
            StaffDataWrapper data = mapper.readValue(dataFile, StaffDataWrapper.class);
            if (data != null && data.getStaff() != null) {
                staffCache.putAll(data.getStaff());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> saveStaffAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                StaffDataWrapper wrapper = new StaffDataWrapper();
                wrapper.setStaff(staffCache);
                mapper.writeValue(dataFile, wrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isStaff(UUID uuid) {
        return staffCache.containsKey(uuid);
    }

    public StaffMember getStaff(UUID uuid) {
        return staffCache.get(uuid);
    }

    public void addStaff(StaffMember member) {
        staffCache.put(member.getUuid(), member);
        saveStaffAsync();
    }

    private static class StaffDataWrapper {
        private Map<UUID, StaffMember> staff;
        public Map<UUID, StaffMember> getStaff() { return staff; }
        public void setStaff(Map<UUID, StaffMember> staff) { this.staff = staff; }
    }
}
