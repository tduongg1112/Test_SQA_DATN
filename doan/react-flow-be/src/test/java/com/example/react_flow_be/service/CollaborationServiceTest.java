package com.example.react_flow_be.service;

import com.example.react_flow_be.config.DiagramSessionManager;
import com.example.react_flow_be.config.WebSocketSessionTracker;
import com.example.react_flow_be.dto.collaboration.CollaborationDTO;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest("spring.cloud.eureka.enabled=false")
@Transactional
public class CollaborationServiceTest {

    @Autowired
    private CollaborationService collaborationService;

    @Autowired
    private CollaborationRepository collaborationRepository;

    @Autowired
    private DiagramRepository diagramRepository;

    @MockBean
    private DiagramSessionManager sessionManager;

    @MockBean
    private WebSocketSessionTracker sessionRegistry;

    private Long diagramId;
    private Long ownerCollabId;
    private Long participantCollabId;
    private final String OWNER_USERNAME = "owner_user";
    private final String PARTICIPANT_USERNAME = "participant_user";

    @BeforeEach
    void setUp() {
        Diagram diagram = new Diagram();
        diagram.setName("Collaboration Test Diagram");
        diagram.setIsDeleted(false);
        diagram = diagramRepository.save(diagram);
        diagramId = diagram.getId();

        Collaboration ownerCollab = new Collaboration();
        ownerCollab.setDiagram(diagram);
        ownerCollab.setUsername(OWNER_USERNAME);
        ownerCollab.setType(Collaboration.CollaborationType.OWNER);
        ownerCollab.setPermission(Collaboration.Permission.FULL_ACCESS);
        ownerCollab.setIsActive(true);
        ownerCollab = collaborationRepository.save(ownerCollab);
        ownerCollabId = ownerCollab.getId();
        
        Collaboration participantCollab = new Collaboration();
        participantCollab.setDiagram(diagram);
        participantCollab.setUsername(PARTICIPANT_USERNAME);
        participantCollab.setType(Collaboration.CollaborationType.PARTICIPANTS);
        participantCollab.setPermission(Collaboration.Permission.FULL_ACCESS);
        participantCollab.setIsActive(true);
        participantCollab = collaborationRepository.save(participantCollab);
        participantCollabId = participantCollab.getId();
    }

    @Test
    @DisplayName("UT_CS_001 - addCollaborator: Existing")
    void ut_cs_001() {
        assertThrows(IllegalArgumentException.class, () -> collaborationService.addCollaborator(diagramId, OWNER_USERNAME, Collaboration.Permission.VIEW));
    }

    @Test
    @DisplayName("UT_CS_002 - addCollaborator: Success")
    void ut_cs_002() {
        CollaborationDTO result = collaborationService.addCollaborator(diagramId, "new_u", Collaboration.Permission.FULL_ACCESS);
        assertNotNull(result);
        assertTrue(collaborationRepository.findByDiagramIdAndUsername(diagramId, "new_u").isPresent());
    }

    @Test
    @DisplayName("UT_CS_003 - updatePermission: Owner Protection")
    void ut_cs_003() {
        assertThrows(IllegalArgumentException.class, () -> collaborationService.updatePermission(ownerCollabId, Collaboration.Permission.VIEW));
    }

    @Test
    @DisplayName("UT_CS_004 - updatePermission: Downgrade & Disconnect")
    void ut_cs_004() {
        when(sessionManager.isUserActiveInDiagram(anyLong(), anyString())).thenReturn(true);
        when(sessionManager.getActiveSessions(anyLong())).thenReturn(Collections.singleton("s1"));
        when(sessionManager.getUsernameForSession("s1")).thenReturn(PARTICIPANT_USERNAME);

        collaborationService.updatePermission(participantCollabId, Collaboration.Permission.VIEW);
        
        assertEquals(Collaboration.Permission.VIEW, collaborationRepository.findById(participantCollabId).get().getPermission());
        verify(sessionRegistry, times(1)).closeSessions(anySet());
    }

    @Test
    @DisplayName("UT_CS_005 - removeCollaborator: Success")
    void ut_cs_005() {
        collaborationService.removeCollaborator(participantCollabId);
        assertTrue(collaborationRepository.findById(participantCollabId).isEmpty());
    }

    @Test
    @DisplayName("UT_CS_006 - removeCollaborator: Owner Protection")
    void ut_cs_006() {
        assertThrows(IllegalArgumentException.class, () -> collaborationService.removeCollaborator(ownerCollabId));
    }

    @Test
    @DisplayName("UT_CS_007 - addCollaborator: Soft-Deleted Diagram BUG")
    void ut_cs_007() {
        Diagram d = diagramRepository.findById(diagramId).get();
        d.setIsDeleted(true);
        diagramRepository.save(d);

        collaborationService.addCollaborator(diagramId, "new_u_fail", Collaboration.Permission.VIEW);
        // Expecting exception but code allows adding to deleted diagram
        fail("[BUG] Allowed adding collaborator to trashed diagram");
    }

    @Test
    @DisplayName("UT_CS_008 - updatePermission: Idempotency Optimization")
    void ut_cs_008() {
        collaborationService.updatePermission(participantCollabId, Collaboration.Permission.FULL_ACCESS);
        verify(sessionRegistry, never()).closeSessions(anySet());
    }

    @Test
    @DisplayName("UT_CS_009 - addCollaborator: Null Permission Check")
    void ut_cs_009() {
        // Depending on DB constraints, this might throw or fail later.
        assertThrows(Exception.class, () -> collaborationService.addCollaborator(diagramId, "u1", null));
    }

    @Test
    @DisplayName("UT_CS_010 - hasAccess: Expiry Logic BUG")
    void ut_cs_010() {
        Collaboration c = collaborationRepository.findById(participantCollabId).get();
        c.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        collaborationRepository.save(c);

        boolean hasAccess = collaborationService.hasAccess(diagramId, PARTICIPANT_USERNAME);
        // Should be false if expired, but code only checks isActive
        assertFalse(hasAccess, "[BUG] hasAccess ignores expiresAt field");
    }

    @Test
    @DisplayName("UT_CS_011 - deactivateCollaboration: Success")
    void ut_cs_011() {
        collaborationService.deactivateCollaboration(participantCollabId);
        assertFalse(collaborationRepository.findById(participantCollabId).get().getIsActive());
    }

    @Test
    @DisplayName("UT_CS_012 - getUserCollaboration: Unauthorized")
    void ut_cs_012() {
        assertThrows(EntityNotFoundException.class, () -> collaborationService.getUserCollaboration(diagramId, "stalker"));
    }
}
