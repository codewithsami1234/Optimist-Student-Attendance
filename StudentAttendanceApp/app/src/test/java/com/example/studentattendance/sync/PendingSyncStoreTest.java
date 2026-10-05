package com.example.studentattendance.sync;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PendingSyncStoreTest {

    @Test
    public void enqueueAndResolveMappedIds() {
        PendingSyncStore store = new PendingSyncStore(new MemoryKvStore());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "CSE");
        PendingOp op = PendingOp.create(SyncOpType.CREATE_DEPARTMENT, "dept_local", payload);
        store.enqueue(op);

        assertEquals(1, store.pendingCount());
        assertTrue(store.unsyncedLocalIds().contains("dept_local"));
        assertEquals("dept_local", store.resolveId("dept_local"));

        store.mapId("dept_local", "dept_server");
        assertEquals("dept_server", store.resolveId("dept_local"));
        assertEquals("untouched", store.resolveId("untouched"));
    }

    @Test
    public void removeOpAndMarkPull() {
        PendingSyncStore store = new PendingSyncStore(new MemoryKvStore());
        assertFalse(store.hasCompletedPull());
        PendingOp op = PendingOp.create(SyncOpType.DELETE_SECTION, null, new LinkedHashMap<>());
        store.enqueue(op);
        store.remove(op.id);
        assertEquals(0, store.pendingCount());
        store.markPullSuccess();
        assertTrue(store.hasCompletedPull());
    }

    @Test
    public void payloadHelpersParseGsonNumbers() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("semesterNumber", 3.0d);
        payload.put("archived", true);
        payload.put("name", "Sem 3");
        assertEquals(Integer.valueOf(3), PendingSyncStore.integer(payload, "semesterNumber"));
        assertEquals(Boolean.TRUE, PendingSyncStore.bool(payload, "archived"));
        assertEquals("Sem 3", PendingSyncStore.str(payload, "name"));
    }

    @Test
    public void hybridPolicyTreatsEmptyListAsCacheOnlyAfterPull() {
        List<String> empty = new ArrayList<>();
        assertFalse(HybridPolicy.isUsableCache(empty, false));
        assertTrue(HybridPolicy.isUsableCache(empty, true));
        assertTrue(HybridPolicy.isUsableCache(Collections.singletonList("x"), false));
        assertFalse(HybridPolicy.isUsableCache(null, true));
    }
}
