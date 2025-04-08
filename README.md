# 📱 Android Multi-Function Assistant App

## 👨‍💻 Nhóm thực hiện

- **22110226 – Nguyễn Chí Thanh**  
- **22110218 – Trần Như Quỳnh**  
- **22110213 – Bùi Lê Đông Quân**  

---

## 📌 1. Giới thiệu dự án

Ứng dụng Android hỗ trợ người dùng trong các nhu cầu hằng ngày, bao gồm:

- **Nghe nhạc**
- **Quản lý cuộc gọi & tin nhắn**
- **Quản lý lịch và sự kiện cá nhân**

> Ứng dụng tuân theo chuẩn **Material Design**, sử dụng các thành phần phổ biến như:
- `Broadcast Receiver`
- `Service`
- `AlarmManager`
- `MediaPlayer`
- `Content Provider`

---

## 🎯 2. Mục tiêu chính

### 2.1. Music Playback
- Phát nhạc bằng `MediaPlayer`
- Foreground Service với điều khiển nhạc
- Giao diện phát nhạc hiện đại, trực quan
- Duy trì phát nhạc khi chạy nền
- Điều khiển: **Play / Pause / Next**

### 2.2. SMS & Call Management
- Gửi và nhận tin nhắn SMS
- Thực hiện cuộc gọi
- Quản lý lịch sử cuộc gọi & tin nhắn
- Giao diện đơn giản, dễ dùng
- Phát hiện số bị chặn (blacklist)

### 2.3. Schedule & Event Management
- Tạo & quản lý sự kiện cá nhân
- Nhắc nhở bằng `AlarmManager`
- Hiển thị thông báo đúng giờ
- Quản lý danh sách sự kiện đã hoàn thành

### 2.4. Battery Optimization
- Giám sát pin theo thời gian thực
- Tối ưu hóa hoạt động khi pin yếu / màn hình tắt

---

## 👥 3. Phân công công việc

### 🔹 Trần Như Quỳnh – **SMS & Call Module**
- Thiết kế UI theo Material Design
- `BroadcastReceiver` lắng nghe & gửi SMS
- Xử lý trạng thái cuộc gọi đến / đi
- Hiển thị lịch sử cuộc gọi
- Phát hiện & xử lý số bị chặn

### 🔹 Nguyễn Chí Thanh – **Music Module**
- Thiết kế giao diện phát nhạc
- `MediaPlayer`: phát / tạm dừng / chuyển bài
- `Foreground Service` để duy trì nhạc và thông báo
- Respond to headphone connect/disconnect broadcasts to pause/resume playback.
- Optimize battery

### 🔹 Bùi Lê Đông Quân – **Calendar & Event Module**
- Thiết kế UI lịch và sự kiện
- Tích hợp `AlarmManager` để nhắc nhở
- Logic xử lý sự kiện định kỳ
- Quản lý các sự kiện đã hoàn thành

---

## 🏗️ 4. Kiến trúc kỹ thuật

### 📱 4.1. Activities
| Activity | Mô tả |
|---------|--------|
| `BatterySaverActivity` | Màn hình giám sát và tối ưu hóa pin |
| `BlacklistActivity` | Quản lý danh sách số bị chặn |
| `CalendarActivity` | Lịch và sự kiện cá nhân |
| `CallsActivity` | Lịch sử cuộc gọi đến/đi |
| `EventCompletedActivity` | Danh sách sự kiện đã hoàn thành |
| `ItemSongActivity` | Giao diện phát nhạc theo bài hát |
| `ListSongActivity` | Danh sách bài hát trên thiết bị |
| `MainActivity` | Màn hình chính điều hướng các module |
| `SMSActivity` | Gửi/nhận và quản lý SMS |

### 🔄 4.2. Adapters
- `BlacklistAdapter` – Hiển thị danh sách số bị chặn
- `CallsAdapter` – Hiển thị lịch sử cuộc gọi
- `CompletedEventAdapter` – Danh sách sự kiện hoàn thành
- `EventAdapter` – Danh sách sự kiện hiện tại
- `SMSAdapter` – Danh sách tin nhắn
- `SongAdapter` – Hiển thị bài hát trong `RecyclerView` hoặc `ListView`

### 📦 4.3. Models
- `BlacklistContact` – Dữ liệu số điện thoại bị chặn
- `Event` – Dữ liệu sự kiện cá nhân
- `IncomingCall` – Thông tin cuộc gọi đến
- `SMS` – Dữ liệu tin nhắn SMS
- `Song` – Dữ liệu bài hát

### ⚙️ 4.4. Services
- `BlacklistService` – Xử lý danh sách chặn
- `CallReceiver` – Lắng nghe cuộc gọi đến / đi
- `CallStateManager` – Quản lý trạng thái cuộc gọi
- `EventDatabaseHelper` – Truy vấn cơ sở dữ liệu sự kiện
- `EventReminderReceiver` – Nhận nhắc nhở từ `AlarmManager`
- `SMSReceiver` – Lắng nghe tin nhắn đến
- `SMSService` – Gửi SMS & xử lý dịch vụ
- `SongReceiver` – Nhận sự kiện điều khiển nhạc
- `SongService` – Phát nhạc bằng Foreground Service

### 🛠️ 4.5. Utils
- `Constants` – Các hằng số toàn cục
- `IClickSongItemListener` – Interface xử lý click bài hát
- `SongRepository` – Truy xuất dữ liệu nhạc từ thiết bị

---
