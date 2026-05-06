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

### A. Core Validator: `schemaValidator.js` (12 Test Cases)
| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_VAL_01** | `schemaValidator.js` | `validateTableName` | Standard valid lowercase | `tableName = "users"` | Returns `isValid: true` | Pass | Validates correct execution path for standard lowercase string inputs. |
| **TC_FE_VAL_02** | `schemaValidator.js` | `validateTableName` | Standard valid snake_case | `tableName = "user_profiles"` | Returns `isValid: true` | Pass | Ensures snake_case naming conventions safely pass the validation logic. |
| **TC_FE_VAL_03** | `schemaValidator.js` | `validateTableName` | Empty string check | `tableName = ""` | Returns Error: "Table name cannot be empty" | Pass | Prevents core failure by intercepting null or empty string value submissions. |
| **TC_FE_VAL_04** | `schemaValidator.js` | `validateTableName` | Special characters check | `tableName = "users@123"` | Returns Error: "Table name contains invalid special characters" | Pass | Enforces structural integrity by rejecting non-alphanumeric/underscore variables mapping. |
| **TC_FE_VAL_05** | `schemaValidator.js` | `validateTableName` | Starts with number | `tableName = "1users"` | Returns Error: "Table name cannot start with a number" | Fail | Adheres to strict SQL dialect naming conventions avoiding numeric prepends. |
| **TC_FE_VAL_06** | `schemaValidator.js` | `validateTableName` | Exceeds max length limit | `tableName` > 64 chars | Returns Error: "Table name exceeds maximum length" | Pass | Database bounding limit verification to protect the persistence layer limits. |
| **TC_FE_VAL_07** | `schemaValidator.js` | `validateTableName` | Duplicate table name | `tableName = "orders"`, existing tables array | Returns Error: "Table name already exists" | Pass | Traverses the current local state arrays ensuring deduplication of table identifiers. |
| **TC_FE_VAL_08** | `schemaValidator.js` | `validateColumnName` | Valid column name | `colName = "id"` | Returns `isValid: true` | Pass | Asserts positive integration branch for simple generic column definitions. |
| **TC_FE_VAL_09** | `schemaValidator.js` | `validateColumnName` | Empty column name | `colName = ""` | Returns Error: "Column name cannot be empty" | Pass | Baseline security boundary to intercept zero-length strings on column creation. |
| **TC_FE_VAL_10** | `schemaValidator.js` | `validateColumnName` | Duplicate column in same table | `colName = "email"`, existing columns array | Returns Error: "Column name already exists in this table" | Pass | Scopes duplication checking rigidly within the designated target table entity. |
| **TC_FE_VAL_11** | `schemaValidator.js` | `validateRelationship` | Valid FK to PK | Source config (FK) & target config (PK) | Returns `isValid: true` | Pass | Validates perfect nominal mapping scenarios representing healthy edge connections. |
| **TC_FE_VAL_12** | `schemaValidator.js` | `validateRelationship` | Data type mismatch | Source = `INT`, Target = `VARCHAR` | Returns Error: "Foreign key data type must match Primary key" | Fail | Evaluates strict type casting validation preventing corrupted relationships on export. |

### B. Core Utility: `diagramUtils.js` (12 Test Cases)
| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_UTIL_01** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing single Node (no edges) | Array with 1 node mock | Valid JSON: `{"tables": [{...}], "relationships": []}` | Pass | Simulates edge-case workflows evaluating a lone structural entity serialization payload. |
| **TC_FE_UTIL_02** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing 2 Nodes with 1 Edge | Array with 2 nodes, 1 edge mock | Valid JSON showing `relationships` properly mapped | Pass | Full pathway verification to guarantee complex multi-node structure graph serialization. |
| **TC_FE_UTIL_03** | `diagramUtils.js` | `generateJSONFromNodes` | Parsing empty canvas | Nodes: `[]`, Edges: `[]` | JSON returns `{ "tables": [], "relationships": [] }` | Pass | Evaluates pure fallback handling routines intercepting null or entirely empty workspace requests. |
| **TC_FE_UTIL_04** | `diagramUtils.js` | `generateJSONFromNodes` | Node missing identity ID | Node lacking `id` | Throws Error: "Missing mandatory Node ID" | Pass | Secures internal DOM node mapping functionality by verifying identity payload constraints. |
| **TC_FE_UTIL_05** | `diagramUtils.js` | `generateJSONFromNodes` | Node missing type variable | Node lacking `type` | Defaults to 'table' type processing | Pass | Ensures robust failover values are seamlessly applied preventing node rendering crashes. |
| **TC_FE_UTIL_06** | `diagramUtils.js` | `generateJSONFromNodes` | Node dataset missing Table Name | Node missing `data.tableName` | Assigns generic name 'untitled_table' or throws Error | Pass | Validates safe fallback routines for missing attribute parameters within the object dataset payload. |
| **TC_FE_UTIL_07** | `diagramUtils.js` | `generateJSONFromNodes` | Edge lacking Source Node ID | Edge missing `source` ID | Ignores/Filters out the edge from relationships | Pass | Tests the filtration logic executing garbage collection for incomplete connecting handles. |
| **TC_FE_UTIL_08** | `diagramUtils.js` | `generateJSONFromNodes` | Redundant/Duplicate Edges | Array with overlapping source/target items | JSON removes duplicate relationship definitions | Pass | Implements aggressive array deduplication strategies minimizing overlapping query generation bloat. |
| **TC_FE_UTIL_09** | `diagramUtils.js` | `generateJSONFromNodes` | Nodes with complex attributes | Attributes (PK, FK, Normal) mapping | JSON correctly tags column types and constraints (NOT NULL) | Pass | Triggers deep complex object serialization effectively translating component mappings to output shapes. |
| **TC_FE_UTIL_10** | `diagramUtils.js` | `generateJSONFromNodes` | Nested relationships (A->B->C) | 3 Nodes sequentially connected | JSON structure maps foreign keys appropriately across tables | Pass | Advanced tree depth traversals confirming sequential link tracking operations map perfectly locally. |
| **TC_FE_UTIL_11** | `diagramUtils.js` | `generateJSONFromNodes` | Coordinate Extraction | Node coordinates = `x:100.5, y:20` | Converts/Rounds position safely to integers | Pass | Assesses explicit float variable truncation routines converting coordinates to raw safe integer maps. |
| **TC_FE_UTIL_12** | `diagramUtils.js` | `parseJSONToNodes` | Valid DB structure to Canvas Nodes | Valid JSON response payload | Outputs React Flow compatible `nodes`/`edges` array | Pass | Confirms backward pipeline pathway seamlessly reconstructing native React Flow graphical layouts perfectly. |

### C. Other Core Engine Files (16 Test Cases)
*(Includes sqlGenerator.js, canvasReducer.js, llmParser.js)*

| Test case ID | File name | Method name | Purpose | Input | Expected output | Test Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_FE_SQL_01** | `sqlGenerator.js` | `generateSQL` | Empty workspace | Empty nodes array | Returns empty string `""` | Pass | Essential baseline script validating return objects gracefully default to empty syntax parameters safely. |
| **TC_FE_SQL_02** | `sqlGenerator.js` | `generateSQL` | Standard single table layout | Modded node (id, username) | Returns `CREATE TABLE username (...)` string block | Pass | Tests initial dynamic string aggregation functions transforming basic data structures to SQL dialects. |
| **TC_FE_SQL_03** | `sqlGenerator.js` | `generateSQL` | FK Relationships existing | 2 Nodes, 1 Edge | Appends `ALTER TABLE ... ADD CONSTRAINT` | Fail | Intercepts advanced multi-module constraint generations for relational referential integrity mapping exports. |
| **TC_FE_SQL_04** | `sqlGenerator.js` | `generateSQL` | Column Data Mapping: INT | Field type `int` | Translates into SQL `INTEGER` | Pass | Cross-checks basic fundamental type conversions mapping universal UI types into strict SQL equivalents. |
| **TC_FE_SQL_05** | `sqlGenerator.js` | `generateSQL` | NotNull & Primary Key | Config `primary=true` | Adds `PRIMARY KEY`, `NOT NULL` strings | Pass | Verifies critical strict schema integrity directives inherently map successfully into script payload results. |
| **TC_FE_SQL_06** | `sqlGenerator.js` | `generateSQL` | SQL Reserved Keyword avoidance | Table named 'Order' | Wraps word in backticks `` `Order` `` to avoid DB syntax crashes | Pass | Enforces stringent syntax safety routines heavily mitigating potential native external database compiling crashes. |
| **TC_FE_RED_01** | `canvasReducer.js` | `ADD_NODE` | Action adding standard table | `action.payload` containing new table obj | Local state `nodes` increments by 1 | Pass | Asserts fundamental foundational state mutability behavior accurately indexing new component scopes seamlessly. |
| **TC_FE_RED_02** | `canvasReducer.js` | `ADD_NODE` | Blocking duplicated IDs | Payload carrying pre-existing ID | State remains strictly unchanged | Pass | Validates defensive memory protection systems preventing component overlap avoiding downstream logic anomalies entirely. |
| **TC_FE_RED_03** | `canvasReducer.js` | `REMOVE_NODE` | Deleting a core relationship Node | Payload Delete Action + ID | Node is erased; **ALL** referring edges are cascade-deleted | Fail | Assesses sophisticated cascade orchestration routines destroying isolated component branches ensuring state memory remains completely unpolluted. |
| **TC_FE_RED_04** | `canvasReducer.js` | `ADD_EDGE` | Drawing standard relational Edge | Mock event drawing A to B | State `edges` incremented | Pass | Confirms directional linking paths append effectively simulating precise user interface edge connection configurations. |
| **TC_FE_RED_05** | `canvasReducer.js` | `CLEAR_CANVAS` | Wipe operation | Empty payload Action | Wipes entire application scope (nodes:[], edges:[]) | Pass | Ensures factory resetting methods trigger precise payload wipes obliterating total application contexts rapidly for cache clearing tasks. |
| **TC_FE_LLM_01** | `llmParser.js` | `parseLLMResult` | Extracting raw standard valid JSON | Code block markdown response | Identifies JSON, returns structured object | Pass | Tests central core AI block isolation systems perfectly differentiating raw configuration data variables from conversational string syntax patterns. |
| **TC_FE_LLM_02** | `llmParser.js` | `parseLLMResult` | Extracting non-markdown bare JSON | Pure JSON string | Still correctly outputs structured object array | Pass | Validates flexible fallback AI pattern string identification correctly circumventing unformatted responses efficiently decoding standard text arrays naturally. |
| **TC_FE_LLM_03** | `llmParser.js` | `parseLLMResult` | Hallucination error interception | Broken JSON (missing closing brace `}`) | Exception caught, throws "Malformed LLM Data Structure" | Pass | Implements crucial heuristic boundaries detecting fragmented intelligence queries successfully maintaining system-level exception tracking catching capabilities natively. |
| **TC_FE_LLM_04** | `llmParser.js` | `parseLLMResult` | AI pre/post-text extraction | AI replies "Here is the result... { JSON } ... Enjoy!" | Uses regex to isolate and extract solely the JSON bracket block | Pass | Advanced string shielding methodologies deployed verifying algorithmic regex extractions systematically eliminating artificial conversational responses flawlessly bypassing noise. |
| **TC_FE_LLM_05** | `llmParser.js` | `sanitizeLLMNames` | Stripping unicode/accented letters | Table proposed as "Khách Hàng" | Formats out into raw ANSI output: "khach_hang" | Fail | Employs stringent deep regex string scrubbing functions targeting multilingual characters correctly standardizing variables adhering exclusively to global English format conventions. |

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

*Note: Run `npm run test -- --coverage` to execute all tests. Coverage must reach **≥ 80%** to pass the grading criteria.*

---
