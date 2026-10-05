package com.example.studentattendance.sync;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Durable outbox + client-to-server ID map. Stored as JSON so Room schema
 * stays unchanged.
 */
public final class PendingSyncStore {

    private static final String KEY_OPS = "pending_ops_json";
    private static final String KEY_ID_MAP = "id_map_json";
    private static final String KEY_LAST_PULL = "last_successful_pull_millis";
    private static final String KEY_POSTED = "posted_notification_ids_json";

    private static final Type OP_LIST = new TypeToken<List<PendingOp>>() { }.getType();
    private static final Type STRING_MAP = new TypeToken<Map<String, String>>() { }.getType();
    private static final Type STRING_SET = new TypeToken<Set<String>>() { }.getType();

    private final KvStore kv;
    private final Gson gson = new Gson();

    public PendingSyncStore(KvStore kv) {
        this.kv = kv;
    }

    public synchronized List<PendingOp> snapshot() {
        String raw = kv.get(KEY_OPS, "[]");
        List<PendingOp> ops = gson.fromJson(raw, OP_LIST);
        return ops == null ? new ArrayList<>() : new ArrayList<>(ops);
    }

    public synchronized int pendingCount() {
        return snapshot().size();
    }

    public synchronized void enqueue(PendingOp op) {
        List<PendingOp> ops = snapshot();
        ops.add(op);
        persistOps(ops);
    }

    public synchronized void replaceAll(List<PendingOp> ops) {
        persistOps(ops);
    }

    public synchronized void remove(String opId) {
        List<PendingOp> ops = snapshot();
        Iterator<PendingOp> iterator = ops.iterator();
        while (iterator.hasNext()) {
            if (opId.equals(iterator.next().id)) {
                iterator.remove();
            }
        }
        persistOps(ops);
    }

    public synchronized void mapId(String localId, String serverId) {
        if (localId == null || serverId == null || localId.equals(serverId)) {
            return;
        }
        Map<String, String> map = idMap();
        map.put(localId, serverId);
        kv.put(KEY_ID_MAP, gson.toJson(map));
    }

    public synchronized String resolveId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        Map<String, String> map = idMap();
        String mapped = map.get(id);
        return mapped == null ? id : mapped;
    }

    public synchronized Set<String> unsyncedLocalIds() {
        Set<String> ids = new HashSet<>();
        for (PendingOp op : snapshot()) {
            if (op.localId != null && !op.localId.isEmpty()) {
                ids.add(op.localId);
            }
        }
        return ids;
    }

    public synchronized void markPullSuccess() {
        kv.put(KEY_LAST_PULL, String.valueOf(System.currentTimeMillis()));
    }

    public synchronized boolean hasCompletedPull() {
        try {
            return Long.parseLong(kv.get(KEY_LAST_PULL, "0")) > 0L;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public synchronized Set<String> postedNotificationIds() {
        Set<String> ids = gson.fromJson(kv.get(KEY_POSTED, "[]"), STRING_SET);
        return ids == null ? new HashSet<>() : new HashSet<>(ids);
    }

    public synchronized void addPostedNotificationId(String notificationId) {
        if (notificationId == null) {
            return;
        }
        Set<String> ids = postedNotificationIds();
        ids.add(notificationId);
        kv.put(KEY_POSTED, gson.toJson(ids));
    }

    public synchronized void clearAll() {
        persistOps(new ArrayList<>());
        kv.put(KEY_ID_MAP, "{}");
        kv.put(KEY_LAST_PULL, "0");
        kv.put(KEY_POSTED, "[]");
    }

    private Map<String, String> idMap() {
        Map<String, String> map = gson.fromJson(kv.get(KEY_ID_MAP, "{}"), STRING_MAP);
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    private void persistOps(List<PendingOp> ops) {
        kv.put(KEY_OPS, gson.toJson(ops));
    }

    public static String str(Map<String, Object> payload, String key) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static Integer integer(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static Boolean bool(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
