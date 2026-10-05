package com.example.studentattendance.ui.main;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainOverflowMenuTest {

    @Test
    public void mainOverflowDoesNotContainImportExport() throws Exception {
        String xml = readMenu();
        assertFalse(xml.contains("action_import_export"));
        assertFalse(xml.contains("menu_import_export"));
        assertTrue(xml.contains("action_profile"));
        assertTrue(xml.contains("action_theme"));
        assertTrue(xml.contains("action_about"));
        assertTrue(xml.contains("action_logout"));
        assertTrue(xml.contains("action_notifications"));
    }

    private static String readMenu() throws Exception {
        Path path = Paths.get("src/main/res/menu/menu_main.xml");
        if (!Files.exists(path)) {
            path = Paths.get("app/src/main/res/menu/menu_main.xml");
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
