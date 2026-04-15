# Unit Test Plan - Frontend Core (Duc Anh)

**Project:** Database Design Support System using Large Language Models  
**Role:** Core Frontend & Usability QA (JavaScript / Jest / React Testing Library)  
**Owner:** Duc Anh  
**Scope (In-scope):** Custom utility functions and reducers serving the Canvas logic (`diagramUtils.js`, `schemaValidator.js`, etc.). *Third-party libraries like React Flow are explicitly excluded from testing.*  
**Testing Rules:** Since the Frontend has no direct DB connection, CheckDB and DB Rollback rules **do not apply**. Tests must heavily emphasize **Mocking** for user events, state behavior, and callback functions.  
**Team Task:** In charge of compiling the entire team's Excel Test Case file into the standard formatting.

---

## 1. Test Case Documentation

*Format: Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note.*  
*Objective: Achieve Level 2 Branch Coverage. Outputs are strictly dictated by the System Requirements Specification (SRS).*

### A. Core Validator: `schemaValidator.js` (15 Test Cases)
| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_VAL_01** | `schemaValidator.js` | `validateTableName` | Standard valid lowercase | `tableName = "users"` | Returns `isValid: true` | Pass | Validates correct execution path for standard lowercase string inputs. |
| **TC_FE_VAL_02** | `schemaValidator.js` | `validateTableName` | Standard valid snake_case | `tableName = "user_profiles"` | Returns `isValid: true` | Pass | Ensures snake_case naming conventions safely pass the validation logic. |
| **TC_FE_VAL_03** | `schemaValidator.js` | `validateTableName` | Empty string check | `tableName = ""` | Returns Error: "Table name cannot be empty" | Pass | Prevents core failure by intercepting null or empty string value submissions. |
| **TC_FE_VAL_04** | `schemaValidator.js` | `validateTableName` | Special characters check | `tableName = "users@123"` | Returns Error: "Table name contains invalid special characters" | Pass | Enforces structural integrity by rejecting non-alphanumeric/underscore variables mapping. |
| **TC_FE_VAL_05** | `schemaValidator.js` | `validateTableName` | White space check | `tableName = "user data"` | Returns Error: "Table name cannot contain spaces" | Fail | Crucial assertion to prevent SQL engine crashes caused by rogue whitespace characters. |
| **TC_FE_VAL_06** | `schemaValidator.js` | `validateTableName` | Starts with number | `tableName = "1users"` | Returns Error: "Table name cannot start with a number" | Fail | Adheres to strict SQL dialect naming conventions avoiding numeric prepends. |
| **TC_FE_VAL_07** | `schemaValidator.js` | `validateTableName` | Exceeds max length limit | `tableName` > 64 chars | Returns Error: "Table name exceeds maximum length" | Pass | Database bounding limit verification to protect the persistence layer limits. |
| **TC_FE_VAL_08** | `schemaValidator.js` | `validateTableName` | Duplicate table name | `tableName = "orders"`, existing tables array | Returns Error: "Table name already exists" | Pass | Traverses the current local state arrays ensuring deduplication of table identifiers. |
| **TC_FE_VAL_09** | `schemaValidator.js` | `validateColumnName` | Valid column name | `colName = "id"` | Returns `isValid: true` | Pass | Asserts positive integration branch for simple generic column definitions. |
| **TC_FE_VAL_10** | `schemaValidator.js` | `validateColumnName` | Empty column name | `colName = ""` | Returns Error: "Column name cannot be empty" | Pass | Baseline security boundary to intercept zero-length strings on column creation. |
| **TC_FE_VAL_11** | `schemaValidator.js` | `validateColumnName` | Invalid column syntax | `colName = "first-name"` | Returns Error: "Invalid character in column name" | Pass | Prevents dashboard parsing failures by filtering dashes and special syntaxes. |
| **TC_FE_VAL_12** | `schemaValidator.js` | `validateColumnName` | Duplicate column in same table | `colName = "email"`, existing columns array | Returns Error: "Column name already exists in this table" | Pass | Scopes duplication checking rigidly within the designated target table entity. |
| **TC_FE_VAL_13** | `schemaValidator.js` | `validateRelationship` | Valid FK to PK | Source config (FK) & target config (PK) | Returns `isValid: true` | Pass | Validates perfect nominal mapping scenarios representing healthy edge connections. |
| **TC_FE_VAL_14** | `schemaValidator.js` | `validateRelationship` | Self-reference circular loop | Source Col = Target Col on same Node | Returns Error: "Cannot create self-referencing relationship" | Pass | Employs algorithmic loop prevention to secure downstream schema tree traversal. |
| **TC_FE_VAL_15** | `schemaValidator.js` | `validateRelationship` | Data type mismatch | Source = `INT`, Target = `VARCHAR` | Returns Error: "Foreign key data type must match Primary key" | Fail | Evaluates strict type casting validation preventing corrupted relationships on export. |

### B. Core Utility: `diagramUtils.js` (15 Test Cases)
| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_UTIL_01** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing single Node (no edges) | Array with 1 node mock | Valid JSON: `{"tables": [{...}], "relationships": []}` | Pass | Simulates edge-case workflows evaluating a lone structural entity serialization payload. |
| **TC_FE_UTIL_02** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing 2 Nodes with 1 Edge | Array with 2 nodes, 1 edge mock | Valid JSON showing `relationships` properly mapped | Pass | Full pathway verification to guarantee complex multi-node structure graph serialization. |
| **TC_FE_UTIL_03** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing empty canvas | Nodes: `[]`, Edges: `[]` | JSON returns `{ "tables": [], "relationships": [] }` | Pass | Evaluates pure fallback handling routines intercepting null or entirely empty workspace requests. |
| **TC_FE_UTIL_04** | `diagramUtils.js` | `generateJSONFromNodes` | Node missing identity ID | Node lacking `id` | Throws Error: "Missing mandatory Node ID" | Fail | Secures internal DOM node mapping functionality by verifying identity payload constraints. |
| **TC_FE_UTIL_05** | `diagramUtils.js` | `generateJSONFromNodes` | Node missing type variable | Node lacking `type` | Defaults to 'table' type processing | Pass | Ensures robust failover values are seamlessly applied preventing node rendering crashes. |
| **TC_FE_UTIL_06** | `diagramUtils.js` | `generateJSONFromNodes` | Node dataset missing Table Name | Node missing `data.tableName` | Assigns generic name 'untitled_table' or throws Error | Fail | Validates safe fallback routines for missing attribute parameters within the object dataset payload. |
| **TC_FE_UTIL_07** | `diagramUtils.js` | `generateJSONFromNodes` | Edge lacking Source Node ID | Edge missing `source` ID | Ignores/Filters out the edge from relationships | Pass | Tests the filtration logic executing garbage collection for incomplete connecting handles. |
| **TC_FE_UTIL_08** | `diagramUtils.js` | `generateJSONFromNodes` | Edge lacking Target Node ID | Edge missing `target` ID | Ignores/Filters out the edge from relationships | Pass | Secondary validation checks asserting incomplete directional relationships are immediately dropped. |
| **TC_FE_UTIL_09** | `diagramUtils.js` | `generateJSONFromNodes` | Redundant/Duplicate Edges | Array with overlapping source/target items | JSON removes duplicate relationship definitions | Fail | Implements aggressive array deduplication strategies minimizing overlapping query generation bloat. |
| **TC_FE_UTIL_10** | `diagramUtils.js` | `generateJSONFromNodes` | Node containing 0 columns | Node with empty `attributes` Array | Resulting JSON table includes empty `[]` columns property | Pass | Verifies graceful handling of strictly structural entities possessing zero embedded attributes. |
| **TC_FE_UTIL_11** | `diagramUtils.js` | `generateJSONFromNodes` | Nodes with complex attributes | Attributes (PK, FK, Normal) mapping | JSON correctly tags column types and constraints (NOT NULL) | Pass | Triggers deep complex object serialization effectively translating component mappings to output shapes. |
| **TC_FE_UTIL_12** | `diagramUtils.js` | `generateJSONFromNodes` | Nested relationships (A->B->C) | 3 Nodes sequentially connected | JSON structure maps foreign keys appropriately across tables | Pass | Advanced tree depth traversals confirming sequential link tracking operations map perfectly locally. |
| **TC_FE_UTIL_13** | `diagramUtils.js` | `generateJSONFromNodes` | Unrecognized React Flow Node Type | Node type = 'stickyNote' | Ignored and excluded from DB structure export | Fail | Validates aggressive UI/UX specific component filtration from strict logical DB schema exports. |
| **TC_FE_UTIL_14** | `diagramUtils.js` | `generateJSONFromNodes` | Coordinate Extraction | Node coordinates = `x:100.5, y:20` | Converts/Rounds position safely to integers | Fail | Assesses explicit float variable truncation routines converting coordinates to raw safe integer maps. |
| **TC_FE_UTIL_15** | `diagramUtils.js` | `parseJSONToNodes` | Valid DB structure to Canvas Nodes | Valid JSON response payload | Outputs React Flow compatible `nodes`/`edges` array | Pass | Confirms backward pipeline pathway seamlessly reconstructing native React Flow graphical layouts perfectly. |

### C. Other Core Engine Files (20 Test Cases)
*(Includes sqlGenerator.js, canvasReducer.js, llmParser.js)*

| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_SQL_01** | `sqlGenerator.js` | `generateSQL` | Empty workspace | Empty nodes array | Returns empty string `""` | Pass | Essential baseline script validating return objects gracefully default to empty syntax parameters safely. |
| **TC_FE_SQL_02** | `sqlGenerator.js` | `generateSQL` | Standard single table layout | Modded node (id, username) | Returns `CREATE TABLE username (...)` string block | Fail | Tests initial dynamic string aggregation functions transforming basic data structures to SQL dialects. |
| **TC_FE_SQL_03** | `sqlGenerator.js` | `generateSQL` | FK Relationships existing | 2 Nodes, 1 Edge | Appends `ALTER TABLE ... ADD CONSTRAINT` | Fail | Intercepts advanced multi-module constraint generations for relational referential integrity mapping exports. |
| **TC_FE_SQL_04** | `sqlGenerator.js` | `generateSQL` | Column Data Mapping: INT | Field type `int` | Translates into SQL `INTEGER` | Pass | Cross-checks basic fundamental type conversions mapping universal UI types into strict SQL equivalents. |
| **TC_FE_SQL_05** | `sqlGenerator.js` | `generateSQL` | Column Data Mapping: String | Field type `varchar` len `255` | Translates into SQL `VARCHAR(255)` | Pass | Confirms accurate extraction and encapsulation of field bounding variable parsing within output statements. |
| **TC_FE_SQL_06** | `sqlGenerator.js` | `generateSQL` | NotNull & Primary Key | Config `primary=true` | Adds `PRIMARY KEY`, `NOT NULL` strings | Fail | Verifies critical strict schema integrity directives inherently map successfully into script payload results. |
| **TC_FE_SQL_07** | `sqlGenerator.js` | `generateSQL` | SQL Reserved Keyword avoidance | Table named 'Order' | Wraps word in backticks `` `Order` `` to avoid DB syntax crashes | Pass | Enforces stringent syntax safety routines heavily mitigating potential native external database compiling crashes. |
| **TC_FE_RED_01** | `canvasReducer.js` | `ADD_NODE` | Action adding standard table | `action.payload` containing new table obj | Local state `nodes` increments by 1 | Pass | Asserts fundamental foundational state mutability behavior accurately indexing new component scopes seamlessly. |
| **TC_FE_RED_02** | `canvasReducer.js` | `ADD_NODE` | Blocking duplicated IDs | Payload carrying pre-existing ID | State remains strictly unchanged | Pass | Validates defensive memory protection systems preventing component overlap avoiding downstream logic anomalies entirely. |
| **TC_FE_RED_03** | `canvasReducer.js` | `REMOVE_NODE` | Deleting a core relationship Node | Payload Delete Action + ID | Node is erased; **ALL** referring edges are cascade-deleted | Fail | Assesses sophisticated cascade orchestration routines destroying isolated component branches ensuring state memory remains completely unpolluted. |
| **TC_FE_RED_04** | `canvasReducer.js` | `UPDATE_NODE` | Modifying internal fields | Node ID, payload field data | Internal properties of specific state array node updated | Pass | Intercepts hyper-focused sub-array mapping logic confirming specific index targets successfully update local property scopes correctly. |
| **TC_FE_RED_05** | `canvasReducer.js` | `ADD_EDGE` | Drawing standard relational Edge | Mock event drawing A to B | State `edges` incremented | Pass | Confirms directional linking paths append effectively simulating precise user interface edge connection configurations. |
| **TC_FE_RED_06** | `canvasReducer.js` | `ADD_EDGE` | Overlapping/duplicate connection attempt | Try to connect already-connected fields | Fails, line discarded without altering state | Fail | Tests built-in redundancy filtration mechanisms efficiently catching duplicate linking iterations discarding illegal commands flawlessly. |
| **TC_FE_RED_07** | `canvasReducer.js` | `CLEAR_CANVAS` | Wipe operation | Empty payload Action | Wipes entire application scope (nodes:[], edges:[]) | Pass | Ensures factory resetting methods trigger precise payload wipes obliterating total application contexts rapidly for cache clearing tasks. |
| **TC_FE_LLM_01** | `llmParser.js` | `parseLLMResult` | Extracting raw standard valid JSON | Code block markdown response | Identifies JSON, returns structured object | Pass | Tests central core AI block isolation systems perfectly differentiating raw configuration data variables from conversational string syntax patterns. |
| **TC_FE_LLM_02** | `llmParser.js` | `parseLLMResult` | Extracting non-markdown bare JSON | Pure JSON string | Still correctly outputs structured object array | Pass | Validates flexible fallback AI pattern string identification correctly circumventing unformatted responses efficiently decoding standard text arrays naturally. |
| **TC_FE_LLM_03** | `llmParser.js` | `parseLLMResult` | Hallucination error interception | Broken JSON (missing closing brace `}`) | Exception caught, throws "Malformed LLM Data Structure" | Pass | Implements crucial heuristic boundaries detecting fragmented intelligence queries successfully maintaining system-level exception tracking catching capabilities natively. |
| **TC_FE_LLM_04** | `llmParser.js` | `parseLLMResult` | AI pre/post-text extraction | AI replies "Here is the result... { JSON } ... Enjoy!" | Uses regex to isolate and extract solely the JSON bracket block | Pass | Advanced string shielding methodologies deployed verifying algorithmic regex extractions systematically eliminating artificial conversational responses flawlessly bypassing noise. |
| **TC_FE_LLM_05** | `llmParser.js` | `sanitizeLLMNames` | Stripping unicode/accented letters | Table proposed as "Khách Hàng" | Formats out into raw ANSI output: "khach_hang" | Fail | Employs stringent deep regex string scrubbing functions targeting multilingual characters correctly standardizing variables adhering exclusively to global English format conventions. |
| **TC_FE_LLM_06** | `llmParser.js` | `sanitizeLLMNames` | Auto-trimming white space boundaries| Name defined as " Orders  " | Eliminates edge whitespace: "orders" | Pass | Spacing rectification processing algorithms testing trailing spacing artifacts reliably deleting unwanted boundary syntax elements preventing downstream rendering overlap bugs. |

---

## 2. Testing Constraints & Guidelines (Arrange - Act - Assert)

When writing automated scripts, the mandatory 3-step paradigm must be followed.

```javascript
import { validateTableName } from './schemaValidator';

// Test Case ID: TC_FE_VAL_03
test('validateTableName should return error when table name is strictly empty', () => {
    // 1. Arrange: Declare variables for mock user input and precise expectation
    const inputTableName = "";
    const expectedErrorMsg = "Table name cannot be empty";

    // 2. Act: Invoke the target method 
    const result = validateTableName(inputTableName);

    // 3. Assert: Verify via testing assertions
    expect(result.isValid).toBe(false);
    expect(result.message).toBe(expectedErrorMsg);
});
```

*Note: Since execution environments vary, ensure `npm run test -- --coverage` outputs successfully yielding over 80% coverage to qualify for grading.*

---

## 3. API & Integration Test Cases (Chatbot & Statistics)
*(Phần của thành viên khác - Tích hợp chung vào bảng Frontend)*

| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_CBT_01** | `ChatbotServiceTest` | `parseResponse_withValidStructuredOutput` | Validate parsing of standard AI output into a structured frontend object | Target: `chatbotService.ts::parseResponse()`, Valid mock payload injected | Returns valid `ChatbotResponse` object where action evaluates to "UPDATE" | Pass | Standard API integration check to ensure core structural alignment. |
| **TC_FE_CBT_02** | `ChatbotServiceTest` | `parseResponse_withRefreshPrefix` | Verify handling of prefixed AI outputs containing `_REFRESH_` tag | Target: `chatbotService.ts::parseResponse()`, Input precisely prefixed by `_REFRESH_` | The prefix is stripped during parsing; underlying `create/delete` instructions are correctly allocated | Pass | Ensures UI refresh flags do not contaminate the primary JSON parsing mechanism. |
| **TC_FE_CBT_03** | `ChatbotServiceTest` | `sendMessage_whenApiReturnsValidOutput` | Confirm successful chatbot POST request logic and response extraction | Mock `fetch` simulating API `ok=true`, JSON output contains valid data | Function yields a correct `ChatbotResponse`. POST parameters & headers assert safely | Pass | Validates the 'Happy Path' transaction between Frontend client and Chatbot API. |
| **TC_FE_CBT_04** | `ChatbotServiceTest` | `sendMessage_whenApiReturnsMalformedOut` | Test fallback mechanism when AI hallucination returns malformed JSON structures | Mock `fetch` simulating API `ok=true`, but payload JSON is intentionally corrupted | Function safely swallows error (NO UI throw) and automatically returns an empty fallback object | Pass | Crucial error boundary check to prevent React crashes during unexpected AI heuristics. |
| **TC_FE_CBT_05** | `ChatbotServiceTest` | `sendMessage_whenHttpError_returnsFallback` | Test fallback operations during critical network interruptions (HTTP 500 Exceptions) | Mock `fetch` enforcing an HTTP Error state (`ok=false, status 500`) | Function catches exception cleanly without throwing, returning a safe error fallback object | Pass | Guarantees application stability and graceful degradation during network downtime. |
| **TC_FE_STA_01** | `StatisticsApiTest` | `getStatistics_withValidApis` | Evaluate the aggregation engine for dashboard statistical metrics | Target: `statisticsApi.ts::getStatistics()`, Simulating 3 concurrent API calls | Load resolves successfully, returning an aggregated object enriched with diagram & chat metrics | Pass | Checks parallel promise resolution and proper aggregation logic for dashboard rendering. |
| **TC_FE_STA_02** | `StatisticsApiTest` | `getChatMetrics_whenApiError` | Validate exception pushing mechanism when statistics API is unreachable | Target: `statisticsApi.ts::getChatMetrics()`, Mock `fetch` throwing `ok=false` | Function natively throws a JavaScript `Error` allowing upstream Catch modules to notify users | Pass | Exception bubbling mechanism designed to trigger UI Toast notifications correctly. |
