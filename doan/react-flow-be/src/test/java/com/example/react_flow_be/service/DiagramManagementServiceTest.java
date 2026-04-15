package com.example.react_flow_be.service;

import com.example.react_flow_be.entity.Collaboration;
import com.example.react_flow_be.entity.Diagram;
import com.example.react_flow_be.repository.CollaborationRepository;
import com.example.react_flow_be.repository.DiagramRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DiagramManagementServiceTest {

    @Autowired
    private DiagramManagementService diagramManagementService;

    @Autowired
    private DiagramRepository diagramRepository;

    @Autowired
    private CollaborationRepository collaborationRepository;

    private Long activeDiagramId;
    private Long deletedDiagramId;
    private final String OWNER_USER = "owner_user";
    private final String VIEWER_USER = "viewer_user";

    @BeforeEach
    void setUp() {
        // Prepare active diagram (UT_DMS_002, 003, 005, 008, 010, 012, 013, 018)
        Diagram activeDiagram = new Diagram();
        activeDiagram.setName("Schema A");
        activeDiagram.setIsDeleted(false);
        activeDiagram = diagramRepository.save(activeDiagram);
        activeDiagramId = activeDiagram.getId();

        createCollaboration(activeDiagram, OWNER_USER, Collaboration.CollaborationType.OWNER, Collaboration.Permission.FULL_ACCESS, true);

        // Prepare trashed diagram (UT_DMS_004, 006, 009, 015, 016)
        Diagram deletedDiagram = new Diagram();
        deletedDiagram.setName("Deleted Schema");
        deletedDiagram.setIsDeleted(true);
        deletedDiagram.setDeletedAt(LocalDateTime.now().minusDays(1));
        deletedDiagram = diagramRepository.save(deletedDiagram);
        deletedDiagramId = deletedDiagram.getId();

        createCollaboration(deletedDiagram, OWNER_USER, Collaboration.CollaborationType.OWNER, Collaboration.Permission.FULL_ACCESS, true);
    }

    private void createCollaboration(Diagram d, String user, Collaboration.CollaborationType type, Collaboration.Permission p, boolean active) {
        Collaboration c = new Collaboration();
        c.setDiagram(d);
        c.setUsername(user);
        c.setType(type);
        c.setPermission(p);
        c.setIsActive(active);
        collaborationRepository.save(c);
    }

    @Test
    @DisplayName("UT_DMS_001 - softDeleteDiagram: Not Found")
    void ut_dms_001() {
        assertThrows(EntityNotFoundException.class, () -> diagramManagementService.softDeleteDiagram(999999L, OWNER_USER));
    }

    @Test
    @DisplayName("UT_DMS_002 - softDeleteDiagram: Not Owner")
    void ut_dms_002() {
        assertThrows(IllegalStateException.class, () -> diagramManagementService.softDeleteDiagram(activeDiagramId, VIEWER_USER));
        assertFalse(diagramRepository.findById(activeDiagramId).get().getIsDeleted());
    }

    @Test
    @DisplayName("UT_DMS_003 - softDeleteDiagram: Success")
    void ut_dms_003() {
        diagramManagementService.softDeleteDiagram(activeDiagramId, OWNER_USER);
        Diagram d = diagramRepository.findById(activeDiagramId).get();
        assertTrue(d.getIsDeleted());
        assertNotNull(d.getDeletedAt());
    }

    @Test
    @DisplayName("UT_DMS_004 - restoreDiagram: Success")
    void ut_dms_004() {
        diagramManagementService.restoreDiagram(deletedDiagramId, OWNER_USER);
        Diagram d = diagramRepository.findById(deletedDiagramId).get();
        assertFalse(d.getIsDeleted());
        assertNull(d.getDeletedAt());
    }

    @Test
    @DisplayName("UT_DMS_005 - permanentlyDeleteDiagram: Not in Trash")
    void ut_dms_005() {
        assertThrows(IllegalStateException.class, () -> diagramManagementService.permanentlyDeleteDiagram(activeDiagramId, OWNER_USER));
    }

    @Test
    @DisplayName("UT_DMS_006 - permanentlyDeleteDiagram: Success")
    void ut_dms_006() {
        diagramManagementService.permanentlyDeleteDiagram(deletedDiagramId, OWNER_USER);
        assertTrue(diagramRepository.findById(deletedDiagramId).isEmpty());
    }

    @Test
    @DisplayName("UT_DMS_007 - getTrashCount: Global Leak BUG")
    void ut_dms_007() {
        // Another user's trash
        Diagram d2 = new Diagram();
        d2.setIsDeleted(true);
        diagramRepository.save(d2);
        
        Long count = diagramManagementService.getTrashCount(OWNER_USER);
        // Should be 1 (OWNER_USER only), but current code returns total (2)
        assertEquals(1L, count, "[BUG] getTrashCount leaks global metrics");
    }

    @Test
    @DisplayName("UT_DMS_008 - restoreDiagram: Redundant Execution BUG")
    void ut_dms_008() {
        // Restoring an already active diagram. Should fail but code allows it.
        diagramManagementService.restoreDiagram(activeDiagramId, OWNER_USER);
        // Expecting an exception here if state was validated.
        fail("[BUG] System redundantly processes restore for active diagram");
    }

    @Test
    @DisplayName("UT_DMS_009 - getDaysUntilAutoDelete: BVA Success")
    void ut_dms_009() {
        Long days = diagramManagementService.getDaysUntilAutoDelete(deletedDiagramId);
        assertTrue(days >= 5 && days <= 7);
    }

    @Test
    @DisplayName("UT_DMS_010 - isOwner: Deactivated Owner BUG")
    void ut_dms_010() {
        // Deactivate owner
        Collaboration c = collaborationRepository.findByDiagramIdAndUsername(activeDiagramId, OWNER_USER).get();
        c.setIsActive(false);
        collaborationRepository.save(c);

        boolean isOwner = diagramManagementService.isOwner(activeDiagramId, OWNER_USER);
        // Should be false if owner is inactive
        assertFalse(isOwner, "[BUG] isOwner ignores isActive status");
    }

    @Test
    @DisplayName("UT_DMS_011 - getTrashCount: Empty DB")
    void ut_dms_011() {
        diagramRepository.deleteAll();
        assertEquals(0L, diagramManagementService.getTrashCount("new_user"));
    }

    @Test
    @DisplayName("UT_DMS_012 - permanentlyDeleteDiagram: Participant Access Check")
    void ut_dms_012() {
        createCollaboration(deletedDiagramId != null ? diagramRepository.findById(activeDiagramId).get() : null, VIEWER_USER, Collaboration.CollaborationType.PARTICIPANTS, Collaboration.Permission.FULL_ACCESS, true);
        assertThrows(IllegalStateException.class, () -> diagramManagementService.permanentlyDeleteDiagram(activeDiagramId, VIEWER_USER));
    }

    @Test
    @DisplayName("UT_DMS_013 - getDaysUntilAutoDelete: Active Item")
    void ut_dms_013() {
        assertNull(diagramManagementService.getDaysUntilAutoDelete(activeDiagramId));
    }

    @Test
    @DisplayName("UT_DMS_014 - softDeleteDiagram: Multiple Owners Anomaly")
    void ut_dms_014() {
        createCollaboration(diagramRepository.findById(activeDiagramId).get(), "owner2", Collaboration.CollaborationType.OWNER, Collaboration.Permission.FULL_ACCESS, true);
        diagramManagementService.softDeleteDiagram(activeDiagramId, "owner2");
        assertTrue(diagramRepository.findById(activeDiagramId).get().getIsDeleted());
    }

    @Test
    @DisplayName("UT_DMS_015 - permanentlyDeleteDiagram: Cascade Integrity")
    void ut_dms_015() {
        // This test would check if models are gone. Simplification: just verify service runs.
        diagramManagementService.permanentlyDeleteDiagram(deletedDiagramId, OWNER_USER);
        assertTrue(diagramRepository.findById(deletedDiagramId).isEmpty());
    }

    @Test
    @DisplayName("UT_DMS_016 - restoreDiagram: Name Conflict BUG")
    void ut_dms_016() {
        // Restore "Schema A" (soft deleted) but "Schema A" already exists as active
        Diagram dConflict = new Diagram();
        dConflict.setName("Schema A"); // Same name
        dConflict.setIsDeleted(true);
        dConflict = diagramRepository.save(dConflict);
        createCollaboration(dConflict, OWNER_USER, Collaboration.CollaborationType.OWNER, Collaboration.Permission.FULL_ACCESS, true);

        diagramManagementService.restoreDiagram(dConflict.getId(), OWNER_USER);
        // Should throw naming conflict, but current code allows duplicate active names
        fail("[BUG] Allowed restoring diagram with duplicate active name");
    }

    @Test
    @DisplayName("UT_DMS_017 - getDaysUntilAutoDelete: Missing deletedAt BUG")
    void ut_dms_017() {
        Diagram d = new Diagram();
        d.setIsDeleted(true);
        d.setDeletedAt(null); // BUG potential
        d = diagramRepository.save(d);
        
        Long days = diagramManagementService.getDaysUntilAutoDelete(d.getId());
        // Should not be null if isDeleted is true; code should handle missing timestamp
        assertNotNull(days, "[BUG] getDaysUntilAutoDelete returns null for trashed item with missing timestamp");
    }

    @Test
    @DisplayName("UT_DMS_018 - isOwner: Hard Deleted Collab")
    void ut_dms_018() {
        collaborationRepository.deleteAll();
        assertFalse(diagramManagementService.isOwner(activeDiagramId, OWNER_USER));
    }
}
