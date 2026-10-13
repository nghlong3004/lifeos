# PRODUCT REQUIREMENTS DOCUMENT (PRD)
## LifeOS — AI-Powered Personal Life Assistant

**Version:** 0.3.0 — ACVL + Goal Execution Revision  
**Ngày tạo:** 11/10/2026  
**Cập nhật:** 11/10/2026  
**Người cập nhật:** Long (nghlong3004)
**Trạng thái:** Planning — Ready for Technical Design (sau khi chốt Open Decisions)  
**Nền tảng:** Telegram Bot → Web Dashboard / Telegram Mini App  
**Backend dự kiến:** Java 21, Spring Boot, PostgreSQL  
**AI:** OpenAI Small Language Model (cấu hình được)

---

## Revision Summary — v0.3.0

Bản này kế thừa đầy đủ PRD v0.2.0 (bao gồm 6 nhóm cải tiến CRUD, tài chính, analytics, task lifecycle, AI safety và recovery), đồng thời bổ sung Goal Execution & Learning dựa trên các ý tưởng đã nghiên cứu từ LifeOS của Daniel Miessler. Đây là thiết kế độc lập cho ứng dụng Telegram, **không phải bản port/cài đặt repository tham khảo**.

Các hạng mục kế thừa từ v0.2.0:

1. **CRUD & Undo:** Thêm/xem/sửa/xóa mềm cho task, habit, account, transaction, budget, goal, milestone; có tra cứu đối tượng, xác nhận và lịch sử thay đổi.
2. **Finance Rules:** Công thức tiền có thể chi, hoàn tiền/hủy giao dịch, ngân sách qua ngày, tiền tiết kiệm dành riêng, sửa giao dịch quá khứ.
3. **Daily Analytics:** Biểu đồ 7/30 ngày và báo cáo ngày qua Telegram, không phụ thuộc Web Dashboard.
4. **Task Lifecycle:** One-time task độc lập, trạng thái cuối ngày, rollover, tiến độ cộng dồn/ghi đè, streak và snapshot lịch sử.
5. **AI Confirmation Policy:** Phân loại hành động an toàn, mơ hồ, nhạy cảm; xác nhận hàng loạt và kiểm soát đa ý định.
6. **Backup & Integrity:** Backup/restore, audit log, idempotency, kiểm thử recovery và đối soát số liệu.

**Mới trong v0.3.0:**

7. **ACVL — Articulate / Climb / Verify / Learn:** Chuyển mục tiêu thành định nghĩa hoàn thành, bước tiếp theo, xác minh có nguồn và vòng review có kiểm soát.
8. **Goal Card (ISA-lite):** Mẫu mục tiêu tối giản có Current State, Ideal State, Success Criteria, Verification Method, Next Action, trạng thái và version; không áp dụng nghi thức phức tạp cho các việc nhỏ.
9. **TELOS-lite & Goal Alignment:** Liên kết *lý do → mục tiêu → milestone → hành động*, phần thông tin cá nhân sâu là tùy chọn, không bắt buộc thu thập.
10. **Evidence-aware Reporting & Memory-lite:** Phân biệt tự báo cáo với dữ liệu backend và bằng chứng ngoài; lưu nhận xét đã xác nhận, không dùng giả thuyết AI làm sự thật.
11. **Scope Guardrails:** MVP chỉ có ACVL-lite trên goal, xác minh self-report và weekly reflection thủ công; kiểm chứng tự động bên ngoài, graph ngữ nghĩa, memory retrieval nâng cao và tự động tái lập kế hoạch là P1/P2.

**Quy ước:** `P0` = bắt buộc trong MVP; `P1` = cải tiến sau MVP hoặc bản rút gọn nếu nêu rõ; `P2` = giai đoạn nâng cao. Các quy tắc có ghi **mặc định đề xuất** cần được chốt ở mục 14 trước khi triển khai, không được hiểu là quyết định cuối cùng.

---

# 1. Product Overview

## 1.1. Giới thiệu

LifeOS là hệ thống trợ lý cá nhân ứng dụng AI, giúp người dùng quản lý, theo dõi và cải thiện các hoạt động trong cuộc sống hằng ngày.

Hệ thống tập trung vào 4 lĩnh vực:

1. **Daily Habits & Tasks:** Xây dựng và duy trì thói quen, kiểm soát công việc hằng ngày.
2. **Personal Finance:** Quản lý chi tiêu, ngân sách, số dư và mục tiêu tiết kiệm.
3. **Goals & Planning:** Xây dựng kế hoạch phát triển bản thân ngắn hạn và dài hạn.
4. **Analytics & Self-Improvement:** Đo lường tiến độ, phân tích dữ liệu và đưa ra đề xuất cải thiện.

Người dùng tương tác chủ yếu thông qua Telegram bằng ngôn ngữ tự nhiên. AI chịu trách nhiệm hiểu yêu cầu và chuyển đổi thành các hành động có cấu trúc. Backend Java thực hiện nghiệp vụ và quản lý dữ liệu.

Trong tương lai, hệ thống hỗ trợ Web Dashboard và Telegram Mini App mà không cần xây dựng lại core business logic.

## 1.2. Vấn đề cần giải quyết

Người dùng thường gặp khó khăn trong việc:

- Đặt nhiều mục tiêu nhưng không duy trì thực hiện đều đặn.
- Không nhớ hôm nay cần hoàn thành những đầu việc nào.
- Không có số liệu để đánh giá sự tiến bộ của bản thân.
- Chi tiêu thiếu kiểm soát, không biết thực tế còn được phép tiêu bao nhiêu.
- Lập kế hoạch dài hạn nhưng thiếu cơ chế chia nhỏ và theo dõi.
- Phải sử dụng nhiều ứng dụng riêng lẻ cho thói quen, tài chính, công việc và mục tiêu.
- Mất thời gian nhập liệu bằng các biểu mẫu phức tạp.

LifeOS giải quyết vấn đề bằng cách tập trung các hoạt động vào một hệ thống, với Telegram làm giao diện tương tác nhanh và AI giúp giảm thao tác nhập liệu.

## 1.3. Product Vision

> Một trợ lý cá nhân có khả năng hiểu mục tiêu, ghi nhận hoạt động, chủ động nhắc nhở và hỗ trợ người dùng đưa ra quyết định tốt hơn dựa trên dữ liệu của chính mình.

Sản phẩm không chỉ ghi nhận "hôm nay đã làm gì", mà còn biến mục tiêu thành **Definition of Done → Next Action → Evidence → Learning**, và giúp trả lời:

- Hôm nay tôi cần làm những gì?
- Tôi đang tiến bộ hay thụt lùi?
- Tại sao một thói quen không được duy trì?
- Tôi còn bao nhiêu tiền có thể chi tiêu?
- Tôi có đang đi đúng hướng với mục tiêu dài hạn?
- Tuần tiếp theo nên điều chỉnh kế hoạch như thế nào?

---

# 2. Target Users

## 2.1. Primary User

Người dùng cá nhân muốn quản lý cuộc sống, phát triển bản thân và tài chính bằng một trợ lý Telegram.

Trong phiên bản đầu tiên, sản phẩm được tối ưu cho một người sử dụng.

## 2.2. Future Users

Hệ thống cần có khả năng hỗ trợ nhiều người dùng trong tương lai, với dữ liệu, cấu hình, ngân sách và kế hoạch hoàn toàn độc lập.

## 2.3. User Goals

- Quản lý thói quen mỗi ngày trong thời gian ngắn.
- Tạo hoặc thay đổi công việc bằng tin nhắn tự nhiên.
- Kiểm soát chi tiêu mà không phải mở bảng tính.
- Nhận thông báo nhắc nhở đúng thời điểm.
- Theo dõi tiến độ theo tuần, tháng và năm.
- Nhận đề xuất lập kế hoạch từ AI.
- Truy cập dữ liệu dưới dạng Excel hoặc Web Dashboard.

---

# 3. Product Principles

**P1. Conversation First**

Người dùng có thể thực hiện các thao tác phổ biến bằng hội thoại, không bắt buộc sử dụng command.

**P2. Minimal Interaction**

Các hoạt động hằng ngày phải có thể cập nhật nhanh chóng thông qua Telegram Inline Keyboard hoặc tin nhắn ngắn.

**P3. AI Assists, Backend Decides**

AI hiểu ngôn ngữ, trích xuất ý định và đề xuất kế hoạch. Backend Java chịu trách nhiệm xác thực, tính toán và thực thi.

**P4. Data Ownership**

Dữ liệu được lưu trong database riêng và có thể xuất ra định dạng phổ biến.

**P5. Measurable Progress**

Các mục tiêu và nhiệm vụ nên được đo bằng số liệu cụ thể khi có thể.

**P6. Extensible Architecture**

Telegram, Web Dashboard và các giao diện tương lai sử dụng chung domain services.

**P7. Reversible by Default**

Các thay đổi thông thường hỗ trợ hoàn tác; xóa dữ liệu nghiệp vụ là soft delete và không làm biến mất lịch sử tài chính/analytics đã khóa.

**P8. Idempotent & Auditable Writes**

Mọi hành động ghi nhận có request identity, authorization, audit và bảo đảm không nhân đôi khi Telegram/LLM/scheduler retry.

**P9. Deterministic Business Rules**

Tính tiền, progress, streak, ngày nghiệp vụ và trạng thái không phụ thuộc suy luận tự do của model.

**P10. Outcome over Activity (Inspired by ISA)**

Hoạt động đã thực hiện không mặc nhiên đồng nghĩa với mục tiêu đã đạt; mục tiêu phải có tiêu chí hoàn thành cụ thể và phương thức đánh giá phù hợp.

**P11. Evidence-labeled Claims**

Kết quả phân biệt `SELF_REPORTED`, `SYSTEM_OBSERVED`, `EXTERNAL_EVIDENCE` và `NOT_VERIFIED`; không gán nhãn 'đã xác minh' khi chỉ có AI suy luận.

**P12. Human-Controlled Learning**

AI có thể đề xuất điều chỉnh, nhưng thay đổi mục tiêu, milestone, schedule hoặc định nghĩa hoàn thành phải được người dùng duyệt và có audit trail.

**P13. Context Minimalism**

Trợ lý chỉ dùng dữ liệu cá nhân cần cho hành động đang làm; các thông tin như niềm tin, đời tư, sức khỏe không phải điều kiện tiên quyết để bắt đầu sử dụng bot.

---

# 4. Core Functional Requirements

## FR-01: User Profile & Personal Preferences

**Priority:** P0

### Description

Hệ thống cho phép người dùng thiết lập hồ sơ cá nhân và tùy chỉnh cách trợ lý hoạt động.

### Requirements

- Liên kết tài khoản với Telegram User ID.
- Thiết lập timezone, mặc định Asia/Ho_Chi_Minh.
- Thiết lập thời gian bắt đầu và kết thúc ngày.
- Cấu hình thời gian nhắc nhở buổi sáng, buổi tối.
- Cấu hình số lượng và tần suất thông báo.
- Cho phép bật/tắt từng nhóm thông báo.
- Lưu ngữ cảnh và các tùy chọn được người dùng xác nhận.

- Xem và sửa preferences qua Telegram; cho phép reset một cài đặt về mặc định sau khi xác nhận.
- Thiết lập `business_day_cutoff` (mặc định đề xuất 00:00), đơn vị tiền mặc định VND và phạm vi tài khoản được dùng để tính tiền có thể chi.
- Không cho phép thao tác xóa user/profile trong MVP bằng lời nói tự do; yêu cầu luồng xuất dữ liệu và xác nhận đặc biệt ở giai đoạn sau.

### Acceptance Criteria

- Người dùng chỉ cần thiết lập cấu hình cơ bản một lần.
- Thay đổi cấu hình không làm mất dữ liệu lịch sử.
- Người dùng khác không thể xem hoặc thay đổi dữ liệu cá nhân.

---

## FR-02: Daily Habit Management

**Priority:** P0

### Description

Theo dõi các thói quen cần duy trì theo ngày hoặc theo lịch tùy chỉnh.

### Habit Types

- Daily: Lặp lại hằng ngày.
- Weekly: Lặp lại vào các thứ được chọn.
- Custom Schedule: Lặp lại theo quy tắc.
- **One-time task là một entity nhiệm vụ độc lập**, không phải một loại habit.
- **Quantitative là thuộc tính đo lường** (ví dụ km, phút, lần), có thể áp dụng cho habit và one-time task, không phải tần suất lặp.

### Example Habits

| Habit | Target | Frequency |
|---|---|---|
| Đánh răng trước khi ngủ | 1 lần | Daily |
| Rửa mặt | 1 lần | Daily |
| Chạy bộ | 3–4 km | Thứ 2, 4, 6 |
| Học tiếng Anh | 20 phút | Daily |
| Học kỹ năng mới | 20 phút | Daily |
| Giặt quần áo | 1 lần | Chủ nhật |

### Requirements

- Tạo, sửa, tạm dừng và xóa habit.
- Thiết lập mục tiêu và đơn vị đo.
- Tự động tạo daily checklist từ habit definitions.
- Đánh dấu hoàn thành từ Telegram.
- Cho phép cập nhật tiến độ một phần.
- Theo dõi trạng thái Pending, Completed, Skipped, Missed.
- Cho phép sửa trạng thái hoặc tiến độ đã ghi nhận.
- Theo dõi số ngày duy trì liên tiếp (streak).
- Không làm thay đổi dữ liệu lịch sử khi sửa habit definition.

- Hỗ trợ **liệt kê/xem chi tiết/sửa/xóa mềm/khôi phục** habit definition; không xóa cascade các daily task đã phát sinh.
- Đối với sửa lịch lặp lại: lựa chọn `chỉ hôm nay`, `từ ngày mai`, hoặc `toàn bộ lịch tương lai`; mặc định áp dụng từ ngày mai nếu người dùng không nêu khác.
- Mỗi occurrence lưu snapshot tên, target, unit, schedule và version của habit tại thời điểm phát sinh; truy vấn lịch sử đọc snapshot thay vì definition hiện hành.
- Không cho phép đồng thời hai habit active trùng ý nghĩa do AI tự tạo khi chưa hỏi lại (phát hiện probable duplicate).

### Daily Task Lifecycle & Business Rules (P0)

**State model:** `PENDING` → `IN_PROGRESS` (nếu target định lượng và tiến độ > 0) → `COMPLETED`; hoặc chuyển sang `SKIPPED` theo yêu cầu; khi đóng ngày, task còn `PENDING/IN_PROGRESS` → `MISSED`. Cho phép sửa trạng thái ngày quá khứ qua hành động có audit. `CANCELLED` dành cho một one-time task bị hủy trước hạn và không tính vào mẫu số completion rate.

- `COMPLETED` khi đạt target theo đơn vị định lượng hoặc người dùng xác nhận hoàn thành với task boolean; `IN_PROGRESS` không tự được tính là hoàn thành.
- **Cutoff:** Đóng ngày theo timezone và `business_day_cutoff` của user; chốt vào ngày nghiệp vụ tương ứng, không dùng ngày UTC làm ngày hiển thị.
- **Rollover:** Mặc định **không** tự chuyển task chưa hoàn thành sang hôm sau; người dùng có thể chủ động tạo bản sao/task mới, giữ bản cũ là `MISSED`.
- **Progress semantics:** "Chạy được 2 km" = cập nhật tổng hiện tại (`set=2 km`); "Chạy thêm 1 km" = cộng dồn (`add=1 km`). Nếu không rõ đây là cập nhật hay cộng thêm, hỏi lại. Ghi nhận từng thay đổi vào `TaskProgressLog`.
- **Streak:** Chỉ ngày **có occurrence theo lịch** và đạt target mới cộng streak; `MISSED` hoặc `SKIPPED` mặc định làm gián đoạn; ngày không có lịch không làm gián đoạn. Miễn trừ nghỉ có lý do (excused skip) là P1.
- Khi user sửa task/habit quá khứ, analytics và streak bị ảnh hưởng được tính lại theo dữ liệu đã audit.

### Task Actions Matrix

| Object | Create | Read/List | Update | Soft Delete/Cancel | Restore/Undo |
|---|---|---|---|---|---|
| HabitDefinition | Có | Có | Có | Có | Có |
| DailyTask occurrence | Sinh từ lịch hoặc tạo thủ công | Có | Có | Có (cancel) | Có |
| TaskProgressLog | Sinh từ cập nhật | Có | Không sửa bản log; thêm event sửa | Không xóa trực tiếp | Bằng compensating event |

**Acceptance Criteria bổ sung**

- Với lịch thứ 2/4/6, ngày thứ 3 không làm giảm streak khi không có occurrence.
- Cập nhật `set=2 km` rồi `add=1 km` cho ra 3 km, không phát sinh 2 task.
- Task còn 2/3 km khi đóng ngày là `MISSED`, vẫn lưu 2 km actual.
- Thay target từ 3 km thành 5 km từ ngày mai không sửa snapshot lịch sử hôm nay.
- Xóa mềm habit không xóa dữ liệu đã ghi của các ngày trước.

### Example

User: "Hôm nay chạy được 2.5 cây."

Bot:

"Đã ghi nhận 2.5/3 km chạy bộ hôm nay. Bạn đã hoàn thành 83% mục tiêu."

### Acceptance Criteria

- Không tạo trùng daily task khi scheduler chạy lại.
- Một ngày có thể chứa nhiều task độc lập.
- Dữ liệu lịch sử không bị thay đổi khi chỉnh mục tiêu trong tương lai.
- Task chưa hoàn thành không bị tự động chuyển thành Completed.

---

## FR-03: Daily Planning & Reminder

**Priority:** P0

### Description

Tự động xây dựng checklist hằng ngày và chủ động gửi thông báo theo cấu hình.

### Morning Workflow

1. Scheduler xác định danh sách task trong ngày.
2. Hệ thống tạo daily checklist.
3. Telegram Bot gửi thông báo.
4. Người dùng xem, chỉnh sửa hoặc bổ sung nhiệm vụ.
5. Người dùng hoàn thành từng task trong ngày.

### Evening Workflow

1. Bot gửi thông báo tổng kết.
2. Hiển thị task đã hoàn thành và chưa hoàn thành.
3. Người dùng cập nhật các công việc còn thiếu.
4. Hệ thống chốt trạng thái theo quy tắc cấu hình.
5. Lưu dữ liệu phục vụ analytics.

### Requirements

- Nhắc việc theo lịch đã thiết lập.
- Chỉnh thời gian nhắc từng nhiệm vụ.
- Hỗ trợ thay đổi lịch của một ngày mà không làm thay đổi toàn bộ lịch lặp lại.
- Hỗ trợ snooze reminder.
- Hạn chế thông báo trùng lặp.
- Cho phép bỏ qua một ngày hoặc tạm dừng habit.

- **One-time task CRUD:** Tạo, xem danh sách, sửa nội dung/thời điểm/target, hoàn thành, hủy (soft delete), khôi phục theo id hoặc chọn từ danh sách.
- Khi một yêu cầu "xóa việc hôm nay" có thể chỉ là hủy occurrence, bot phải hỏi có muốn giữ hay hủy habit lặp lại.
- Mọi reminder có `dedupe_key` theo user + task + time slot + channel; retry không tạo bản thông báo mới cùng khóa.
- Daily checklist hợp nhất occurrence từ habit và one-time task; đánh dấu nguồn tạo để tránh trùng và xử lý chính xác khi đổi lịch.
- Khi đóng ngày, job cập nhật trạng thái theo lifecycle FR-02; có thể chạy lại an toàn.
- Gửi biểu đồ tóm tắt 7 ngày qua Telegram khi người dùng yêu cầu, không phụ thuộc xây dashboard.

### Acceptance Criteria

- Thông báo có thể được gửi lại an toàn nếu có lỗi mạng.
- Không gửi lặp cùng thông báo khi scheduler chạy trùng.
- Hệ thống xử lý đúng timezone người dùng.

- Retry cùng job/message không tạo thêm daily task hoặc gửi lại một reminder đã thành công.
- Task one-time bị hủy không tính là Missed hoặc Completed.
- Người dùng có thể sửa lịch chỉ hôm nay mà không làm thay đổi các ngày tương lai.

---

## FR-04: Personal Finance Management

**Priority:** P0

### Description

Theo dõi tiền hiện có, các khoản thu chi, ngân sách và số tiền có thể sử dụng.

### Main Features

**Account Management**

- Tạo tài khoản ngân hàng hoặc ví tiền mặt.
- Nhập số dư ban đầu.
- Ghi nhận thu nhập, chi tiêu và chuyển khoản giữa tài khoản.
- Hỗ trợ điều chỉnh số dư sau khi đối soát.
- Theo dõi lịch sử biến động số dư.

- Xem danh sách/chi tiết, đổi tên và vô hiệu hóa tài khoản; **không xóa cứng** tài khoản đã có giao dịch. Tài khoản đóng vẫn xem được lịch sử.
- Phân loại tài khoản `SPENDABLE` (được dùng để chi) và `SAVINGS/LOCKED` (không dùng để chi).
- Mỗi giao dịch thuộc đúng một hoặc hai account (transfer); ghi cân đối theo một database transaction.

**Expense Tracking**

- Ghi nhận chi tiêu bằng ngôn ngữ tự nhiên.
- Phân loại giao dịch.
- Sửa hoặc xóa giao dịch có ghi nhận lịch sử thay đổi.
- Xem chi tiêu ngày, tuần, tháng.
- Phát hiện giao dịch có khả năng trùng lặp.

- Hỗ trợ tìm/lọc giao dịch theo ngày, tài khoản, danh mục, số tiền và từ khóa để chọn đúng bản ghi cần sửa/xóa.
- Sửa transaction qua revision/audit; soft delete là `VOIDED`, khôi phục hợp lệ bằng `RESTORED`; không xóa dòng lịch sử kế toán.
- Hỗ trợ `REFUND` tham chiếu giao dịch chi gốc; hoàn tiền làm giảm chi tiêu net của kỳ theo quy tắc báo cáo, vẫn lưu thời điểm tiền thực về tài khoản.
- Khi sửa giao dịch quá khứ, backend tính lại balances, ngân sách và báo cáo liên quan.

**Budget Management**

- Thiết lập ngân sách ngày.
- Thiết lập ngân sách tháng.
- Thiết lập ngân sách theo danh mục.
- Theo dõi ngân sách đã sử dụng và còn lại.
- Cảnh báo khi sắp vượt hoặc đã vượt ngân sách.

- CRUD ngân sách theo `scope` (daily, monthly, category), ngày hiệu lực và version; cho phép tắt/bật chứ không xóa dữ liệu lịch sử.
- Quy tắc xác định budget áp dụng phải có precedence rõ: category limit là rào cản bổ sung, không được cộng trùng vào budget tổng.
- Mặc định đề xuất **không cộng dồn ngân sách ngày còn dư** sang ngày sau; tùy chọn carryover là P1.

**Savings Management**

- Thiết lập mục tiêu tiết kiệm.
- Theo dõi tiến độ tiết kiệm.
- Hỗ trợ khoản tiền được dành riêng cho tiết kiệm.

- CRUD mục tiêu tiết kiệm; sửa mức mục tiêu không được giả tạo khoản tiền thực đã tiết kiệm.
- Khoản earmark logic trong tài khoản spendable và tiền đã chuyển vào tài khoản savings là hai cách lưu trữ khác nhau, phải tránh trừ hai lần.

### Business Rules

Số dư tài khoản, ngân sách còn lại và tiền dành riêng cho tiết kiệm là ba khái niệm độc lập.

Ví dụ:

- Số dư: 5.000.000 VND.
- Ngân sách chi tiêu ngày: 150.000 VND.
- Đã tiêu: 90.000 VND.
- Ngân sách còn lại trong ngày: 60.000 VND.

Các phép tính tài chính phải được xử lý bởi backend, sử dụng kiểu số chính xác như Java BigDecimal.

### Finance Calculation Contract (P0)

**Định nghĩa số liệu** (đơn vị VND, một currency trong MVP):

- `account_balance` = opening balance + income + refunds + incoming transfer − expenses − outgoing transfer ± reconciled adjustments trên account (không tính giao dịch VOIDED).
- `spendable_balance` = tổng số dư các account `SPENDABLE` − tổng **earmark logic** còn hiệu lực đang nằm trong các account đó. Không trừ thêm tiền đã chuyển sang `SAVINGS/LOCKED` vì account này không thuộc tổng spendable.
- `daily_budget_remaining` = daily budget limit − net expenses thuộc ngày nghiệp vụ; `monthly_budget_remaining` tương tự cho tháng; `category_remaining` nếu category có giới hạn.
- `amount_can_spend_now` = `max(0, min(spendable_balance, các budget remaining có hiệu lực))`; nếu không có budget cho một scope thì **bỏ scope đó** khỏi phép `min`, không mặc định bằng 0. Hiển thị thêm rào cản nào đang giới hạn khả năng chi.
- `net expenses` dùng cho ngân sách = tổng expense hợp lệ trừ refund liên quan; hiển thị đồng thời dòng tiền theo thời điểm phát sinh (cash flow) và điều chỉnh báo cáo kỳ gốc (budget attribution) để người dùng không nhầm.

**Business Rules (mặc định đề xuất cho MVP):**

1. **Budget calendar:** Ngân sách ngày tính theo `business_day_cutoff`, ngân sách tháng theo tháng local. Không tự động chuyển dư ngân sách sang ngày kế tiếp (P1 mới hỗ trợ carryover).
2. **Transfer:** Chuyển giữa hai account của cùng user làm thay đổi balance hai tài khoản nhưng không làm tăng `income` hoặc `expense`. Transfer cần source/destination, amount và currency hợp lệ; khớp ghi nợ/ghi có trong một transaction.
3. **Refund:** Hoàn tiền tham chiếu expense ban đầu; làm tăng tài khoản nhận vào ngày nhận tiền, giảm net expense của kỳ gốc khi phân tích ngân sách (có chú thích báo cáo). Không thể refund vượt phần expense chưa được refund nếu không có override có xác nhận.
4. **Void/Delete:** Khi hủy giao dịch, tạo event reversal/void với reference, không xóa cứng. Tính toán lại ledger projection và tất cả các kỳ báo cáo bị ảnh hưởng.
5. **Edit Backdated:** Sửa amount/date/account/category của giao dịch cũ phải áp dụng atomically, cập nhật đúng ngày/kỳ trước và kỳ sau; báo cáo tải lại sau sửa không dùng cache cũ.
6. **Reconciliation:** Điều chỉnh số dư phải tạo transaction riêng loại `ADJUSTMENT` có lý do, không sửa trực tiếp opening balance đã sử dụng.
7. **Savings:** Tiền được earmark logic là phần số dư chưa được phép tiêu; tiền chuyển sang SAVINGS/LOCKED không còn được tính spendable, không ghi nhận là chi tiêu.
8. **Money types:** Mọi khoản tiền lưu `BigDecimal`/`NUMERIC` với currency và scale rõ; không dùng float/double. Bỏ qua hỗ trợ đa tiền tệ/chuyển đổi FX trong MVP.
9. **Insufficient information:** Nếu có nhiều account mà người dùng không chỉ rõ, dùng `default_account` đã cấu hình; nếu không có, hỏi chọn. Không đoán số tiền, đối tượng hoặc ngày giao dịch khi thiếu thông tin quyết định.

**Ví dụ minh họa:** Có 5.000.000đ ở ví spendable, dành riêng 2.000.000đ (earmark logic), ngân sách hôm nay 150.000đ và đã tiêu 90.000đ, ngân sách tháng còn 1.000.000đ. Khi đó `spendable_balance = 3.000.000đ`, `daily_budget_remaining = 60.000đ`, `amount_can_spend_now = 60.000đ`. Nếu không có ngân sách ngày, tính mức có thể chi theo các giới hạn còn áp dụng.

### Finance CRUD & Integrity Acceptance Criteria

- User xem/sửa/vô hiệu hóa account; không mất giao dịch khi đóng account.
- Tạo/cập nhật/xóa mềm/khôi phục giao dịch đều phản ánh vào balance và budget **đúng một lần** ngay cả khi retry.
- Trường hợp expense 45.000đ bị sửa thành 35.000đ, balance và budget tương ứng tăng lại 10.000đ.
- Giao dịch hoàn tiền 20.000đ không được tính thành lương/income; chỉ tăng cash balance và giảm net expenses theo quy tắc trên.
- Transfer 500.000đ từ ví A sang B giữ nguyên tổng balance của user và không tính là chi tiêu.
- Earmark 2.000.000đ rồi chuyển chính khoản đó sang Savings không được trừ khỏi spendable hai lần; trạng thái earmark phải được chuyển/giải phóng phù hợp.
- Mọi số liệu trong Telegram, Excel và Analytics phải dùng chung backend calculation service.

### Example

User: "Ăn trưa 45k."

Bot:

"Đã ghi nhận chi tiêu 45.000đ cho Ăn uống.

Hôm nay đã tiêu: 95.000đ.

Ngân sách còn lại: 55.000đ."

### Acceptance Criteria

- Không ghi trùng giao dịch từ cùng một Telegram message.
- Chuyển tiền giữa các tài khoản không được tính thành chi tiêu.
- Các số liệu được tính từ giao dịch đã lưu, không phụ thuộc phép tính do AI tự tạo.
- Nếu thiếu số tiền hoặc thông tin bắt buộc, hệ thống yêu cầu bổ sung.

---

## FR-05: Long-Term Goals & Planning

**Priority:** P0 (goal/milestone CRUD cơ bản trong MVP); P1 (AI planning nâng cao)

### Description

Hỗ trợ người dùng định nghĩa mục tiêu dài hạn, chia nhỏ thành milestones và liên kết với hoạt động hằng ngày. Mỗi goal cần có một **Goal Card / ISA-lite** mô tả thế nào là hoàn thành, dựa trên quy tắc FR-11; không chỉ lưu tên và deadline.

### Goal Examples

- Tiết kiệm 50 triệu trong 12 tháng.
- Giao tiếp tiếng Anh tự tin hơn trong 3 tháng.
- Hoàn thành một khóa học Java trong 8 tuần.
- Duy trì chạy bộ 3 lần/tuần trong 90 ngày.

### Requirements

- Tạo mục tiêu có thời hạn.
- Thiết lập đơn vị đo và tiêu chí hoàn thành.
- Chia mục tiêu thành các milestones.
- Liên kết milestone với daily tasks hoặc habits.
- Theo dõi tiến độ theo thời gian.
- Cập nhật hoặc thay đổi kế hoạch.
- Hỗ trợ AI đề xuất kế hoạch dựa trên mục tiêu, thời gian rảnh và lịch sử thực hiện.

- CRUD goal, milestone và plan: liệt kê/xem, tạo, cập nhật deadline/target/progress, tạm dừng/đóng/hủy, khôi phục nếu hợp lệ.
- Xóa một goal không tự hủy các habit/task độc lập được liên kết; phải hiển thị số lượng dependency và xin xác nhận nếu muốn gỡ liên kết/hủy hàng loạt.
- Cập nhật milestone hoàn thành có timestamp và audit; tiến độ mục tiêu tổng không được AI bịa mà dùng quy tắc tính nhất quán từ backend.
- Với goal mới: ghi `current_state`, `ideal_state`, ít nhất một `success_criterion`, `verification_method`; cho phép lưu DRAFT nếu thiếu thông tin, không coi goal DRAFT là đã kích hoạt.
- Với goal có nhiều criteria: xác định quy tắc hoàn tất (`ALL_REQUIRED` mặc định; hỗ trợ `ANY`/weighted ở P1), criteria có ngưỡng định lượng hoặc rubric định tính do người dùng phê duyệt.
- Mỗi goal ACTIVE được gợi ý một `next_action` nhỏ, có thể gắn với `Task`/`Habit`; không tự tạo hàng loạt hành động khi chưa duyệt plan.
- Goal và milestone có link ngược tới hành động; một task được liên kết nhiều goal chỉ được hoàn thành một lần, không cộng lặp thống kê task.
- Thêm `purpose`/`parent_goal` **tùy chọn** để truy vết lý do thực hiện, không bắt buộc người dùng khai báo mission/values cá nhân.

### Goal Card — ISA-lite Contract (P0)

| Field | Required | Ý nghĩa / Example |
|---|---|---|
| `goal_id`, `owner_id`, `version`, `status` | Yes | Khóa owner, optimistic locking, DRAFT/ACTIVE/PAUSED/COMPLETED/ARCHIVED |
| `purpose` | No | "Học Java để tự xây công cụ cá nhân"; TELOS-lite, không bắt buộc |
| `current_state` | Yes for ACTIVE | "Biết Java core nhưng chưa làm REST API" |
| `ideal_state` | Yes for ACTIVE | "Triển khai được REST API CRUD có PostgreSQL" |
| `success_criteria[]` | Yes for ACTIVE | "Có API CRUD chạy được, test pass, tự giải thích thiết kế" |
| `verification_method` per criterion | Yes for ACTIVE | SELF_REPORT / SYSTEM_EVENT / EXTERNAL_ARTIFACT / USER_REVIEW |
| `milestones[]` | Optional | Các mốc trung gian theo kỳ |
| `next_action_ref` | Optional at creation | Liên kết với task hoặc gợi ý đã duyệt |
| `review_schedule`, `last_review_at` | Optional | Check-in/weekly review; không ép thông báo |

**Quy tắc:** `COMPLETED` chỉ được gán khi tất cả tiêu chí bắt buộc được đóng theo chính sách bằng chứng đã chọn hoặc người dùng xác nhận override có lý do. `Task completed` không tự đồng nghĩa với `Goal completed`.

### AI Planning Workflow

1. Người dùng mô tả mục tiêu.
2. AI thu thập thông tin còn thiếu.
3. AI tạo bản kế hoạch nháp.
4. Người dùng xem và điều chỉnh.
5. Người dùng xác nhận.
6. Backend lưu plan, milestones và lịch thực hiện.
7. Hệ thống theo dõi tiến độ và đề xuất điều chỉnh.

### Acceptance Criteria

- AI không tự động tạo hàng loạt task từ kế hoạch chưa được xác nhận.
- Mọi milestone có thể cập nhật trạng thái.
- Khi thay đổi kế hoạch, dữ liệu lịch sử phải được bảo toàn.

- Hủy hoặc thay goal chỉ ảnh hưởng dữ liệu tương lai sau khi xác nhận; báo cáo quá khứ vẫn truy vết được.
- Trong MVP, tạo/sửa/đóng goal và milestone cơ bản là P0; AI lập kế hoạch phức tạp vẫn là P1.
- Goal ACTIVE không thể thiếu `current_state`, `ideal_state`, success criterion và verification method; thiếu dữ liệu được giữ DRAFT và AI chỉ hỏi đúng trường cần thiết.
- Sửa success criterion tạo version mới và giữ lại criteria/evidence đã ghi ở các phiên bản trước.
- Nếu user hoàn thành task "Học Java 30 phút", hệ thống chỉ kết luận task đã xong; không tự kết luận goal "Tự làm REST API" đã hoàn thành.
- Có thể truy xuất cho mỗi goal: mục đích (nếu có), tiêu chí, bằng chứng từng tiêu chí, bước tiếp theo, các lần review và các lần đổi kế hoạch.

---

## FR-06: AI Natural Language Assistant

**Priority:** P0

### Description

Tích hợp small language model để xử lý ngôn ngữ tự nhiên và điều phối nghiệp vụ.

### Supported Intents

| Intent | Example |
|---|---|
| CREATE_TASK / UPDATE_TASK / DELETE_TASK | "Mai nhắc tao giặt đồ" / "Đổi sang 8h" / "Hủy việc đó" |
| CREATE_HABIT / UPDATE_HABIT / DELETE_HABIT | "Ngày nào cũng học 20p" / "Đổi 30p" / "Dừng thói quen này" |
| LIST_OR_SEARCH_ITEMS | "Liệt kê các khoản cafe hôm nay" |
| COMPLETE_TASK / UPDATE_PROGRESS / SKIP_TASK | "Đánh răng rồi" / "Chạy được 2 cây" / "Hôm nay nghỉ chạy" |
| CREATE_EXPENSE / CREATE_INCOME | "Cafe 30k" / "Hôm nay nhận lương 15 triệu" |
| UPDATE_TRANSACTION / DELETE_TRANSACTION / REFUND | "Sửa cafe còn 25k" / "Xóa khoản cafe sáng" / "Được hoàn 20k" |
| CREATE_BUDGET / UPDATE_BUDGET / DELETE_BUDGET | "Đặt 150k/ngày" / "Tăng lên 200k" / "Tắt ngân sách đồ uống" |
| CREATE_ACCOUNT / UPDATE_ACCOUNT / CLOSE_ACCOUNT | "Thêm ví tiền mặt" / "Đổi tên ví" / "Đóng ví" |
| QUERY_BUDGET / QUERY_BALANCE | "Hôm nay còn được tiêu bao nhiêu?" / "Tài khoản còn bao nhiêu?" |
| QUERY_PROGRESS / DAILY_ANALYTICS / GENERATE_REPORT | "Tuần này học được mấy tiếng?" / "Vẽ biểu đồ 7 ngày" / "Tổng kết tuần" |
| CREATE_GOAL / UPDATE_GOAL / DELETE_GOAL | "Muốn chạy 10km" / "Lùi hạn 1 tháng" / "Hủy mục tiêu đó" |
| CREATE_MILESTONE / UPDATE_MILESTONE / DELETE_MILESTONE | "Thêm mốc chạy 5km" / "Đánh dấu mốc xong" / "Xóa mốc này" |
| GENERATE_PLAN / UPDATE_PLAN | "Lên kế hoạch học tiếng Anh" / "Đổi lịch chạy bộ sang thứ 3" |
| UNDO_LAST_ACTION / RESTORE_ITEM | "Hoàn tác thay đổi vừa rồi" / "Khôi phục khoản đã hủy" |
| DEFINE_GOAL_DONE / UPDATE_SUCCESS_CRITERIA | "Thế nào mới tính là học xong Java?" / "Thêm điều kiện test pass" |
| SUGGEST_NEXT_ACTION / QUERY_GOAL_ALIGNMENT | "Bước nhỏ tiếp theo là gì?" / "Hôm nay làm gì giúp đạt mục tiêu?" |
| SUBMIT_GOAL_EVIDENCE / VERIFY_CRITERION | "Tôi làm xong API rồi, đây là kết quả test" / "Đã đạt tiêu chí này chưa?" |
| REVIEW_GOAL / PROPOSE_REPLAN / APPROVE_REPLAN | "Tổng kết việc học tuần này" / "Giảm lịch học còn 15 phút" |

### AI Responsibilities

- Intent classification.
- Entity extraction.
- Nhận diện thời gian tương đối.
- Hiểu cách viết tắt tiếng Việt.
- Xử lý nhiều yêu cầu trong cùng một tin nhắn.
- Nhận biết dữ liệu thiếu hoặc mơ hồ.
- Trả lời câu hỏi dựa trên thông tin lấy từ backend.
- Đề xuất kế hoạch và điều chỉnh mục tiêu.
- Chọn tool phù hợp với yêu cầu.
- Phân biệt *fact* (task log, transaction, timestamp, verification) với *hypothesis* ("có lẽ học buổi tối không hiệu quả") và *suggestion* ("thử chuyển sang buổi sáng").
- Chỉ đưa các field Goal Card và learning đã liên quan vào prompt; không gửi toàn bộ cuộc sống hoặc dữ liệu tài chính riêng tư chỉ để đề xuất bước học tiếp theo.

- Phân biệt câu mô tả hành động đã xảy ra với câu ra lệnh: "Tối học tiếng Anh 20p" có thể là lịch dự định hoặc kết quả; hỏi lại nếu context chưa đủ.
- Khi tham chiếu mơ hồ ("xóa khoản đó"), dùng retrieval danh sách candidates, trả về các lựa chọn; không sửa/xóa ngẫu nhiên.
- Phân tích multi-intent thành các action độc lập, phân biệt actions thực thi được và actions phải xác nhận; không ép cả tin nhắn thành một write duy nhất.

### AI Execution Pipeline

User Message → Context Retrieval → LLM → Structured Tool Call → Validation → Domain Service → Database → Response.

### Constraints

- Không để LLM truy cập trực tiếp database.
- Không thực thi action không có trong tool registry.
- Không tự đoán dữ liệu tài chính bắt buộc.
- Không tuyên bố thao tác thành công nếu backend chưa xác nhận.
- Hỏi lại khi thời gian hoặc đối tượng không rõ.
- Các hành động nhạy cảm hoặc thay đổi hàng loạt phải được xác nhận.
- Không gửi toàn bộ dữ liệu tài chính và lịch sử hội thoại nếu không cần thiết.
- Có fallback bằng Telegram command/button khi AI lỗi.

- LLM chỉ đưa ra đề xuất action; authorization, confirmation, idempotency, domain validation được quyết định phía backend.
- Không cho LLM tự tạo hoặc tự xác nhận một confirmation token.
- AI không được tự sinh `evidence_status=VERIFIED` dựa trên văn bản do chính AI viết; service xác minh áp policy và provenance.
- Đề xuất thay đổi success criteria/replan phải được xác nhận; không được làm giảm tiêu chí sau khi thấy bằng chứng thất bại mà không ghi revision/approval.
- Nếu một action trong multi-intent lỗi, phản hồi chi tiết action nào thành công, action nào thất bại/chờ duyệt và tuyệt đối không tuyên bố toàn bộ thành công.
- Với tool có side effect, metadata tối thiểu gồm `request_id`, `source_message_id`, `action_index`, `actor_id`, `target_id` (khi cập nhật), và `expected_version` để chặn sửa ghi đè.

### Confirmation & Safety Policy (P0)

| Loại hành động | Chính sách mặc định đề xuất | Ví dụ |
|---|---|---|
| Read/search/query/report | Thực hiện ngay nếu đúng quyền | "Còn được tiêu bao nhiêu?" |
| Write rõ ràng, rủi ro thấp | Thực hiện ngay; trả kết quả và nút Undo | "Ăn sáng 35k", "Đánh răng rồi" |
| Sửa/hủy một item xác định, có thể khôi phục | Cho xem bản ghi mục tiêu; thực hiện sau xác nhận nếu ảnh hưởng tài chính, còn task có thể hủy ngay với Undo | "Xóa khoản cafe lúc sáng" |
| Thiếu dữ liệu / tham chiếu mơ hồ | Hỏi đúng trường thiếu, không ghi DB trước | "Xóa khoản cafe" khi có 3 giao dịch |
| Xóa nhiều item, thay đổi lịch hàng loạt, chuyển tiền bất thường | Bắt buộc hiển thị diff/tóm tắt và nút xác nhận | "Xóa tất cả task tháng này" |
| Thao tác không thể hoàn tác / export toàn bộ dữ liệu / restore | Confirmation mạnh, thực hiện bằng luồng admin riêng, không chỉ bằng LLM | "Xóa sạch dữ liệu", "Khôi phục DB" |

**Execution contract:** `PROPOSED → NEEDS_CLARIFICATION / NEEDS_CONFIRMATION / APPROVED → EXECUTED / FAILED / CANCELLED`. Confirmation ràng buộc với owner, action payload hash và thời hạn; nhấn lại cùng token không thực thi lần 2. Với action thành công, nút Undo phát compensating event có audit. Action phá hủy dữ liệu không hồi phục nằm ngoài MVP.

**Định danh thao tác:** Resolve theo stable `id`, owner và `version`; câu nói tự nhiên chỉ giúp tìm candidate, không là khóa chính. Dùng optimistic locking, trả thông tin conflict nếu dữ liệu thay đổi sau khi hiển thị xác nhận.

**Acceptance Criteria bổ sung:**

- "Sáng cafe 30k" ghi một expense và nhận Undo mà không cần xác nhận mỗi lần (khi đã cấu hình account mặc định).
- "Xóa khoản cafe" có 2 giao dịch phù hợp phải hỏi chọn, không xóa ngẫu nhiên.
- Xóa 10 task cần xác nhận số lượng/phạm vi; hủy confirmation thì 0 task thay đổi.
- Gửi lại cùng Telegram update hoặc nhấn xác nhận hai lần chỉ tạo một write.
- Hai intent trong cùng message: một hợp lệ và một mơ hồ → action hợp lệ xử lý, action mơ hồ giữ Pending; reply tách rõ.

### Example Multi-Intent

User:

"Hôm nay ăn sáng 30k, tối học tiếng Anh 20p và mai nhớ nhắc tao chạy 3 cây."

Expected behavior:

1. Parse và ghi một expense trị giá 30.000đ.
2. Xác định "tối học tiếng Anh 20p" là nhiệm vụ cần tạo hoặc cập nhật; hỏi giờ nếu cần reminder chính xác.
3. Xác định task chạy 3 km cho ngày mai.
4. Chỉ tạo những hành động đã có đủ dữ liệu hợp lệ.
5. Trả lời rõ hành động nào đã lưu và hành động nào cần người dùng xác nhận.

---

## FR-07: Daily Analytics & Weekly Review

**Priority:** P0 (daily summary + chart 7/30 ngày trong MVP); P1 (phân tích nâng cao)

### Description

Cung cấp thống kê và đánh giá tiến độ từ dữ liệu thực tế.

### Metrics

**Habit Metrics**

- Daily completion rate.
- Weekly completion rate.
- Current streak.
- Longest streak.
- Tổng thời gian học.
- Tổng khoảng cách chạy bộ.
- Tỷ lệ bỏ qua hoặc không hoàn thành.

**Finance Metrics**

- Daily spending.
- Weekly spending.
- Monthly spending.
- Budget utilization.
- Spending by category.
- Savings progress.

**Goal Metrics**

- Số success criteria đã `VERIFIED` / tổng criteria bắt buộc, phân biệt self-reported và system-observed trong hiển thị.
- Mức độ có `next_action` cho active goals; số ngày không có hành động liên kết (không đồng nhất với lười biếng).
- Số lần điều chỉnh plan và tỷ lệ thay đổi đã được người dùng duyệt.
- Goal completion percentage.
- Milestone completion.
- Remaining time.
- Progress trend.


### Daily Analytics & Telegram Charts (P0)

- Mỗi ngày có `DailySummary` gồm số task eligible, completed, in_progress, missed, skipped, completion rate; phân biệt việc không được lên lịch khỏi việc bỏ dở.
- Biểu đồ 7/30 ngày: completion rate theo ngày; số phút học/quãng đường vận động theo ngày khi có dữ liệu; số tiền tiêu thực tế so với giới hạn ngày theo ngày.
- Hiển thị top 3 thói quen tốt và các thói quen thiếu ổn định dựa trên dữ liệu, không gán nguyên nhân khi chưa hỏi user.
- User có thể yêu cầu "Biểu đồ 7 ngày" / "Biểu đồ chi tiêu tháng này"; backend chuẩn hóa dữ liệu ngày local, render chart thành ảnh (PNG) và gửi qua Telegram.
- **Nguồn dữ liệu thống nhất:** cùng aggregation/calculation service với Excel và Web; cache nếu có phải invalidated khi sửa giao dịch/task quá khứ.
- Không có mục tiêu/ngân sách thì hiển thị `N/A` hoặc chú thích, không tạo tỷ lệ phần trăm giả.
- `completion_rate = completed / eligible`, `eligible = completed + in_progress + pending + missed` (không tính `SKIPPED` và `CANCELLED` vào mẫu số báo cáo mức hoàn thành; báo cáo riêng số skipped). Quy tắc streak của FR-02 vẫn độc lập: `SKIPPED` không cộng streak.

### Analytics Acceptance Criteria

- Chart đủ 7 điểm cho 7 ngày local, kể cả ngày không có dữ liệu; phân biệt 0 với N/A.
- Sửa giao dịch hôm trước phải làm thay đổi biểu đồ ngày đó và số liệu tổng hợp tháng ngay lần xem tiếp theo.
- Các con số trên ảnh Telegram, bảng chi tiết và Excel khớp nhau theo cùng filter/timezone.
- Không coi ngày không có eligible task là 100% completion (N/A).

### Weekly Report

Báo cáo bao gồm:

1. Tổng số task đã hoàn thành.
2. Tỷ lệ hoàn thành theo từng ngày.
3. Thói quen duy trì tốt nhất.
4. Thói quen thường bị bỏ qua.
5. Tổng chi tiêu và so sánh với ngân sách.
6. Tiến độ mục tiêu dài hạn.
7. Đề xuất cải thiện cho tuần tiếp theo.
8. Với goal đã kích hoạt ACVL: mục tiêu/tiêu chí nào tiến triển, bằng chứng thuộc loại nào, bước tiếp theo và một câu hỏi reflection tùy chọn.
9. Phân biệt "đã quan sát" và "giả thuyết / gợi ý", không tự động thay đổi lịch hoặc mức mục tiêu.

Các nhận xét do AI tạo phải dựa trên thống kê thực tế, không suy diễn nguyên nhân như một sự thật khi chưa có dữ liệu xác nhận.

---

## FR-08: Excel Export

**Priority:** P0 (xuất Excel thủ công trong MVP); P1 (export nâng cao và tự động)

### Description

Cho phép người dùng xuất dữ liệu cá nhân ra file Excel định dạng `.xlsx`.

### Workbook Structure

- Sheet 1: Daily Habits
- Sheet 2: Habit History
- Sheet 3: Transactions
- Sheet 4: Budget Summary
- Sheet 5: Goals & Milestones
- Sheet 6: Daily Analytics
- Sheet 7: Weekly Analytics
- Sheet 8: Goal Verification (criterion, status, evidence_type, evidence_time, source_ref; **P0** nếu đã dùng Goal Card)
- Sheet 9: Learning Reviews (tóm tắt đã duyệt, thay đổi kế hoạch; **P1**)
- Sheet 10: Audit Summary (chỉ metadata an toàn; tùy chọn)

### Requirements

- Xuất theo khoảng thời gian.
- Xuất báo cáo tuần hoặc tháng.
- Tự động đặt tên file theo thời gian.
- Cho phép tải file trực tiếp từ Telegram.
- Đảm bảo dữ liệu Excel khớp với dữ liệu trong database.
- Không đưa thông tin xác thực hoặc secret vào báo cáo.

- Sheet Daily Analytics có các cột `local_date`, `eligible_tasks`, `completed_tasks`, `completion_rate`, `expense_net`, `budget_limit`, `budget_remaining`.
- Ghi rõ quy tắc attribution hoàn tiền, time range, timezone, currency và dữ liệu bị xóa mềm/void để người dùng kiểm tra đối soát.
- File export được sinh từ backend read model, không dựa trên kết quả AI tính toán.
- Goal Verification phải thể hiện evidence type và status thực tế; không xuất raw photo/file attachment hoặc ghi chú nhạy cảm trừ khi người dùng chủ động chọn.

### Acceptance Criteria

- So sánh từng tổng tiền trong Excel với API cùng filter cho ra giá trị bằng nhau đến đơn vị tiền nhỏ nhất.
- Export sau khi sửa giao dịch quá khứ phản ánh ngay kết quả sửa.
- Báo cáo không chứa bot token, key hoặc raw conversation không cần thiết.

---

## FR-09: Web Dashboard & Telegram Mini App

**Priority:** P2

### Description

Cung cấp giao diện trực quan để quản lý và phân tích dữ liệu.

### Pages

- Overview Dashboard.
- Daily Checklist.
- Habit Management.
- Expense & Budget Management.
- Goals & Planning.
- Analytics & Reports.
- AI Assistant.
- Settings.

### Requirements

- Web và Telegram sử dụng chung backend.
- API tách biệt khỏi Telegram message handling.
- Hỗ trợ xác thực và phân quyền.
- Hỗ trợ xem, tạo, sửa, xóa mềm và khôi phục đối với các entity được phân quyền theo cùng chính sách backend với Telegram.
- Biểu đồ trực quan.
- Responsive trên desktop và mobile.

---

## FR-10: Data Administration, Audit, Backup & Recovery

**Priority:** P0 (audit, backup/restore được kiểm chứng và idempotency); P1 (giao diện quản trị backup nâng cao)

### Description

Bảo vệ dữ liệu cá nhân/tài chính khỏi tác động của lỗi thao tác, Telegram retry, scheduler retry, service restart và sự cố database.

### Requirements

- **Soft delete + Restore:** Các entity nghiệp vụ dùng `deleted_at/deleted_by` hoặc trạng thái đóng/void phù hợp domain; truy vấn mặc định lọc deleted nhưng lịch sử vẫn tra cứu được qua chế độ audit.
- **Undo:** Với action hoàn tác được, lưu reference action gốc, xác thực owner và tạo event bù (compensating action); không đảo ngược thao tác của người khác hoặc làm sai trạng thái hiện tại.
- **Audit log:** Bắt buộc cho create/update/delete/restore/confirm/undo, gồm `actor`, `entity_type/id`, `action`, `before/after` đã mask trường nhạy cảm, timestamp, correlation ID và source; audit append-only trong ứng dụng.
- **Idempotency:** Dedupe inbound Telegram `update_id`; unique write key `(source, message_id, action_index)` hoặc idempotency key duy nhất; jobs dùng `(user_id, business_date, job_type, task_id)` khi thích hợp.
- **Data Integrity:** Giao dịch tiền, task progress, history và audit commit trong transaction boundary nhất quán; dùng constraints, foreign keys, optimistic locking.
- **Backup:** Lên lịch PostgreSQL backup tự động (đề xuất mỗi ngày), lưu ngoài volume database, giới hạn quyền đọc, mã hóa khi lưu trữ nếu khả thi, retention đề xuất ít nhất 7 daily snapshots và kiểm tra trạng thái backup.
- **Restore:** Có runbook restore về môi trường test trước; quy định downtime, schema compatibility, kiểm tra row counts và checksum/finance invariants; không tự động restore khi LLM yêu cầu.
- **Recovery after restart:** Scheduler tính công việc còn thiếu nhưng không chạy nhân đôi; NotificationLog lưu trạng thái retry, tạo task và expensing phải chống duplicate.
- **Export/Delete account:** Chính sách xóa toàn bộ dữ liệu cá nhân và backup retention cần quyết định riêng; không làm chức năng irreversible delete trong MVP.

### Acceptance Criteria

- Gửi lại Telegram update 3 lần chỉ có 1 expense; nhấn cùng confirmation 2 lần chỉ thực thi 1 action.
- Thử restart giữa hai lần scheduler chạy không tạo duplicate daily tasks/reminders.
- Soft delete và Undo làm số dư/ngân sách/streak đúng sau tính lại, audit vẫn còn.
- Ít nhất một bài kiểm tra restore tự động hoặc có runbook được chạy thành công trước khi release MVP; so sánh số lượng bản ghi và invariant tổng số dư trước/sau.
- Backup lỗi được ghi log và gửi cảnh báo; lần backup thành công cuối cùng xem được ở health/status.
- Không thể đọc/sửa dữ liệu của user khác qua id đoán được (đã thiết kế future multi-user, kể cả single-user MVP).

---

## FR-11: Goal Execution & Learning — ACVL (ISA-lite)

**Priority:** P0 = Articulate + Climb + self-reported Verify + lưu review cơ bản; P1 = learning analytics nâng cao + evidence ngoại vi tự động + đề xuất replan thông minh.

### Description

Áp dụng vòng lặp **Articulate → Climb → Verify → Learn** để biến mục tiêu mơ hồ thành kết quả có thể kiểm tra và ghi nhận bài học xuyên suốt. Đây là adaptation lấy cảm hứng từ *The Algorithm + ISA + TELOS + Cortex* trong dự án LifeOS của Daniel Miessler, được rút gọn cho Telegram-first app.

**Mục tiêu của ACVL:** Không chỉ hỏi "hôm nay đã làm gì", mà còn trả lời "đang hướng tới trạng thái nào, bước tiếp là gì, bằng chứng tiến bộ là gì, và cần điều chỉnh ra sao".

### Step 1 — ARTICULATE: Define Done (P0)

- Từ mô tả của người dùng, tạo **Goal Card DRAFT** với `current_state`, `ideal_state`, `success_criteria[]` và `verification_method`.
- Tiêu chí có thể là định lượng (`run 5 km`, `20 bài test pass`) hoặc định tính có rubric phê duyệt (`giao tiếp 10 phút bằng tiếng Anh với người thật`).
- Mỗi tiêu chí phải nêu **điều gì có thể chứng minh tiêu chí chưa đạt** và bằng chứng chấp nhận được; điều kiện bảo vệ khỏi mục tiêu mơ hồ.
- Khi mô tả "muốn giỏi tiếng Anh", AI gợi ý 2–3 kết quả kiểm chứng được, nhưng chỉ kích hoạt goal sau khi user chọn/chỉnh.
- Không bắt buộc tạo Goal Card cho đánh răng, rửa mặt hay các daily habit đơn giản nếu người dùng không gắn vào mục tiêu dài hạn.

### Step 2 — CLIMB: Smallest Useful Next Action (P0)

- Goal ACTIVE có tối đa **một `next_action` nổi bật** trên Telegram; có thể có kế hoạch/milestones khác ở phía sau.
- Next action chứa `action_text`, `estimate_minutes` (tùy chọn), `due_date` (tùy chọn), `goal_id`, `milestone_id` (tùy chọn), `action_status`.
- Đề xuất bước đủ nhỏ để người dùng thực hiện; ưu tiên task đã có thay vì tạo bản ghi trùng.
- Khi bước hoàn thành, backend cập nhật trạng thái task; AI chỉ đề xuất bước tiếp, không tự nhận goal đã xong.
- Nếu thiếu thời gian rảnh hoặc có xung đột lịch, ưu tiên hỏi/gợi ý, không tự đặt lịch bắt buộc.

### Step 3 — VERIFY: Evidence, Status, Provenance (P0/P1)

| Evidence type | Example | MVP policy |
|---|---|---|
| `SELF_REPORTED` | "Đã đánh răng", "học 20 phút" | P0; hiển thị `Đã ghi nhận (tự báo cáo)` |
| `SYSTEM_OBSERVED` | App ghi được task timer, transaction, dữ liệu test từ integration tin cậy | P0 nếu nguồn đã có trong backend; không suy diễn kết quả ngoài phạm vi nguồn |
| `EXTERNAL_ARTIFACT` | Link repo, ảnh, chứng chỉ, file kết quả test | P1; P0 cho phép lưu reference text nhưng **không tự xác thực nội dung** |
| `USER_REVIEW` | Người dùng duyệt tiêu chí định tính | P0; ghi actor/time/policy version |

- `VerificationRecord` được gắn với **một success criterion phiên bản cụ thể**, chứa `submitted_by`, `source_type`, `source_reference`, `observed_at`, `decision_at`, `decision`, `criterion_version`.
- Tách **claim** (user nói hoàn thành) khỏi **verification** (hệ thống/nguồn/duyệt xác nhận); không dùng AI opinion làm bằng chứng độc lập.
- Trạng thái criterion: `UNVERIFIED` → `SUBMITTED` → `VERIFIED`/`REJECTED`, hoặc `WAIVED` sau thao tác override có lý do và xác nhận.
- Với thói quen rủi ro thấp, self-report đủ để đánh dấu task COMPLETED; với goal yêu cầu bằng chứng kỹ thuật, self-report **không tự nâng thành VERIFIED** nếu policy đòi test output.
- Mỗi lần sửa/xóa mềm evidence phải để lại audit, ảnh hưởng Goal progress được tính lại, không overwrite record cũ.
- Không gửi file/ảnh/link chứa dữ liệu riêng tư tới AI provider ngoài khi người dùng chủ động cho phép xử lý nội dung.

### Step 4 — LEARN: Review & Controlled Replanning (P0/P1)

- Review ngắn (P0) khi user yêu cầu hoặc trong weekly report: dữ kiện quan sát được, điểm vướng do user khai báo, một đề xuất bước kế tiếp.
- Structured `LearningReview` gồm `period_start/end`, `facts`, `user_reflection`, `hypotheses`, `recommendations`, `reviewed_by_user`, `status`.
- Nếu "3 ngày liên tiếp không học", bot **không** kết luận do lười/thiếu kỷ luật; đưa ra dữ liệu và hỏi có cần giảm mục tiêu hoặc đổi khung giờ.
- AI có thể tạo `PlanChangeProposal` (P1) ghi rõ old/new, reason, projected impact và approval status; backend áp dụng chỉ khi user xác nhận.
- Ghi phiên bản kế hoạch và tiêu chí trước/sau; báo cáo quá khứ không bị tái diễn giải theo tiêu chí mới.
- Chỉ nâng reflection thành **confirmed preference** khi user xác nhận; giả thuyết AI và sở thích tự suy diễn không tạo permanent memory.

### ACVL Flow & State Transitions

User defines goal → Goal Card DRAFT → user approves → ACTIVE → choose next action → task/event recorded → evidence submitted → verification decision → review → continue / propose replan / close goal.

- Goal Card DRAFT không sinh recurring task hoặc reminders.
- Step "Climb" chỉ tác động task đã được duyệt; việc học cá nhân không cần chạy tool tự động ngoài Telegram.
- `COMPLETED` goal đòi criteria hoàn tất theo verification policy; nếu chưa đủ, bot dùng "Đã làm xong nhiệm vụ, tiêu chí mục tiêu đang chờ xác minh".
- Người dùng có thể bỏ qua bước Learn/Review; việc dùng bot nhanh không bị khóa vì bắt buộc 4 bước cho mỗi message.

### Example — Learning Java

1. **Articulate:** "Muốn học Spring Boot trong 8 tuần" → user duyệt done = REST CRUD + PostgreSQL + 5 tests pass + giải thích được cơ chế; baseline là Java core.
2. **Climb:** "Hôm nay tạo endpoint GET /tasks 25 phút" → lưu next action nếu user xác nhận.
3. **Verify:** "Đã viết endpoint" → task COMPLETED (self-report); criterion `API tests pass` chưa VERIFIED khi chưa có test evidence.
4. **Learn:** "Tuần này 4/5 buổi, vướng việc tối thứ 4" → ghi nhận fact và reflection; gợi ý đổi 1 buổi, chỉ áp dụng sau duyệt.

### Acceptance Criteria

- Tạo goal mơ hồ phải sinh DRAFT, hỏi rõ tiêu chí và cách verify trước khi ACTIVE; không tự chế success criteria rồi tuyên bố hoàn thành.
- Một goal ACTIVE có criterion versioned, next action có liên kết và trạng thái xác minh truy vấn được qua Telegram.
- Một task hoàn thành không tự động đóng goal trừ khi toàn bộ criteria theo policy đạt trạng thái tương ứng.
- Bằng chứng `SELF_REPORTED` luôn có nhãn; không bị hiển thị sai là hệ thống đã tự kiểm tra.
- Lặp message hoặc tool call không tạo trùng `VerificationRecord`/`LearningReview`; thay đổi trạng thái có auth, audit và idempotency.
- Sửa criteria sau khi thất bại không thay thế bản cũ và phải được xác nhận; trend lịch sử truy vết theo version.
- Weekly review nêu được dữ kiện thật và đề xuất tách biệt; không tự thay mục tiêu hoặc lịch.
- Người dùng dùng bot như habit tracker/expense tracker thông thường mà không bị ép ACVL trên từng message.

---

# 5. AI Architecture Requirements

## 5.1. Model Strategy

Sử dụng model nhỏ làm mặc định để tối ưu độ trễ và chi phí.

Model phải được cấu hình thông qua environment variables hoặc application configuration, tránh hardcode.

Cho phép thay model hoặc chuyển sang model mạnh hơn đối với những nhiệm vụ lập kế hoạch phức tạp.

Không yêu cầu fine-tuning trong MVP.

## 5.2. Tool Registry

Các tool nghiệp vụ dự kiến được nhóm theo domain. **Không bắt buộc tách một REST endpoint cho mỗi tool**; đây là các operation mà AI orchestrator được whitelist để gọi.

**Tasks & Habits (P0)**

- `create_task`, `list_tasks`, `get_task`, `update_task`, `complete_task`, `skip_task`, `cancel_task`, `restore_task`, `update_task_progress`
- `create_habit`, `list_habits`, `get_habit`, `update_habit`, `pause_habit`, `delete_habit`, `restore_habit`
- `get_daily_checklist`, `get_habit_statistics`

**Finance (P0)**

- `create_account`, `list_accounts`, `update_account`, `close_account`, `get_account_balance`
- `record_expense`, `record_income`, `record_transfer`, `list_transactions`, `update_transaction`, `void_transaction`, `restore_transaction`, `record_refund`
- `create_budget`, `list_budgets`, `update_budget`, `deactivate_budget`, `get_budget_status`, `get_amount_can_spend`
- `create_savings_goal`, `update_savings_goal`, `get_savings_progress`

**Goals & Reporting (MVP core P0, advanced planning P1)**

- `create_goal`, `list_goals`, `update_goal`, `close_goal`, `restore_goal`, `create_milestone`, `update_milestone`, `delete_milestone`
- `get_goal_progress`, `get_daily_analytics`, `generate_daily_chart`, `generate_weekly_report`, `export_excel`
- `get_goal_card`, `update_goal_card_draft`, `set_goal_success_criteria`, `activate_goal` (P0, typed inputs + confirmation)
- `suggest_next_action`, `link_goal_action`, `get_next_goal_action` (P0; AI gợi ý, backend lưu khi user duyệt)
- `submit_goal_evidence`, `get_verification_status`, `review_goal_criterion` (P0 với self-report/user review, P1 external auto verification)
- `create_learning_review`, `get_goal_learning_history` (P0 review đơn giản)
- `propose_plan_change`, `approve_plan_change`, `get_goal_alignment` (P1, trừ các thao tác thay đổi goal hiện có vẫn phải dùng confirmation P0)
- `generate_plan_draft` (P1 cho kế hoạch AI nâng cao)

**Action Control (P0)**

- `search_entities`, `get_action_preview`, `confirm_action`, `cancel_action`, `undo_last_action`, `get_action_status`

Mỗi tool phải có:

- Input schema.
- Required fields.
- Validation rules.
- Authentication and authorization checks.
- Output schema.
- Structured error response.
- Idempotency strategy với write operations.

- Với mutations: `target_id`, `expected_version`, `source_message_id`/`idempotency_key`, actor context và `confirmation_token` khi cần.
- Trong tool response: `status` (`EXECUTED`/`NEEDS_CONFIRMATION`/`NEEDS_CLARIFICATION`/`FAILED`), affected entity id/version, `audit_id`, có thể có `undo_action_id`.
- Domain service validate authorization và confirmation **lại** trước khi commit; model không thể giả lập thông qua text.

**Nguyên tắc triển khai:** Dùng một typed tool registry (JSON schema) và domain commands có shared service; không để logic CRUD rải rác vào Telegram adapter hoặc system prompt.

## 5.3. Context Management

Context bao gồm:

- User preferences.
- Current active habits.
- Active goals.
- Relevant recent conversation.
- Relevant financial summary.
- Pending confirmation actions.
- Goal Card đã duyệt và `next_action` cho yêu cầu liên quan; chỉ gửi criteria/version/evidence summary cần thiết.
- `LearningReview` được người dùng xác nhận (nếu có) với timestamp/provenance; không dùng AI hypothesis chưa duyệt như profile fact.

### Memory-lite Strategy (P0/P1)

- **Operational state (P0):** DB là nguồn sự thật cho finance, task, goal, verification, next actions.
- **Conversation state (P0):** phiên hội thoại gần đây và pending confirmation có TTL; không dùng như sổ cái hay nguồn truth.
- **Confirmed learning (P1):** bài học, tùy chọn và quyết định được user duyệt lưu dạng có nguồn và thời điểm, cho phép sửa/xóa.
- **Knowledge retrieval / vector search (P2):** chỉ bổ sung khi thực tế vượt nhu cầu truy vấn relational/keyword; không có vector DB trong MVP.

Chỉ truyền context cần thiết cho từng yêu cầu.

Không sử dụng toàn bộ conversation history như nguồn sự thật về số dư hoặc trạng thái task.

## 5.4. AI Observability

Theo dõi:

- Model name/version.
- Input/output tokens.
- Response latency.
- Estimated cost.
- Tool call success/failure.
- Intent classification accuracy.
- Structured output validity.
- Tỷ lệ AI tạo Goal Card draft hợp lệ nhưng không tự thay tiêu chí; tỷ lệ `verification status` bị suy luận sai và bị domain layer chặn.
- Số evidence source bị thiếu, số lần user sửa tiêu chí/next action, tỷ lệ đề xuất replan được chấp nhận.
- Số lần cần người dùng sửa lại yêu cầu.

Dữ liệu nhạy cảm cần được che hoặc loại khỏi log quan sát khi không cần thiết.

---

# 6. High-Level Data Model

Các entity chính:

| Entity | Description |
|---|---|
| User | Thông tin và cấu hình cá nhân |
| HabitDefinition | Quy tắc thói quen định kỳ, version, lịch hiệu lực, trạng thái active/paused/deleted |
| DailyTask | Occurrence được snapshot từ habit hoặc manual task; owner, business date, target, state |
| OneTimeTask | Nhiệm vụ độc lập; có thể triển khai chung `Task` với `source_type=MANUAL` và occurrence duy nhất |
| TaskProgressLog | Append-only event thay đổi tiến độ/trạng thái; phân biệt SET và ADD |
| Account | Tài khoản tiền, `SPENDABLE/SAVINGS/LOCKED`, currency, trạng thái đóng |
| Transaction | Loại `INCOME/EXPENSE/TRANSFER/REFUND/ADJUSTMENT`, có reference/revision/void |
| TransactionRevision | Lịch sử before/after khi sửa, hủy, restore và liên kết giao dịch gốc |
| Budget | Scope, period, category, limit, effective dates, version |
| SavingsGoal | Target, hạn và khoản earmark logic hoặc account tiết kiệm tương ứng |
| Goal | Mục tiêu dài hạn, current/ideal state, mục đích tùy chọn, trạng thái, version |
| GoalSuccessCriterion | Tiêu chí hoàn thành thuộc Goal Card, verifier policy, threshold/rubric, revision |
| GoalTaskLink | Liên kết goal/milestone với task/habit đã có; tránh tạo trùng task khi nhiều goal |
| VerificationRecord | Claim, evidence_type, source_ref, decision/status, actor, timestamp, criterion_version, audit |
| LearningReview | Period, factual summary, reflection, hypotheses, suggestions, user acknowledgment |
| PlanChangeProposal | Đề nghị replan old/new, lý do, phê duyệt, version; P1 |
| Milestone | Cột mốc và trạng thái tiến độ |
| Plan | Kế hoạch và phiên bản có phê duyệt |
| Reminder | Cấu hình nhắc nhở và snooze |
| NotificationLog | Trạng thái gửi/retry + dedupe key |
| DailySummary | Aggregate theo ngày nghiệp vụ cho Telegram chart/Excel, có cơ chế tính lại |
| ConversationSession | Ngữ cảnh hội thoại và pending references có giới hạn |
| AiActionLog | Trạng thái proposal, confirmation, tool execution, correlation IDs |
| AuditEvent | Append-only audit cho mutation và undo |
| IdempotencyRecord | Khóa chống replay + response/result fingerprint |
| BackupJobLog | Lịch sử backup/test restore, kết quả, thời gian và nơi lưu (không chứa secret) |

### Data Design Principles

- Mỗi bản ghi nghiệp vụ có owner rõ ràng.
- Sử dụng thời gian chuẩn để lưu trữ, chuyển đổi theo timezone khi hiển thị.
- Tách definition và occurrence đối với recurring tasks.
- Lưu lịch sử thay đổi quan trọng.
- Áp dụng database migrations.
- Bảo đảm transaction integrity.
- Các khoản tiền sử dụng decimal, không dùng floating point.
- Có unique constraints và idempotency keys phù hợp.

- `owner_id` đi cùng toàn bộ truy vấn/entity; authorization không dựa duy nhất vào Telegram chat ID.
- Xóa mềm/void, audit append-only; không cascade xóa lịch sử khi xóa definition.
- Unique `(owner_id, habit_id, business_date)` cho scheduled occurrence và `(source, idempotency_key)` cho write; hỗ trợ action index khi multi-intent.
- Mỗi DailyTask lưu snapshot target/schedule và origin để thay đổi definition không thay đổi lịch sử.
- Các projections cho balance, budget và daily analytics được recompute/reconcile sau historical edits; không dựa vào dữ liệu tổng hợp cũ làm source of truth.
- Lưu `occurred_at` UTC và `business_date` local cho các giao dịch/task; backdated edits phải xem xét cả hai.
- Criteria và verification references là append-only/versioned; `verification_record` có unique request ID và không thể tham chiếu nhầm owner hoặc criterion version khác.
- Tính `goal_completion` từ verified criteria tại goal version hiện hành; thống kê task completion và goal completion là hai metric riêng.
- `source_ref` lưu URI/token tham chiếu có kiểm soát, không tự thu thập nội dung file ngoài; có quyền xóa/ẩn dữ liệu cá nhân và retention policy.
- Reflection và preference cần có `confirmed_by_user` + provenance; nội dung giả định của AI không được tự chảy vào factual profile.

---

# 7. Non-Functional Requirements

## 7.1. Performance

- Các thao tác không cần AI nên phản hồi nhanh, mục tiêu p95 dưới 1 giây ở mức tải cá nhân.
- Với AI, mục tiêu p95 dưới 5 giây cho các thao tác đơn giản, tùy thuộc độ trễ model.
- Các báo cáo lớn có thể thực hiện qua job riêng.
- Cần timeout và xử lý lỗi khi AI service không khả dụng.

## 7.2. Security

- Bot token và API keys lưu trong secrets/environment.
- Chỉ Telegram User ID được cấp quyền mới có thể thao tác trong bản single-user.
- Mọi API phải kiểm tra quyền truy cập dữ liệu.
- Bảo vệ thông tin tài chính và dữ liệu cá nhân.
- Không log API keys hoặc dữ liệu nhạy cảm không cần thiết.
- Có cơ chế backup và recovery.
- Verification artifact và learning reflection có thể chứa thông tin riêng tư: chỉ cho phép lưu/đọc theo owner; redaction ở prompt/log/export, hạn chế lưu nội dung file nhạy cảm khi chỉ cần reference.

- Confirmation tokens ngắn hạn, có chữ ký/lookup server-side, bind đúng actor + payload + target_version.
- Khi hiển thị số tiền, log và export chỉ sử dụng fields cần thiết theo quyền; che dữ liệu nhạy cảm trong tracing.
- Có audit theo actor và kiểm soát quyền đối với export/restore dữ liệu.

## 7.3. Reliability

- Scheduler không tạo trùng daily tasks.
- Notification phải có tracking và retry phù hợp.
- Write operations quan trọng phải idempotent.
- Hệ thống khôi phục được sau khi restart.
- AI không được trở thành single point of failure cho các chức năng cơ bản.

- PostgreSQL backup tự động và bài diễn tập restore tối thiểu trước release; backup hỏng phải có tín hiệu cảnh báo.
- Xử lý concurrent modifications bằng optimistic locking, event source/audit cho undo và idempotent recovery.
- Kiểm thử finance invariants: tổng transfer không thay đổi tổng số dư; void/refund/edit không double-apply.

## 7.4. Maintainability

- Modular Monolith architecture.
- Phân tách Telegram adapter, AI orchestration và domain services.
- REST API được version hóa.
- Sử dụng Flyway cho schema migrations.
- Unit tests cho nghiệp vụ.
- Integration tests cho database và APIs.
- Evaluation dataset cho AI intent extraction.
- Contract tests cho Goal Card schema, evidence policy, criterion versioning và goal-completion gate; golden test cases cho "task done != goal done".

- Integration tests cho lifecycle end-of-day, historical edits, confirmation replay, undo và backup/restore runbook.
- Test golden datasets để so sánh API, Telegram chart và Excel aggregation.

---

# 8. Technical Stack

| Layer | Technology |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Telegram Integration | Telegram Bot API / Java Library |
| AI Integration | OpenAI API |
| Database | PostgreSQL |
| Persistence | Spring Data JPA |
| Migration | Flyway |
| Scheduling | Spring Scheduler |
| Excel | Apache POI |
| Web Frontend | React + TypeScript |
| Charting | Apache ECharts / Recharts (Web); server-side PNG renderer cho Telegram |
| Deployment | Docker Compose |
| Testing | JUnit, Mockito, Testcontainers |
| Backup & Restore | PostgreSQL pg_dump / pg_restore + định kỳ kiểm tra restore |

**Architectural Style:** Modular Monolith, API-first, event-ready.

Không yêu cầu Kafka, Kubernetes, Redis, Vector DB hay multi-agent framework trong MVP.

---

# 9. MVP Scope

## In Scope — V0.3 (MVP)

- Telegram Bot cho một người dùng.
- User preferences.

- CRUD cơ bản, xem danh sách và tìm kiếm object; soft delete/restore/undo trên các entity P0.
- Action confirmation policy, audit events và idempotency cho mọi write.
- Daily habit definition.
- Daily checklist.
- Daily reminders.

- One-time task độc lập, lifecycle cuối ngày, history snapshots, streak và quy tắc set/add progress.
- Hoàn thành và cập nhật tiến độ.
- Nhập expense/income bằng ngôn ngữ tự nhiên.
- Quản lý tài khoản, ngân sách và số dư cơ bản.

- Finance calculation contract: budget hạn mức theo ngày/tháng/category, refund, transfer, void, earmark savings, chỉnh giao dịch quá khứ.
- VND là tiền tệ MVP; cho phép nhiều account cùng VND nếu owner cấu hình.
- Query chi tiêu và checklist bằng hội thoại.
- Simple goals tracking.
- Goal Card/ISA-lite P0: current/ideal state, ít nhất một success criterion, verification method, status/version.
- ACVL-lite P0: next action cho active goal, self-report/user-review evidence, lưu learning review đơn giản theo yêu cầu.
- Goal-task linkage và thao tác xem trạng thái verified/unverified ngay trong Telegram.
- AI Function Calling.
- Lịch sử hoạt động.
- Báo cáo tuần cơ bản.

- Daily summary và biểu đồ Telegram 7/30 ngày (habit completion, chi tiêu/budget).
- Backup PostgreSQL tự động và kiểm chứng phục hồi tối thiểu một lần.
- Xuất Excel thủ công.

## Out of Scope — V0.3 (MVP)

- Multi-user public registration.
- Voice messages.
- Tự động đồng bộ giao dịch ngân hàng.
- Web Dashboard hoàn chỉnh.
- AI tự động thay đổi kế hoạch mà không xác nhận.
- Vector memory và RAG phức tạp.
- Multi-agent architecture.
- Full ISA 12-section artifact / deterministic Claude Code hook system của repository tham khảo.
- Tự động đọc/đánh giá GitHub, kết quả test, ảnh/chứng chỉ hoặc hệ thống bên ngoài để xác minh goal (P1).
- Graph TELOS nhiều tầng, data ingestion cá nhân sâu, self-improving AI tự sửa chính nó, semantic/vector memory (P2).
- Tự động replan, tự ghi recurring task hàng loạt từ AI mà không cần phê duyệt.
- Mobile application độc lập.

- Multi-currency và tỷ giá ngoại tệ.
- Carryover ngân sách dư qua ngày/tháng (P1).
- Đồng bộ ngân hàng tự động; expense categorization từ biên lai bằng OCR.
- Xóa cứng toàn bộ dữ liệu bằng chat và trang admin backup chuyên biệt.
- Advanced streak exemptions / phức tạp hóa lịch nhiệm vụ ngoài MVP.

---

# 10. Delivery Roadmap

## Milestone 1 — Foundation

**Deliverables**

- Java Spring Boot project.
- PostgreSQL integration.
- Database migrations.
- Telegram Bot connection.
- User authentication/authorization.
- Basic configuration.

- Database constraints, auth ownership, audit base, idempotency middleware, backup job và restore runbook.
- Typed domain commands + confirmation action state machine.

## Milestone 2 — Habits & Scheduling

**Deliverables**

- Habit CRUD.
- Recurring schedule engine.
- Daily task generation.
- Telegram checklist.
- Reminders.
- Task completion history.

- One-time task CRUD, soft-delete/restore/undo.
- End-of-day lifecycle, snapshot target, SET/ADD progress, streak test cases.

## Milestone 3 — Finance

**Deliverables**

- Account management.
- Transaction management.
- Budget management.
- Daily balance and budget notifications.
- Finance calculation tests.

- Transaction edit/void/restore/refund/transfer và finance revision log.
- Budget scope precedence, amount-can-spend calculation, savings earmark, reconciliation tests.

## Milestone 4 — AI Assistant

**Deliverables**

- LLM integration.
- Structured tool calling.
- Intent extraction.
- Multi-intent processing.
- Confirmation flow.
- AI evaluation tests.

- CRUD/lookup intent coverage, ambiguity handling, preview-confirm-undo interactions.
- Per-intent execution statuses và replay/retry tests.

## Milestone 5 — Planning & Reporting

**Deliverables**

- Basic goal management.
- Weekly analytics.

- Daily summaries và server-rendered chart gửi Telegram (7/30 ngày).
- Cross-check Telegram/Excel calculations với cùng backend service.
- Excel exports.
- AI-generated plan drafts.
- Goal Card CRUD, criterion versioning, goal-task links, ACVL next action và verified status (P0).
- Goal evidence self-report/user review, weekly reflection có tách facts/hypotheses/suggestions (P0).
- P1 sau khi P0 ổn định: external evidence checks, plan change proposals, memory-lite confirmed learning.

## Milestone 6 — Advanced Product

**Deliverables**

- React Web Dashboard.
- Telegram Mini App.
- Advanced AI planning.
- Trend analysis.
- Personalized recommendations.

---

# 11. Success Metrics

## Product Metrics

- Daily Active Usage.
- Weekly Active Usage.
- Habit completion rate.
- Percentage of days with completed check-in.
- Number of recorded transactions.
- Reminder interaction rate.
- Number of completed milestones.
- Tỷ lệ active goals có Goal Card đủ trường và next action đã xác nhận.
- Tỷ lệ required criteria verified và thời gian từ submit tới verified (phân tách evidence type).
- Số learning reviews được người dùng duyệt; không dùng số review như thước đo duy nhất của tiến bộ.

- Daily summary / chart request completion rate.
- Phần trăm mutation có audit đầy đủ và thực thi idempotent.
- Backup success rate / time since last successful restore test.

## AI Quality Metrics

- Intent classification accuracy.
- Entity extraction accuracy.
- Tool execution success rate.
- Percentage of requests requiring clarification.
- User correction rate.
- Incorrect write operations.
- Average cost per interaction.
- Verification claim false-positive rate (P0 mục tiêu không false-verified khi backend chưa có xác nhận).
- Tỷ lệ đề xuất next action được chấp thuận/sửa; tỷ lệ plan changes bị thực hiện khi chưa được duyệt (mục tiêu 0).

- Mức độ chính xác khi resolve entity cho UPDATE/DELETE; false deletion rate.
- Confirmation error / replay prevention rate.
- Tỷ lệ multi-intent có báo cáo chính xác trạng thái từng action.

## MVP Acceptance Targets

Các mục tiêu ban đầu, sẽ điều chỉnh sau khi thử nghiệm:

- Tối thiểu 95% intent accuracy trên bộ test đã gán nhãn cho những ý định được hỗ trợ.
- Tối thiểu 99% tool execution success khi input hợp lệ và dependency hoạt động.
- Không chấp nhận lỗi ghi trùng chi tiêu do retry.
- Không chấp nhận sai lệch số dư do lỗi tính toán.
- Có thể sử dụng bot liên tục trong 14 ngày mà không cần chỉnh dữ liệu trực tiếp trong database.

- 100% write operation P0 có idempotency test và audit event; replay không tạo tác động lặp.
- 100% tiền khả dụng/chi tiêu/ngân sách trên API, Telegram và Excel khớp nhau với dataset chuẩn.
- Test one-time tasks, SKIPPED/MISSED, streak, rollover, lịch local và update quá khứ đều pass.
- Ít nhất 1 restore drill thành công và có cảnh báo khi backup lỗi.
- Bot có thể xuất biểu đồ theo ngày cho 7/30 ngày mà không cần Web Dashboard.
- Một goal được tạo từ mô tả mơ hồ phải hoàn thành vòng Articulate → Climb → Verify (self-report/policy) → Learn (simple review) bằng Telegram, với audit và approval đúng.
- 100% `Goal.COMPLETED` phải truy vết được tới các success criteria phiên bản hiệu lực và verification decisions phù hợp.
- Không có trường hợp `SELF_REPORTED` được hiển thị nhầm là `SYSTEM_OBSERVED`/`EXTERNAL_EVIDENCE`.
- Lịch sử goal/criteria/plan không bị viết đè khi replan hoặc sửa điều kiện hoàn thành.

---

# 12. Example End-to-End User Journey

### 07:00 — Morning Planning

Bot tự động gửi checklist hôm nay.

User bổ sung:

"Hôm nay thêm việc học Java 30 phút."

AI nhận diện CREATE_TASK và backend lưu task.

### 09:30 — Finance Tracking

User:

"Ăn sáng hết 35k."

AI gọi `record_expense`.

Backend ghi giao dịch và tính ngân sách còn lại.

Bot gửi phản hồi.

### 18:00 — Habit Tracking

User:

"Đi bộ được 3.5 cây rồi."

AI gọi tool cập nhật tiến độ.

Backend ghi 3.5 km và kiểm tra mục tiêu.

### 22:30 — Daily Review

Bot gửi danh sách các task đã hoàn thành và chưa hoàn thành.

User có thể cập nhật các task còn thiếu.

### 19:00 — Edit Expense & Undo

User: "Khoản cafe sáng thực ra chỉ 25k, sửa giúp tao."

Bot tra cứu đúng giao dịch, hiển thị preview khi cần. Sau khi update thành công, backend áp dụng transaction revision, tự tính lại balance/budget và gửi số liệu mới kèm Undo.

### 21:00 — Daily Chart

User: "Vẽ biểu đồ 7 ngày gần nhất về thói quen và chi tiêu."

Bot lấy DailySummary đã chuẩn hóa, render thành ảnh gửi Telegram; nếu chưa có dữ liệu, biểu đồ hiển thị N/A thay vì tỷ lệ không có căn cứ.

### 20:00 — ACVL Goal Check-in

User: "Tôi muốn học Java đủ để tự viết API cho bot của mình trong 8 tuần."

Bot tạo Goal Card DRAFT: baseline Java core; ideal state CRUD REST API + PostgreSQL; gợi ý tiêu chí test pass và hỏi người dùng xác nhận định nghĩa hoàn thành. Sau khi duyệt, bot đề xuất task đầu tiên "Tạo endpoint GET /tasks".

User: "Đã code xong endpoint" → task được ghi hoàn thành theo self-report; goal vẫn đang chờ criteria về test/DB. Nếu user gửi output test, hệ thống ghi reference; không tự nói "đã kiểm tra" khi chưa có verifier phù hợp. Cuối tuần, bot tóm tắt số buổi học và hỏi có muốn giảm một buổi quá tải hay không; đổi lịch chỉ sau khi xác nhận.

### Sunday — Weekly Review

Bot gửi:

- Habit completion summary.
- Spending summary.
- Goal progress.
- Những điểm nổi bật trong tuần.
- Đề xuất cải thiện tuần tiếp theo.

Người dùng có thể yêu cầu xuất báo cáo Excel.

---

# 13. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| AI hiểu sai yêu cầu | Schema validation, clarification, confirmation |
| Ghi chi tiêu trùng | Idempotency key, unique constraints |
| Scheduler gửi trùng | Notification log, locking |
| AI API không khả dụng | Command/button fallback |
| Chi phí AI tăng | Context trimming, model routing, usage monitoring |
| Dữ liệu bị mất | Database backup, recovery testing |
| Kế hoạch quá phức tạp | Bản nháp, milestones nhỏ, điều chỉnh theo tiến độ |
| AI đóng goal không có bằng chứng | Goal completion gate từ criteria + VerificationRecord, distinct self-report/system-verified |
| AI suy diễn sở thích hoặc nguyên nhân thất bại | Fact/hypothesis/suggestion split; chỉ lưu confirmed learning có provenance |
| Cơ chế ACVL gây phiền khi cập nhật việc nhỏ | Chỉ áp Goal Card với active goals; fast-path thói quen/chi tiêu không bắt chạy full workflow |
| Thay đổi criteria để hợp thức hóa thất bại | Criteria versioning, approval, audit; không rewrite historical verification |
| Nguồn bên ngoài chứa dữ liệu riêng tư | Reference-only MVP; chỉ fetch/process khi user cho phép và đã phân quyền |
| Người dùng bị làm phiền | Notification preferences, snooze, quiet hours |
| Xóa/sửa nhầm đối tượng | List/search candidates, preview, stable ID, optimistic locking, Undo |
| Sai số dư do hoàn tiền hoặc chuyển giữa ví | Ledger rules, finance invariants, reconciliation, unit/integration tests |
| Ngày nghiệp vụ lệch vì timezone | `business_day_cutoff`, snapshot date, deterministic close job |
| Retry LLM hoặc user bấm xác nhận lại | Action-level idempotency và one-time confirmation token |
| Backup không thể restore | Restore drill, retention, status monitoring, recovery runbook |
| Analytics sau chỉnh sửa dữ liệu cũ không cập nhật | Recompute/invalidate projections, golden dataset comparisons |

---

# 14. Open Decisions

Các giá trị sau là **mặc định đề xuất cho quá trình thiết kế**, chưa thay cho quyết định của người dùng/chủ sản phẩm:

| Decision | Proposed Default | Status |
|---|---|---|
| Số user | Minimal echo hỗ trợ cấu hình WHITELIST (danh sách Telegram ID người gửi) hoặc PUBLIC (mọi sender ID hợp lệ); schema DB đa người dùng, bảng User và onboarding mở rộng vẫn tạm hoãn | Đã giải quyết cho minimal echo; mở rộng cần xác nhận |
| Morning / evening reminder | Cấu hình khi onboarding; ví dụ 07:00 / 22:30, không hardcode | Cần xác nhận |
| Tài khoản và currency | Nhiều account cùng VND; chưa hỗ trợ đa tiền tệ | Cần xác nhận |
| Ghi expense rõ ràng | Tự ghi + Undo; mơ hồ/nhạy cảm phải hỏi/xác nhận | Cần xác nhận |
| Task chưa hoàn thành | Đóng ngày thành MISSED, không auto-rollover | Cần xác nhận |
| Savings earmark | Hỗ trợ earmark logic hoặc tài khoản SAVINGS, không double-subtract | Cần xác nhận |
| Excel | Xuất theo yêu cầu, gửi tuần tự động là P1 | Cần xác nhận |
| Telegram transport | Long polling khi local dev; webhook khi deploy public đủ điều kiện | Cần xác nhận |
| Budget carryover | Không cộng dồn sang ngày sau trong MVP | Cần xác nhận |
| Deadline, skip, streak | Streak theo scheduled occurrence; SKIPPED mặc định ngắt streak | Cần xác nhận |
| Completion denominator | Loại SKIPPED/CANCELLED khỏi eligible, thống kê riêng | Cần xác nhận |
| Refund attribution | Dòng tiền vào ngày refund; ngân sách net ghi điều chỉnh tại kỳ expense gốc | Cần xác nhận |
| Backup retention | Ít nhất 7 bản hằng ngày, một restore drill trước release | Cần xác nhận |
| Account deletion | Đóng/vô hiệu hóa, không hard delete dữ liệu có lịch sử | Cần xác nhận |
| Áp dụng ACVL cho phạm vi nào | Chỉ Goal dài hạn và milestone gắn Goal Card; habit nhỏ không bắt buộc | Đề xuất P0 |
| Verify cho goal trong MVP | Self-report/user confirmation được phép nếu criterion policy chấp nhận; artifact auto-check P1 | Đề xuất P0 |
| Tiêu chí goal định tính | Được phép nếu có rubric kiểm tra và người dùng phê duyệt | Đề xuất P0 |
| Review cadence | Khi user yêu cầu + 1 phần ngắn trong weekly summary, có thể tắt | Đề xuất P0 |
| Memory cá nhân dài hạn | Chỉ preferences/learning đã được xác nhận; không thu thập beliefs/trauma tự động | Đề xuất P0 |
| Chính sách xác minh ghi đè | Cho override có lý do/approval/audit, phân biệt `WAIVED` với `VERIFIED` | Đề xuất P0 |

**Ưu tiên chốt sớm:** task lifecycle và completion denominator; finance attribution/earmark; confirmation policy; cách lưu backup/restore; loại bằng chứng được chấp nhận với từng goal. Các quyết định còn lại có thể giữ cấu hình linh hoạt trong implementation. Các quyết định còn lại có thể giữ cấu hình linh hoạt trong implementation.

---

# 15. Definition of Done — MVP

LifeOS V0.3 MVP được coi là hoàn thành khi một người dùng có thể:

- Thiết lập danh sách thói quen bằng Telegram.
- Nhận checklist tự động mỗi ngày.
- Đánh dấu hoàn thành và cập nhật tiến độ.
- Nhập thu chi bằng ngôn ngữ tự nhiên.
- Biết chính xác ngân sách còn lại.
- Theo dõi ít nhất một mục tiêu dài hạn.
- Tạo và duyệt Goal Card (current/ideal state, Definition of Done, verification method), nhận next action, báo cáo task hoàn thành mà không tự gán goal completed.
- Gửi self-reported evidence và xem trạng thái verified/unverified; weekly review phân biệt dữ kiện, giả thuyết, đề xuất.
- Thay đổi tiêu chí hoặc plan không làm mất history, approval và audit.
- Nhận báo cáo tuần.
- Xuất dữ liệu ra Excel.

- Xem danh sách, sửa, xóa mềm và khôi phục task/habit/transaction/budget/goal theo phạm vi P0; có nút Undo phù hợp.
- Nhận daily chart 7/30 ngày qua Telegram và số liệu khớp Excel.
- Chỉnh giao dịch cũ, hoàn tiền, transfer và xem lại `amount_can_spend_now` không bị sai số.
- Có quy tắc đóng ngày và streak nhất quán, kể cả khi sửa lịch sử.
- Các thao tác mơ hồ hoặc hàng loạt được hỏi lại/xác nhận đúng chính sách.
- Có audit trail và chống ghi trùng trong trường hợp message/tool/job retry.
- Restore được một backup thử nghiệm, và trạng thái backup có thể kiểm tra.
- Sử dụng toàn bộ các tính năng trên mà không cần thao tác trực tiếp với database.

# 16. Reference Inspiration & Design Decisions

## 16.1. Nguồn nghiên cứu (kiểm tra ngày 11/10/2026)

- **LifeOS repository** — https://github.com/danielmiessler/LifeOS — personal AI harness hướng Current State → Ideal State.
- **The Algorithm — LifeOS Thinking System** — https://docs.ourlifeos.ai/Algorithm__AlgorithmSystem — diễn đạt done thành claims kiểm chứng được, thực thi và học sau khi quan sát thực tế.
- **ISA — Ideal State Articulation** — https://docs.ourlifeos.ai/ISA__ISASystem — artifact cho mỗi goal/task, tiêu chí xác minh, sự khác biệt giữa "đang làm" và "đã chứng minh".
- **TELOS skill** — https://github.com/danielmiessler/LifeOS/blob/main/LifeOS/install/skills/Telos/SKILL.md — liên hệ vấn đề, mục tiêu, dự án với hành động.
- **Cortex memory** — https://docs.ourlifeos.ai/Memory__MemorySystem — học và ghi nhớ có provenance, ranh giới dữ liệu.
- **Release notes v7.40.4** — https://github.com/danielmiessler/LifeOS/releases/tag/v7.40.4 — nhấn mạnh verification gate / proof, không chỉ output tuyên bố thành công.

## 16.2. What We Adopt / Simplify / Defer

| Concept trong repo tham khảo | Thiết kế LifeOS Telegram | Quyết định |
|---|---|---|
| Current State → Ideal State | Goal.current_state + Goal.ideal_state | **P0 adopt** |
| ISA / done claims | Goal Card + versioned SuccessCriteria | **P0 simplify**; không port 12-section ISA |
| Algorithm loop | Articulate → Climb → Verify → Learn trên active goals | **P0 simplify** |
| TELOS mapping | Optional purpose → goal → milestone → task | **P0 lite**; graph P2 |
| Evidence Gates | Verified state từ backend và provenance, không do model tự gắn | **P0 adopt** |
| Cortex durable memory | Operational truth trong DB; confirmed learning P1 | **P0/P1 simplify** |
| Skills/hooks/agents/daemon/Pulse | Domain services + typed tool registry + Telegram; dashboard sau | **Defer** |
| Agent self-improvement/auto-replan | User approval + audit; AI chỉ đề xuất | **Defer** |

## 16.3. Không nhầm nguồn và phạm vi

- Đây là tài liệu thiết kế sản phẩm độc lập, **không tích hợp runtime** hoặc tái sử dụng code của repository tham khảo.
- Thuật ngữ ACVL là cách trình bày 4 hoạt động cho sản phẩm này; tài liệu LifeOS gốc mô tả Algorithm/ISA ở mức rộng hơn và đã thay đổi phiên bản nhiều lần. Không coi ACVL là API hay giao thức chính thức của repo.
- PRD này không yêu cầu model tự phát triển agent, tự truy cập máy local, tự chạy test ngoài, hay tự lấy dữ liệu cá nhân chưa được cấp phép.
- Tên "LifeOS" có thể trùng với dự án tham khảo; cần xem xét đặt tên sản phẩm độc lập nếu phát hành công khai.

---

**End of PRD v0.3.0**

