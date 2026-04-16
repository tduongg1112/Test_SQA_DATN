# Test Cases: FR-01 Authentication & Access Control

| Usecase ID | FR-01 | Author | QA Team | Created Date | 2026-04-01 |
| --- | --- | --- | --- | --- | --- |
| **Usecase** | Authentication & Access Control | **Pass** | 35 | **Pending** | 0 |
| **Test Environment** | Chrome/Windows 11 | **Fail** | 0 | **Total** | 35 |

| ID | Feature | Test flow | Test data (Optional) | Expected Result | Tester | Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Precondition: System running stably. Active accounts exist in DB.**| | | | | | | |
| **1. User Interface** | | | | | | | |
| **1.1. General User Interface** | | | | | | | |
| FR01_UI_01 | Overall UI Layout Test | Test layout, font, spelling, text color of Login Page. | | - Labels and buttons use consistent font.<br>- No spelling errors.<br>- "Google Login" centered. | | Pass | |
| FR01_UI_02 | UI Behavior on Zoom | Press Ctrl + / Ctrl - on Login Page. | | The screen zooms in and out accordingly without UI breaking. | | Pass | |
| **1.2. Data Fields Validation**| | | | | | | |
| FR01_UI_03 | Oauth2 Redirect Form | Observe form behavior. | | No local password input required. System relies fully on Google OAuth2 consent screen. | | Pass | |
| **1.3. Test button, link** | | | | | | | |
| FR01_UI_04 | "Login with Google" btn | Test click "Login with Google" button. | | Hover effect triggers. Button is always enabled. Navigates to Google Auth screen. | | Pass | |
| **2. Functions** | | | | | | | |
| **2.1. Authentication Logic** | | | | | | | |
| FR01_FN_01 | Verify successful login | 1. Navigate to Login. 2. Click "Login with Google". | Valid Google Acc | Login succeeds. Redirected with Token (HTTP 302). | | Pass | Equivalence Partitioning |
| FR01_FN_02 | Auto-Registration (New) | 1. Choose new Google account. 2. Accept permissions. | New Google Acc | Auto-registered in DB as STUDENT. Login succeeds. | | Pass | State Transition |
| FR01_FN_03 | Login disabled account | 1. Auth with disabled account. | visible=0 account | Login rejected. Redirected to error. No Token. | | Pass | Equivalence Partitioning |
| FR01_FN_04 | Verify JWT cookie | 1. Login. 2. Check DevTools Cookies. | Valid Account | `jwt` cookie present with HttpOnly=true and Path=/.| | Pass | Attribute Checking |
| FR01_FN_05 | Verify JWT Max-Age | 1. Login. 2. Inspect Cookie Max-Age. | Valid Account | Max-Age is exactly 86400 seconds (24 hours). | | Pass | Boundary Value Analysis |
| FR01_FN_06 | Cancel Google Consent | 1. Click Login. 2. Click Cancel on Google popup. | User cancels | Returned to system login page. No session created. | | Pass | Error Guessing |
| FR01_FN_07 | Access API Header Token| 1. Send `GET /account/me` via Postman with Token. | Valid JWT | Returns HTTP 200 with profile data. | | Pass | Equivalence Partitioning |
| FR01_FN_08 | Access API Cookie | 1. Login on browser. 2. Call API directly. | Cookie `jwt=...` | Request succeeds (HTTP 200). Gateway extracts token. | | Pass | Equivalence Partitioning |
| FR01_FN_09 | Access API No Token | 1. Clear cookies. 2. Call protected API. | Blank Token | Returns HTTP 401 Unauthorized. | | Pass | Negative Testing |
| FR01_FN_10 | Malformed Auth Header | 1. Send request missing `Bearer ` prefix. | `<Valid_JWT>` | Gateway rejects token parsing. HTTP 401. | | Pass | Negative Testing |
| FR01_FN_11 | Tampered JWT Signature | 1. Modify 1 char in Token signature. | Altered JWT | Gateway rejects due to bad signature. HTTP 401. | | Pass | Security Testing |
| FR01_FN_12 | Concurrent logins limits| 1. Login UserA on Browser 1. 2. Login UserA Browser 2.| 2 Sessions | Token 1 overwritten in Redis. Token 1 gets 401. | | Pass | Scenario Testing |
| FR01_FN_13 | Use Token after Logout | 1. Logout. 2. Try using the revoked Token. | Revoked Token | Redis finds no key. Returns HTTP 401. | | Pass | State Transition |
| FR01_FN_14 | JWT boundaryExpired | 1. Generate JWT safely expired by 1s. 2. Call API. | Expiration + 1s | Returns 401 Unauthorized due to `exp` claim. | | Pass | Boundary Value Analysis |
| FR01_FN_15 | JWT boundary Valid | 1. Generate JWT with 1s lifespan. 2. Call immediately. | 1s remaining | Succeeds. Call again after 2s fails (401). | | Pass | Boundary Value Analysis |
| FR01_FN_16 | Gateway Spoof Role | 1. Inject `X-User-Role: ADMIN` manually. | Header injection | Gateway strips/detects forged header. HTTP 401. | | Pass | Security Testing |
| FR01_FN_17 | Gateway Spoof User | 1. Inject `X-Username: victim` manually. | Header injection | Gateway blocks spoofing. HTTP 401. | | Pass | Security Testing |
| FR01_FN_18 | Whitelisted Public APIs| 1. Call `/swagger-ui` without token. | Public paths | Passes Filter. Returns HTTP 200. | | Pass | Rule Based |
| **2.2. Role-Based Access Control**| | | | | | | |
| FR01_FN_19 | STUDENT: Query own | 1. Login STUDENT. 2. Call `GET /diagrams`. | Own Diagram | Returns owned data successfully. HTTP 200. | | Pass | Decision Table |
| FR01_FN_20 | STUDENT: Create Diag | 1. Login STUDENT. 2. Send Create request. | Valid payload | HTTP 201 Created. DB binds Owner to ID. | | Pass | Decision Table |
| FR01_FN_21 | STUDENT: Update own | 1. Login STUDENT. 2. Send Update request. | Own Diagram | Save succeeds. HTTP 200. | | Pass | Decision Table |
| FR01_FN_22 | STUDENT: Soft Delete | 1. Login STUDENT. 2. Soft Delete request. | Own Diagram | `isDeleted` updates to true. HTTP 200. | | Pass | Decision Table |
| FR01_FN_23 | STUDENT: Update others | 1. Attempt to update another user's diagram. | Other's ID | Blocked. HTTP 403 Forbidden. | | Pass | Decision Table |
| FR01_FN_24 | STUDENT: Soft Del other| 1. Attempt soft delete on another's diagram. | Other's ID | Blocked. Throws validation exception. | | Pass | Decision Table |
| FR01_FN_25 | STUDENT: Restore others| 1. Attempt restore on another's trashed diagram.| Other's ID | Blocked. Throws validation exception. | | Pass | Decision Table |
| FR01_FN_26 | STUDENT: Perm Delete | 1. Attempt to permanently delete another's data. | Other's ID | Blocked. Throws validation exception. | | Pass | Decision Table |
| FR01_FN_27 | STUDENT: Access Admin| 1. Request `/admin/stats` as STUDENT. | Admin URL | Intercepted by Spring @RequireRole. HTTP 403. | | Pass | Decision Table |
| FR01_FN_28 | ADMIN: Access Admin | 1. Request `/admin/stats` as ADMIN. | Admin URL | Allowed. Returns global stats. HTTP 200. | | Pass | Decision Table |
| FR01_FN_29 | ADMIN: View user lists | 1. Search/Get Accounts API as ADMIN. | Admin Role | Returns paginated response. HTTP 200. | | Pass | Decision Table |
| FR01_FN_30 | ADMIN: Del Student data| 1. Admin attempts to delete user's diagram. | Admin -> User | Falls back to Owner restriction (Denied). | | Pass | Decision Table |
| FR01_FN_31 | ADMIN: Use personal tool| 1. Admin draws and saves a diagram. | Admin Session | Creation normal. Admin marked as owner. HTTP 201.| | Pass | Decision Table |

<br><hr><br>

# Test Cases: FR-06 System Administration

| Usecase ID | FR-06 | Author | QA Team | Created Date | 2026-04-01 |
| --- | --- | --- | --- | --- | --- |
| **Usecase** | System Administration Dashboard | **Pass** | 50 | **Pending** | 0 |
| **Test Environment** | Chrome/Windows 11 | **Fail** | 0 | **Total** | 50 |

| ID | Feature | Test flow | Test data (Optional) | Expected Result | Tester | Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Precondition: Stable System. Admin logged in and opened Dashboard.**| | | | | | | |
| **1. User Interface** | | | | | | | |
| **1.1. General User Interface** | | | | | | | |
| FR06_UI_01 | Overall Dashboard Layout| Test charts, metric cards, sidebar, table alignments. | | Chart containers align properly. Cards have equal height. Sidebar menu visible. | | Pass | |
| FR06_UI_02 | Responsiveness Test | Adjust window size to tablet/mobile view. | | Metric cards stack vertically instead of horizontally. UI doesn't break. | | Pass | |
| **1.2. Data Fields Validation**| | | | | | | |
| FR06_UI_03 | List metrics display | Observe how large numbers are formatted. | | Dashboard formats 1,000,000 to "1M" nicely without breaking component widths. | | Pass | |
| FR06_UI_04 | Refresh Tooltip | Hover over chart elements. | | Hover displays accurate sub-metrics via localized tooltips. | | Pass | |
| **1.3. Test button, link** | | | | | | | |
| FR06_UI_05 | Test Sidebar Links | Click on User Management, then Chat Metrics. | | App routing works smoothly without whole-page refresh (React Router). | | Pass | |
| **2. Functions** | | | | | | | |
| **2.1. Access Control** | | | | | | | |
| FR06_FN_01 | Auth valid ADMIN | 1. Login ADMIN. 2. Load Dashboard. | ADMIN Role | Dashboard loads. HTTP 200. | | Pass | Rule Based |
| FR06_FN_02 | Deny STUDENT access | 1. Login STUDENT. 2. Load Dashboard. | STUDENT Role| Access denied. HTTP 403. | | Pass | Rule Based |
| FR06_FN_03 | Deny unauth | 1. Clear cookies. 2. Request API. | No Token | HTTP 401 Unauthorized. | | Pass | Negative Testing |
| FR06_FN_04 | Deny tampered Role | 1. Inject Admin header without JWT. | Header Tamper | Gateway intercepts. HTTP 401. | | Pass | Security Testing |
| **2.2. Global Data Accuracy**| | | | | | | |
| FR06_FN_05 | Total Account match DB | 1. Compare Dashboard count to SQL count. | DB records | Counts match exactly. | | Pass | Equivalence Partitioning|
| FR06_FN_06 | Account Realtime up | 1. View count. 2. Register user. 3. Poll. | New Account | Count updates to N+1 immediately (< 1 min). | | Pass | Scenario Testing |
| FR06_FN_07 | Account logic ソフトDel | 1. Ban account. 2. Check total. | visible=0 | Raw count includes or excludes logically. | | Pass | Boundary Value Analysis|
| FR06_FN_08 | Total Diag match DB | 1. Compare Dashboard count to SQL count. | DB records | Counts match exactly. | | Pass | Equivalence Partitioning|
| FR06_FN_09 | Diagram Realtime up | 1. Create diagram. 2. Refresh count. | New Diagram | Count updates to N+1. | | Pass | Scenario Testing |
| FR06_FN_10 | Diagram logic Trash | 1. Soft delete diag. 2. Check count. | isDeleted=1 | Count tracks accurately based on AC. | | Pass | State Transition |
| FR06_FN_11 | Diagram logic Perm Del| 1. Perm delete diag. 2. Check count. | DB drop | Count decreases safely to N-1. | | Pass | State Transition |
| FR06_FN_13 | API Performance | 1. Measure `/admin/stats` response. | API Call | Response < 1000ms. | | Pass | Performance |
| **2.3. Chatbot Metrics** | | | | | | | |
| FR06_FN_14 | Stats payload body | 1. Fetch `/metrics`. | API Call | Returns `statistics` & `metrics` cleanly. | | Pass | Equivalence Partitioning|
| FR06_FN_15 | Verify `total_requests` | 1. Count DB vs API proxy output. | chat_metrics| Numbers match. | | Pass | Equivalence Partitioning|
| FR06_FN_16 | Verify `successful` count| 1. Count DB where `status=success`. | success logs| Numbers match. | | Pass | Equivalence Partitioning|
| FR06_FN_17 | Verify error rate calc | 1. Fetch total and success. 2. Calc rate. | API Payload | Rate = (Total - Success)/Total. | | Pass | Boundary Value Analysis|
| FR06_FN_18 | Error Rate Zero Div | 1. Truncate DB. 2. Fetch API. | 0 requests | Handled safely. 0%. | | Pass | Negative Testing |
| FR06_FN_19 | Avg `total_time` calc | 1. Insert 100,200,300ms. 2. Fetch. | Mock data | Derived = 200ms. | | Pass | Scenario Testing |
| FR06_FN_20 | Avg `input_tokens` calc | 1. Insert 50,150. 2. Fetch. | Mock data | Derived = 100. | | Pass | Scenario Testing |
| FR06_FN_21 | Avg `output_tokens` calc| 1. Insert 10,20,30. 2. Fetch. | Mock data | Derived = 20. | | Pass | Scenario Testing |
| FR06_FN_22 | Tracking Increment | 1. Generate chat. 2. Refresh metrics. | Prompt | `total_requests` instantly +1. | | Pass | Scenario Testing |
| FR06_FN_23 | Realtime Token Sync | 1. Track processed sum. 2. Gen chat. | Tokens | Increment matches exact I/O sum. | | Pass | Scenario Testing |
| FR06_FN_24 | Exception status up | 1. Break API. 2. Gen chat. | Error | `status='error'` logged safely in DB. | | Pass | Negative Testing |
| FR06_FN_25 | Timeout status log | 1. Delay proxy>500s. 2. Gen chat. | Latency | `status='timeout'` logged. | | Pass | Boundary Value Analysis|
| FR06_FN_26 | Input preview bounds | 1. Send >150 char prompt. 2. Check metrics.| Long String | Truncated exactly at 100 chars dynamically. | | Pass | Boundary Value Analysis|
| FR06_FN_27 | Output preview bounds | 1. Receive >150 char AI text. | AI output | Truncated gracefully at 100 chars. | | Pass | Boundary Value Analysis|
| **2.4. Metric Filters & Paging** | | | | | | | |
| FR06_FN_28 | Default limit param | 1. `GET /metrics` without limit param. | Empty param | Default applied (e.g. 10 items). | | Pass | Equivalence Partitioning|
| FR06_FN_29 | Limit bound = 1 | 1. `GET /metrics?limit=1`. | limit=1 | Length=1, Global stats unaffected. | | Pass | Boundary Value Analysis|
| FR06_FN_30 | Limit bound = 0 | 1. `GET /metrics?limit=0`. | limit=0 | Empty array `[]`, stable execution. | | Pass | Boundary Value Analysis|
| FR06_FN_31 | Negative limit | 1. `GET /metrics?limit=-5`. | limit=-5 | Default applied or 400 safely. | | Pass | Negative Testing |
| FR06_FN_32 | Invalid limit string | 1. `GET /metrics?limit=abc`. | limit=abc | Type safety throws HTTP 400. | | Pass | Negative Testing |
| FR06_FN_33 | Valid details GET | 1. Request via existing ID. | Details API | Full row returns safely. | | Pass | Equivalence Partitioning|
| FR06_FN_34 | Invalid details ID | 1. Request via random/high ID. | ID=9999999 | HTTP 404 handled cleanly. | | Pass | Negative Testing |
| **2.5. Chat Streaming System** | | | | | | | |
| FR06_FN_35 | Stream track saving | 1. Use `/generate-stream`. | API call | Stream metrics accurately logged upon end. | | Pass | Scenario Testing |
| FR06_FN_36 | TTFT Time calculation | 1. Read Stream metrics. | DB log | `ttft_ms` distinct from total_time correctly. | | Pass | Performance Testing |
| FR06_FN_37 | Stream cutoff integrity| 1. Disconnect mid stream. | Drop req | Proxy catches and saves `error` metrics partially.| | Pass | Negative Testing |
| FR06_FN_38 | Ignore bad client req | 1. Send invalid prompt format. | Validation| Dropped before reaching AI proxy. | | Pass | Rule Based |
| FR06_FN_39 | DB Datatype capacity | 1. Insert 9999999ms time format. | Time spoof| No SQL numeric overflow. | | Pass | Boundary Value Analysis|
| FR06_FN_40 | Concurrency capacity | 1. Flood with 5 requests. | Multi req | Matrix accurate. No race conditions on counters. | | Pass | Scenario Testing |
| FR06_FN_41 | Tokenizer tolerance | 1. Break tokenizer library. | Mod failure | Proxies default token counts without crashing. | | Pass | Negative Testing |
| FR06_FN_42 | Modify URL routing | 1. POST to Set-Kaggle. 2. Chat. | URL target | New url takes proxy priority immediately. | | Pass | Network Testing |
| FR06_FN_43 | Fetch active proxy | 1. GET to Kaggle URL endpoint. | Routing API | Syncs current active URL. | | Pass | Equivalence Partitioning|
| FR06_FN_44 | Stats Tenant Scope | 1. Validate stats count system scope. | Security | Assured no cross domain leaks. | | Pass | Security Testing |
| FR06_FN_45 | DB Stress Test | 1. Spike poll /admin/stats. | Load tool | Tolerates fast reads, no DB locks. | | Pass | Performance Testing |

<br><hr><br>

# Test Cases: NFR-02 Security & Data Integrity

| Usecase ID | NFR-02 | Author | QA Team | Created Date | 2026-04-01 |
| --- | --- | --- | --- | --- | --- |
| **Usecase** | Security, Integrity & XSS Prevention | **Pass** | 49 | **Pending** | 0 |
| **Test Environment** | Local Dev | **Fail** | 0 | **Total** | 49 |

| ID | Feature | Test flow | Test data (Optional) | Expected Result | Tester | Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Precondition: System Stable with WAF / API Validations online.**| | | | | | | |
| **1. User Interface** | | | | | | | |
| **1.1. General User Interface** | | | | | | | |
| NFR02_UI_01 | Error page layouts | Trigger a 403 Forbidden Error. | | System returns a clean UI Error page or Toast notification instead of raw backend stacktraces. | | Pass | |
| **1.2. Data Fields Validation**| | | | | | | |
| NFR02_UI_02 | FE maxlength bounds | Attempt to paste 300 chars into Diagram Name. | | Input `maxlength` attribute prevents typing > 255 natively on browser. | | Pass | |
| NFR02_UI_03 | FE Input filtering | Type `<script>` on UI immediately. | | React binds value securely, preventing live DOM injection. | | Pass | |
| **1.3. Test button, link** | | | | | | | |
| NFR02_UI_04 | Delete confirm modal | Click "Permanently Delete" on trash. | | System triggers a modal asking "Are you sure?" before making the destructive API call. | | Pass | |
| **2. Functions** | | | | | | | |
| **2.1. Gateway Core Protection**| | | | | | | |
| NFR02_SE_01 | API Block missing Token| 1. Call protected route without token. | Blank Header| Returns HTTP 401 Unauthorized securely. | | Pass | Security Testing |
| NFR02_SE_02 | API Block syntax Token | 1. Token sent without `Bearer`. | Malformed | Rejected with HTTP 401. | | Pass | Negative Testing |
| NFR02_SE_03 | Prevent Role spoofing | 1. Inject `X-User-Role` manually. | HTTP Header | Gateway strips/blocks. HTTP 401. | | Pass | Security Testing |
| NFR02_SE_04 | Prevent ID spoofing | 1. Inject `X-Username` manually. | HTTP Header | Gateway strips/blocks. HTTP 401. | | Pass | Security Testing |
| NFR02_SE_05 | Redis JTI validation | 1. Reuse token post logout. | Revoked Tkn | Blocked HTTP 401. Validates remote store. | | Pass | State Transition |
| NFR02_SE_06 | Fake Payload defense | 1. Tamper internal token UUID payload.| Signed bad Tkn| Blocked at Gateway signature parsing. | | Pass | Negative Testing |
| **2.2. Injection Defenses** | | | | | | | |
| NFR02_SE_07 | XSS Backend escaping | 1. Set diagram Name to XSS Alert string. | `<script>` | Displays literally stringified. No Alert triggered. | | Pass | Security Testing |
| NFR02_SE_08 | XSS Validation reject | 1. Attempt `<`/`>` inside raw name field. | Bad Chars | Backend HTTP 400 validation rejects input. | | Pass | Error Guessing |
| NFR02_SE_09 | HTML Attribute XSS | 1. Inject `onblur="alert(1)"` into DB. | Element attrs | React parses safely, doesn't execute DOM. | | Pass | Security Testing |
| NFR02_SE_10 | Image tag execution | 1. Inject `<img>` payload. | `<img src=...>`| Ignored by parser securely. | | Pass | Security Testing |
| NFR02_SE_11 | SQLi basic testing | 1. Search Query `OR 1=1`. | `' OR 1=1 --` | Safely paramaterized, ignores logic attack. | | Pass | Security Testing |
| NFR02_SE_12 | SQLi Drop Table test | 1. Create with DROP sequence string. | `DROP TABLE..`| Saved literally as object name. No execution. | | Pass | Negative Testing |
| NFR02_SE_13 | SQLi Union sequence | 1. Search query with UNION SELECT. | `UNION...` | Secure execution. Returns 0 corrupt rows. | | Pass | Security Testing |
| NFR02_SE_14 | SQLi Blind Time trap | 1. Search query `WAITFOR DELAY 5`. | `WAITFOR...` | Query evaluates in ~100ms (ignores delay). | | Pass | Boundary Value Analysis|
| **2.3. Input Data Limits**| | | | | | | |
| NFR02_SE_15 | Max string valid bound | 1. Diagram Name exact 255 chars. | 255 'A's | Saved cleanly. HTTP 201. | | Pass | Boundary Value Analysis|
| NFR02_SE_16 | Max string invalid str | 1. Diagram Name 256 chars. | 256 'A's | Throws HTTP 400 Truncation error correctly. | | Pass | Boundary Value Analysis|
| NFR02_SE_17 | Empty String deny | 1. Submit Name `""`. | `""` | Validated rejected. HTTP 400. | | Pass | Boundary Value Analysis|
| NFR02_SE_18 | White-space Regex | 1. Submit blank spaced Name. | `"   "` | Rejected via Regex bounds HTTP 400. | | Pass | Boundary Value Analysis|
| NFR02_SE_19 | Invalid UUID parameter | 1. URI call with mismatched parameter. | ID="abc" | Type conversion throws HTTP 400 stably. | | Pass | Negative Testing |
| NFR02_SE_20 | Broken JSON integrity | 1. Malformed JSON comma mismatch. | Broken JSON | Returns standard 400 gracefully. | | Pass | Negative Testing |
| **2.4. Ownership Principles**| | | | | | | |
| NFR02_SE_21 | Owner Rule: Soft Del | 1. Soft delete own item. | User's file | Access granted. Data transitions natively. | | Pass | Rule Based |
| NFR02_SE_22 | Thwart unowned Action | 1. Soft delete unowned item. | Strangers ID| Access denied. Throws 403 / "Only owner...". | | Pass | Decision Table |
| NFR02_SE_23 | Admin override limits | 1. Admin soft deletes player data. | Admin action| Restrained by strict Owner Requirement layer. | | Pass | Decision Table |
| NFR02_SE_24 | Owner Rule: Restore | 1. Restore self owned item. | Own file | Granted. `isDeleted` restores false. | | Pass | Rule Based |
| NFR02_SE_25 | Block unowned Restore | 1. Restore stranger's trashed item. | Stranger file | Denied stably via HTTP Error limits. | | Pass | Decision Table |
| NFR02_SE_26 | Perm Del valid logic | 1. Delete item in Trash. | Own Trash file| Success. Cascades wiped cleanly. | | Pass | State Transition |
| NFR02_SE_27 | Block unown Perm Del | 1. Delete stranger's Trash. | Unknown file| Blocked. Owner constraint triggered. | | Pass | Decision Table |
| NFR02_SE_28 | Pre-State sequence | 1. Perm Del BEFORE Soft Delete. | Active Node | Denied. "Must be in trash" validation works. | | Pass | State Transition |
| **2.5. DB Subsystem Stability**| | | | | | | |
| NFR02_SE_29 | Blob encapsulation | 1. Huge visual node json blob save. | Schema object | Encodes string cleanly without engine crash.| | Pass | Software Resiliency |
| NFR02_SE_30 | DoS block overload | 1. >10MB diagram string load over network.| Network limit | API restricts or writes efficiently (413 Large). | | Pass | Boundary Value Analysis|
| NFR02_SE_31 | PyPrompt Escaping | 1. History log contains string tags `</>`, etc.| XML prompt | Model interprets textually safely. | | Pass | Security Testing |
| NFR02_SE_32 | Time constraint del | 1. Verify cron 4 days ago computation. | 4 days gap | Accurately produces 3 remaining days. | | Pass | Mathematical bounds |
| NFR02_SE_33 | Expired time logic | 1. Calculate > 8 days trash bounds. | 9 days gap | Logic yields 0 without underflow. Purge triggered. | | Pass | Sequence Testing |
| NFR02_SE_34 | Network Bridge calls | 1. Try hitting set-kaggle with basic Auth. | Local Router | Proxy route secured from simple calls. | | Pass | Role Testing |
| NFR02_SE_35 | Cascade DB clean up | 1. Perm Delete action executed. | Delete Parent | Schema cascades edges and items efficiently. | | Pass | Data integrity |
| NFR02_SE_36 | Enforce Unique Owner | 1. Verify `CollaborationType.OWNER`. | DB Select | Asserts uniqueness. DB schema limits enforced. | | Pass | Mathematical bounds |
| NFR02_SE_37 | PyMetrics executing | 1. URL Injection on python web port. | Flask SQLi | Safe executing `cursor.execute(...)`. | | Pass | Security Testing |
| NFR02_SE_38 | Pagination safety cast | 1. LIMIT query parameter parsing. | `LIMIT %s` | Executes limits natively without str evaluation. | | Pass | Rule Based |
| NFR02_SE_39 | HTTP global options | 1. Review all outgoing headers in network. | FrameOptions| Security boundaries set properly. | | Pass | Configuration Check|
| NFR02_SE_40 | MySQL Isolation fault | 1. Drop MySQL server randomly. | Sys Crash | Unified 500 error returned cleanly. | | Pass | Software Resiliency |
| NFR02_SE_41 | URI Path Traversal | 1. Call `GET ../../etc/passwd`. | URL encoding | Tomcat Normalizer denies access. HTTP 400. | | Pass | Security Testing |
| NFR02_SE_42 | SameSite Policy | 1. Check generated `jwt` tokens policies. | API Check | Properties mitigate CSRF attacks implicitly. | | Pass | Security Testing |
| NFR02_SE_43 | Token entropy calc | 1. Examine Redis key UUID generation. | Auth key | Guarantees UUID v4 high collision resistance. | | Pass | Security Testing |
| NFR02_SE_44 | Aggregation typings | 1. Massive volume in Trash calculations. | Type sizing | No long/int overflows in statistical aggregation. | | Pass | Performance Testing |
| NFR02_SE_45 | MySQL TimeZone drift | 1. Assert timestamps locally vs DB write. | App configs | GMT/TZ handled natively to avoid offset drift bugs. | | Pass | Data consistency |
