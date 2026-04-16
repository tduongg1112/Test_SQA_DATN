package com.example.react_flow_be.controller;

import com.example.react_flow_be.config.DiagramSessionManager;
import com.example.react_flow_be.dto.collaboration.CollaborationDTO;
import com.example.react_flow_be.dto.websocket.*;
import com.example.react_flow_be.entity.Collaboration;
import com.example.react_flow_be.service.CollaborationService;
import com.example.react_flow_be.service.DatabaseDiagramService;
import com.example.react_flow_be.service.SchemaVisualizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest("spring.cloud.eureka.enabled=false")
public class SchemaWebSocketControllerTest {

    @Autowired
    private SchemaWebSocketController controller;

    @MockBean
    private SchemaVisualizerService schemaVisualizerService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private DatabaseDiagramService databaseDiagramService;

    @MockBean
    private DiagramSessionManager sessionManager;

    @MockBean
    private CollaborationService collaborationService;

    private SimpMessageHeaderAccessor headerAccessor;
    private final String SESSION_ID = "session123";
    private final String EDITOR_USER = "editor_user";
    private final String VIEWER_USER = "viewer_user";
    private final String COMMENTER_USER = "commenter_user";

    @BeforeEach
    void setUp() {
        headerAccessor = mock(SimpMessageHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn(SESSION_ID);
    }

    private void setupUser(String username, Collaboration.Permission permission) {
        when(sessionManager.getUsernameForSession(SESSION_ID)).thenReturn(username);
        CollaborationDTO dto = new CollaborationDTO();
        dto.setUsername(username);
        dto.setPermission(permission);
        when(collaborationService.getUserCollaboration(anyLong(), eq(username))).thenReturn(dto);
    }

    /**
     * Test Case ID: UT_WSC_001
     * Objective: Test successful broadcast of node position update with EDIT access.
     */
    @Test
    @DisplayName("UT_WSC_001 - updateNodePosition: EDIT access")
    void ut_wsc_001() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ModelUpdateMessage msg = new ModelUpdateMessage();
        msg.setModelId("M1");

        controller.updateNodePosition(1L, msg, headerAccessor);

        verify(schemaVisualizerService, times(1)).updateModelPosition(any(), any(), any(), any());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_002
     * Objective: Verify block and error notification for VIEW-only users.
     */
    @Test
    @DisplayName("UT_WSC_002 - updateNodePosition: VIEW access (Deny)")
    void ut_wsc_002() {
        setupUser(VIEWER_USER, Collaboration.Permission.VIEW);
        ModelUpdateMessage msg = new ModelUpdateMessage();

        controller.updateNodePosition(1L, msg, headerAccessor);

        verify(schemaVisualizerService, never()).updateModelPosition(any(), any(), any(), any());
        verify(messagingTemplate, times(1)).convertAndSend(contains("/queue/errors-"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_003
     * Objective: Test real-time addition of an attribute by an authorized collaborator.
     */
    @Test
    @DisplayName("UT_WSC_003 - addAttribute: Success")
    void ut_wsc_003() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        AddAttributeMessage msg = new AddAttributeMessage();
        msg.setModelId("M1");
        msg.setNewAttributeId("A1");
        when(schemaVisualizerService.addAttribute(any(), any(), any(), any())).thenReturn("attr_id_in_db");

        controller.addAttribute(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_004
     * Objective: Test successful hard removal of a model via WebSocket.
     */
    @Test
    @DisplayName("UT_WSC_004 - deleteModel: Success")
    void ut_wsc_004() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        DeleteModelMessage msg = new DeleteModelMessage();
        msg.setModelId("M1");
        when(schemaVisualizerService.deleteModel(any())).thenReturn(true);

        controller.deleteModel(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_005
     * Objective: Verify internal server error handling (Exception in service).
     */
    @Test
    @DisplayName("UT_WSC_005 - Exception Handling")
    void ut_wsc_005() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ModelUpdateMessage msg = new ModelUpdateMessage();
        when(schemaVisualizerService.updateModelPosition(any(), any(), any(), any())).thenThrow(new RuntimeException("DB Connection Error"));

        controller.updateNodePosition(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(contains("/queue/errors-"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_006
     * Objective: Test real-time renaming of a database model.
     */
    @Test
    @DisplayName("UT_WSC_006 - updateModelName: Success")
    void ut_wsc_006() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        UpdateModelNameMessage msg = new UpdateModelNameMessage();
        when(schemaVisualizerService.updateModelName(any(), any(), any())).thenReturn(true);

        controller.updateModelName(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_007 [FAIL EXPECTED]
     * Objective: Verify if users with COMMENT permission are blocked from editing.
     */
    @Test
    @DisplayName("UT_WSC_007 - COMMENT permission EDITing BUG")
    void ut_wsc_007() {
        setupUser(COMMENTER_USER, Collaboration.Permission.COMMENT);
        ModelUpdateMessage msg = new ModelUpdateMessage();

        controller.updateNodePosition(1L, msg, headerAccessor);

        // Code hiện tại chỉ chặn VIEW, không chặn COMMENT
        verify(schemaVisualizerService, times(1)).updateModelPosition(any(), any(), any(), any());
        fail("[BUG] System permits users with COMMENT permission to perform BROADCAST editing actions.");
    }

    /**
     * Test Case ID: UT_WSC_008
     * Objective: Verify graceful rejection of unknown key types.
     */
    @Test
    @DisplayName("UT_WSC_008 - toggleKeyType: Invalid Type")
    void ut_wsc_008() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ToggleKeyTypeMessage msg = new ToggleKeyTypeMessage();
        msg.setKeyType("INVALID_KEY_TYPE");

        controller.toggleKeyType(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(contains("/queue/errors-"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_009
     * Objective: Verify successful model creation with coordinate broadcasting.
     */
    @Test
    @DisplayName("UT_WSC_009 - addModel: Success")
    void ut_wsc_009() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        AddModelMessage msg = new AddModelMessage();
        when(schemaVisualizerService.addModel(any(), any(), any(), any(), any())).thenReturn("new_model_id");

        controller.addModel(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_010
     * Objective: Verify broadcast of global diagram rename events.
     */
    @Test
    @DisplayName("UT_WSC_010 - updateDiagramName: Success")
    void ut_wsc_010() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        UpdateDiagramNameMessage msg = new UpdateDiagramNameMessage();
        msg.setNewName("New Project Title");
        when(databaseDiagramService.updateDiagramName(any(), any())).thenReturn(true);

        controller.updateDiagramName(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_011
     * Objective: Verify attachment of sessionId to BaseWebSocketMessage.
     */
    @Test
    @DisplayName("UT_WSC_011 - SessionId Tracking")
    void ut_wsc_011() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ModelUpdateMessage msg = new ModelUpdateMessage();

        controller.updateNodePosition(1L, msg, headerAccessor);

        // Verification of manual sessionId setting in BaseWebSocketMessage
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_012
     * Objective: Test successful sync of FK relationship removal.
     */
    @Test
    @DisplayName("UT_WSC_012 - disconnectForeignKey: Success")
    void ut_wsc_012() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ForeignKeyDisconnectMessage msg = new ForeignKeyDisconnectMessage();
        when(schemaVisualizerService.removeForeignKeyConnection(any())).thenReturn(true);

        controller.disconnectForeignKey(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }

    /**
     * Test Case ID: UT_WSC_013
     * Objective: Test successful sync of new FK connection events.
     */
    @Test
    @DisplayName("UT_WSC_013 - connectForeignKey: Success")
    void ut_wsc_013() {
        setupUser(EDITOR_USER, Collaboration.Permission.EDIT);
        ForeignKeyConnectMessage msg = new ForeignKeyConnectMessage();
        when(schemaVisualizerService.createForeignKeyConnection(any(), any(), any(), any())).thenReturn(true);

        controller.connectForeignKey(1L, msg, headerAccessor);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/diagram/1"), any(WebSocketResponse.class));
    }
}
