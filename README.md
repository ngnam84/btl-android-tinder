# Bài tập lớn học phần Phát triển ứng dụng cho các thiết bị di động


## E22CNPM02 - Nhóm 01


## Đề tài: Ứng dụng Mạng xã hội Kết nối & Hẹn hò trực tuyến LoveMatch

📌 Lý do chọn đề tài:

  Trong bối cảnh công nghệ phát triển mạnh mẽ và thời gian dành cho giao tiếp trực tiếp ngày càng hạn chế, một nền tảng ghép đôi tiện lợi, thông minh và an toàn trở nên vô cùng cần thiết. Người dùng hiện nay mong muốn các ứng dụng không chỉ gợi ý ngẫu nhiên, mà phải mang đến trải nghiệm được “may đo” theo sở thích, phong cách sống và nhu cầu riêng. Việc xây dựng một ứng dụng hẹn hò sẽ giúp tận dụng xu hướng này bằng cách phát triển các thuật toán ghép đôi theo hướng cá nhân hóa sâu hơn, nâng cao trải nghiệm người dùng và tăng mức độ hài lòng khi sử dụng ứng dụng.

📌 Tổng quát về ứng dụng:
<img width="2613" height="1630" alt="mermaid-diagram-2025-12-12-094110" src="https://github.com/user-attachments/assets/aebbeba9-976c-45dd-813a-6aec0a065a6d" />

| | | |
|---|---|---|
| ![Screen_recording_20251212_091042 (1)](https://github.com/user-attachments/assets/82b1d3f4-d663-4a36-b872-e218eb3316b2) | ![Screen_recording_20251212_091042 (5)](https://github.com/user-attachments/assets/0830a03d-6041-4bc5-8ce8-6a9a68dce5a3)| ![Screen_recording_20251212_091042 (4)](https://github.com/user-attachments/assets/478ef859-03bc-4bf6-a692-1fc0cdd267d2) |

# 🔄 LUỒNG HOẠT ĐỘNG ỨNG DỤNG TINDER CLONE

## 📱 1. LUỒNG ĐĂNG KÝ & THIẾT LẬP TÀI KHOẢN

### 1.1. Đăng ký lần đầu
```
Người dùng mở app 
--> Màn hình Splash 
--> Chọn "Sign Up"
--> Nhập Username, Email, Password
--> Nhấn "Sign Up"
--> Firebase xác thực và tạo tài khoản
--> Tự động chuyển đến màn hình Setup Profile (First Time)
```

### 1.2. Thiết lập hồ sơ lần đầu (Bắt buộc)
```
Màn hình FTS Profile
--> Upload ảnh đại diện (bắt buộc)
--> Nhập Name (bắt buộc)
--> Chọn City/Location (có autocomplete từ Firestore)
--> Nhập Bio
--> Chọn Gender (Male/Female) - bắt buộc
--> Chọn Gender Preference (Male/Female/Any) - bắt buộc
--> Chọn Interests (tìm kiếm hoặc thêm mới)
--> Nhấn "Continue"
--> Lưu vào Firestore với ftsComplete = true
--> Chuyển đến màn hình Swipe
```

---

## 🔐 2. LUỒNG ĐĂNG NHẬP

### 2.1. Đăng nhập thông thường
```
Người dùng mở app
--> Màn hình Splash
--> Chọn "Login"
--> Nhập Email, Password
--> Nhấn "Login"
--> Firebase xác thực
--> Kiểm tra ftsComplete
    |
    ├─> Nếu ftsComplete = false --> Chuyển đến FTS Profile
    └─> Nếu ftsComplete = true --> Chuyển đến màn hình Profile/Swipe
--> Kết nối Stream Chat (getStreamToken)
--> Đăng ký FCM token cho notifications
```

### 2.2. Quên mật khẩu
```
Màn hình Login
--> Chọn "Forgot Password"
--> Nhập Email
--> Firebase gửi email reset password
--> Người dùng check email và reset
--> Quay lại Login
```

---

## 💕 3. LUỒNG MATCHING & SWIPING

### 3.1. Xem và đánh giá profiles
```
Màn hình Swipe
--> Hệ thống tải danh sách profiles phù hợp:
    • Lọc theo gender preference (2 chiều)
    • Loại trừ người đã swipe/match
    • Tính match score (70% interests + 30% location)
    • Sắp xếp theo score tăng dần
--> Hiển thị profile card đầu tiên
--> Người dùng xem thông tin:
    • Ảnh đại diện
    • Tên, tuổi
    • Location
    • Bio
    • Match score (%)
    • Interests
```

### 3.2. Swipe Left (Dislike)
```
Người dùng vuốt trái HOẶC nhấn nút X
--> Thêm userId vào swipesLeft trong Firestore
--> Ẩn profile khỏi danh sách
--> Hiển thị profile tiếp theo
```

### 3.3. Swipe Right (Like)
```
Người dùng vuốt phải HOẶC nhấn nút ❤️
--> Kiểm tra người kia đã like mình chưa
    |
    ├─> CHƯA: 
    |   --> Thêm userId vào swipesRight
    |   --> Hiển thị profile tiếp theo
    |
    └─> ĐÃ LIKE (Reciprocal Match):
        --> Xóa khỏi swipesRight của cả 2
        --> Thêm vào matches của cả 2
        --> Hiển thị popup "It's a Match! 💕"
        --> Tạo Stream Chat channel mới
        --> Gửi tin nhắn chào tự động
        --> Cho phép chat với nhau
```

### 3.4. Xem chi tiết profile
```
Màn hình Swipe
--> Nhấn vào profile card
--> Mở Profile Detail Screen:
    • Ảnh profile lớn
    • Thông tin đầy đủ
    • Match score
    • Danh sách bài posts của người đó
    • Có thể vuốt để like/dislike ngay tại đây
--> Nhấn Back để quay lại
```

---

## 💬 4. LUỒNG CHAT & MESSAGING

### 4.1. Xem danh sách matches
```
Bottom Navigation --> Nhấn icon Chat
--> Màn hình Chat List
--> Hiển thị danh sách người đã match
--> Mỗi channel hiển thị:
    • Avatar
    • Tên
    • Tin nhắn mới nhất
    • Số tin nhắn chưa đọc
    • Thời gian
```

### 4.2. Gửi & nhận tin nhắn
```
Chat List --> Chọn một match
--> Mở màn hình Single Chat (Stream UI)
--> Người dùng gõ tin nhắn
--> Nhấn Send
--> Tin nhắn gửi qua Stream Chat API
--> Người nhận:
    • Nếu ONLINE: Nhận real-time
    • Nếu OFFLINE: Nhận push notification qua FCM
```

### 4.3. Nhận thông báo
```
App đang BACKGROUND/CLOSED
--> Người khác gửi tin nhắn
--> Stream Chat trigger webhook
--> Firebase Cloud Messaging (FCM) gửi notification
--> Thiết bị nhận notification với:
    • Title: Tên người gửi
    • Body: Nội dung tin nhắn
    • Channel ID để mở đúng chat
--> Người dùng nhấn notification
--> App mở và navigate đến chat đó
```

### 4.4. Video Call
```
Màn hình Single Chat
--> Nhấn icon Video Call
--> Tạo callId từ channelId
--> Mở Video Call Screen
--> Khởi tạo Stream Video SDK:
    • Tạo/join call với callId
    • Request camera & microphone permissions
    • Kết nối video/audio streams
--> Hiển thị UI cuộc gọi:
    • Video local
    • Video remote
    • Nút bật/tắt camera
    • Nút bật/tắt micro
    • Nút end call
--> Khi kết thúc:
    • Tính thời lượng cuộc gọi
    • Gửi tin nhắn "Cuộc gọi đã kết thúc • [thời gian]"
    • Đóng màn hình và quay về chat
```

---

## 📱 5. LUỒNG SOCIAL FEATURES (POSTS)

### 5.1. Tạo bài viết mới
```
Bottom Navigation --> Nhấn Profile
--> Profile Screen --> Nhấn "+" (Create Post)
--> Màn hình Create Post
--> Nhập caption (tùy chọn)
--> Nhấn "Add Photo/Video"
--> Chọn ảnh/video từ thiết bị (tối đa 10 files)
--> Preview media đã chọn
--> Nhấn "POST"
--> Upload từng file lên Firebase Storage:
    • Images --> /posts/{userId}/images/{uuid}
    • Videos --> /posts/{userId}/videos/{uuid}
--> Lưu PostData vào Firestore:
    • Collection: user/{userId}/posts/{postId}
    • Fields: caption, media[], timestamp, likes[]
--> Quay về Profile Screen
--> Hiển thị post mới tạo
```

### 5.2. Xem bài viết của bạn bè (matches)
```
Bottom Navigation --> Nhấn icon "Friend Posts"
--> Màn hình Friend Post Screen
--> Hiển thị danh sách avatar của tất cả matches ở trên
--> Load posts từ tất cả người đã match:
    • Query: user/{matchId}/posts
    • Merge và sort theo timestamp giảm dần
--> Hiển thị feed posts:
    • Avatar + tên người đăng
    • Caption
    • Media (ảnh/video) với pager nếu nhiều
    • Số lượt like
    • Comments section
```

### 5.3. Like bài viết
```
Friend Post Screen / Profile Detail
--> Nhấn icon ❤️ trên post
--> Kiểm tra đã like chưa:
    |
    ├─> Chưa like: 
    |   --> Thêm currentUserId vào likes[]
    |   --> Icon chuyển sang đỏ
    |   --> Số lượt like tăng
    |
    └─> Đã like:
        --> Xóa currentUserId khỏi likes[]
        --> Icon chuyển về màu xám
        --> Số lượt like giảm
--> Update Firestore real-time
```

### 5.4. Comment bài viết
```
Friend Post Screen
--> Nhấn vào ô "Add a comment..."
--> Gõ nội dung comment
--> Nhấn Send hoặc Enter
--> Tạo CommentData:
    • commentId (auto-generated)
    • text
    • username, userImage, userId
    • timestamp (server timestamp)
--> Lưu vào: user/{authorId}/posts/{postId}/comments/{commentId}
--> Comment hiển thị ngay (real-time listener)
```

### 5.5. Xóa post/comment của mình
```
Xóa Post:
--> Nhấn icon X trên post (chỉ hiện nếu là post của mình)
--> Hiển thị confirm dialog
--> Nhấn "Delete"
--> Xóa tất cả media từ Storage (parallel)
--> Xóa document từ Firestore
--> Post biến mất khỏi UI

Xóa Comment:
--> Nhấn icon X trên comment (chỉ hiện nếu là comment của mình)
--> Hiển thị confirm dialog
--> Nhấn "Delete"
--> Xóa document comment từ Firestore
--> Comment biến mất khỏi UI
```

---

## 👤 6. LUỒNG QUẢN LÝ PROFILE

### 6.1. Xem profile của mình
```
Bottom Navigation --> Nhấn icon Profile
--> Profile Screen hiển thị:
    • Ảnh đại diện lớn
    • Tên, location
    • Bio
    • Interests (dạng chips)
    • Danh sách posts của mình
--> Có nút Settings (⚙️) góc trên
```

### 6.2. Chỉnh sửa profile
```
Profile Screen --> Nhấn icon Settings
--> Edit Profile Screen
--> Cho phép chỉnh sửa:
    • Upload ảnh mới
    • Username
    • Name
    • Bio
    • Gender (Male/Female)
    • Gender Preference (Male/Female/Any)
    • City/Location (autocomplete)
    • Interests (search + add mới)
--> Nhấn "Save"
--> Validation:
    • Nếu có thay đổi address và không có lat/long
    --> Gọi Geoapify API để geocode
    --> Lấy lat/long từ address
--> Update Firestore
--> Quay về Profile Screen
```

### 6.3. Đổi mật khẩu
```
Edit Profile Screen --> Nhấn "Change Password"
--> Hiển thị dialog:
    • Current Password
    • New Password
    • Confirm New Password
--> Nhấn "Save"
--> Firebase re-authenticate với current password
--> Nếu thành công --> updatePassword(new password)
--> Hiển thị thông báo "Password updated successfully"
```

### 6.4. Xóa tài khoản
```
Edit Profile Screen --> Nhấn "Delete Account"
--> Hiển thị confirm dialog (màu đỏ)
--> Nhập password để xác nhận
--> Nhấn "DELETE"
--> Firebase re-authenticate
--> Xóa tất cả data theo thứ tự:
    1. Xóa tất cả media từ Storage (profile + posts)
    2. Xóa tất cả posts documents
    3. Xóa user document từ Firestore
    4. Xóa user từ Firebase Auth
--> Disconnect Stream Chat
--> Đăng xuất
--> Quay về Login Screen
```

---

## 🔄 7. LUỒNG ĐĂNG XUẤT

```
Profile/Settings --> Nhấn "Logout"
--> Xóa FCM device token khỏi Stream Chat
--> Disconnect Stream Chat (flushPersistence = true)
--> Firebase Auth signOut()
--> Clear local user data
--> signInState = SIGNED_OUT
--> Navigate về Login Screen
--> Hiển thị "Logged out"
```

---

## 🔔 8. LUỒNG NOTIFICATIONS

### 8.1. Đăng ký nhận thông báo
```
App khởi động
--> Request notification permission (Android 13+)
--> Lấy FCM token từ Firebase Messaging
--> Khi connect Stream Chat thành công
--> Đăng ký device token với Stream:
    • Device(token, pushProvider=FIREBASE)
    • chatClient.addDevice()
```

### 8.2. Nhận và xử lý notification
```
App ở BACKGROUND
--> Stream Chat phát hiện tin nhắn mới
--> Gửi push notification qua FCM
--> FirebaseMessagingService.onMessageReceived()
--> Kiểm tra app có đang foreground không:
    |
    ├─> FOREGROUND: Không hiện notification (Stream UI tự xử lý)
    |
    └─> BACKGROUND:
        --> Parse notification data:
            • channelId
            • sender name
            • message text
        --> Tạo Android notification với PendingIntent
        --> Người dùng tap notification
        --> MainActivity mở và navigate đến chat
```

---

## 📊 9. THUẬT TOÁN MATCHING

### 9.1. Tính điểm tương đồng
```
Khi load profiles cho Swipe Screen:

1. Lọc ứng cử viên (candidates):
   --> Query Firestore theo điều kiện:
       • gender = user's genderPreference
       • genderPreference = user's gender HOẶC ANY
       • userId != currentUserId
       • Chưa có trong swipesLeft, swipesRight, matches

2. Tính Match Score cho mỗi candidate:
   
   A. Interest Score (70%):
      --> Jaccard Similarity:
          • Intersection = số interests chung
          • Union = tổng số interests unique
          • Score = Intersection / Union
   
   B. Distance Score (30%):
      --> Tính khoảng cách Haversine (km):
          • Dùng lat/long của 2 người
      --> Normalize:
          • Nếu > 100km --> score = 0
          • Nếu <= 100km --> score = 1 - (distance/100) - 0.5
   
   C. Final Score:
      --> (0.7 × InterestScore) + (0.3 × DistanceScore)

3. Sắp xếp:
   --> Lọc những người có score >= 0.0
   --> Sort theo score TĂNG DẦN (thấp nhất lên trước)
   --> Hiển thị từ dưới lên (stack)
```

---

## 🎯 10. CÁC TÍNH NĂNG ĐẶC BIỆT

### 10.1. Interests Management
```
Khi chọn interests:
--> Gõ tên interest vào search box
--> Hệ thống tìm kiếm trong Firestore:
    • Collection: interests
    • Where: approved = true
    • OrderBy: usageCount DESC
    • Filter: name contains search text
--> Hiển thị suggestions
--> Chọn từ suggestion:
    --> Thêm vào selectedInterests
    --> Increment usageCount trong Firestore
--> Hoặc thêm mới:
    --> Tạo InterestData mới:
        • userGenerated = true
        • approved = false (cần admin duyệt)
        • usageCount = 1
    --> Lưu vào Firestore
    --> Thêm vào selectedInterests
```

### 10.2. City Autocomplete
```
Khi nhập city:
--> Mỗi keystroke trigger searchCities()
--> Query Firestore collection "cities":
    • startAt(query)
    • endAt(query + "\uf8ff")
    • limit(10)
--> Hiển thị dropdown suggestions
--> Chọn city từ dropdown:
    --> Lưu city name, lat, lng
--> Hoặc tự nhập không chọn:
    --> Khi Save, gọi Geoapify API:
        • GET https://api.geoapify.com/v1/geocode/search
        • Params: text=address, apiKey
        • Parse response để lấy lat/lng
```

### 10.3. Real-time Updates
```
Sử dụng Firestore Snapshot Listeners:

1. User Data:
   --> db.collection("user").document(uid).addSnapshotListener
   --> Mỗi khi data thay đổi --> cập nhật UI ngay

2. Posts:
   --> db.collection("user/{uid}/posts").addSnapshotListener
   --> Posts mới/sửa/xóa --> cập nhật feed real-time

3. Comments:
   --> Flow<List<CommentData>> với snapshot listener
   --> Comments mới --> hiển thị ngay không cần refresh

4. Match Profiles:
   --> Listener trên user collection với query filters
   --> Profile mới phù hợp --> thêm vào stack
```

---

## ⚙️ 11. XỬ LÝ LỖI & EDGE CASES

### 11.1. Network Errors
```
Khi có lỗi network:
--> Hiển thị CommonProgressSpinner nếu đang loading
--> Catch exception trong try-catch
--> Hiển thị popup notification với error message
--> User có thể retry bằng cách thực hiện action lại
```

### 11.2. Empty States
```
Không có profiles để swipe:
--> Hiển thị "No more profiles available"

Không có matches:
--> Hiển thị "You haven't matched with anyone yet"

Không có posts:
--> Friend Posts: "Your friends haven't posted anything yet"
--> Own Profile: "No posts yet. Tap + to create your first post!"
```

### 11.3. Permissions
```
Camera/Microphone cho video call:
--> LaunchCallPermissions tự động request
--> Nếu bị từ chối:
    --> Hiển thị dialog giải thích
    --> Hướng dẫn vào Settings để bật

Notifications (Android 13+):
--> Request khi app khởi động
--> Nếu bị từ chối:
    --> User vẫn dùng app bình thường
    --> Nhưng không nhận push notifications
```

---

## 🔐 12. BẢO MẬT & AUTHENTICATION

### 12.1. Token Management
```
Firebase Auth:
--> Mỗi request quan trọng:
    • firebaseUser.getIdToken(true) // Force refresh
    • Sử dụng fresh token cho API calls

Stream Chat:
--> Token từ Firebase Extension:
    • Call Cloud Function: getStreamUserToken
    • Truyền Firebase ID token
    • Nhận Stream token
    • Dùng để connectUser()
```

### 12.2. Data Validation
```
Trước khi lưu Firestore:
--> Validate tất cả required fields
--> Sanitize user input
--> Check permissions (chỉ owner mới sửa/xóa được)
```

---

## 📈 13. PERFORMANCE OPTIMIZATION

```
1. Image Loading:
   --> Sử dụng Coil library
   --> Cache images automatically
   --> Placeholder khi đang load

2. Lazy Loading:
   --> LazyColumn cho lists
   --> Pagination nếu cần (chưa implement)

3. Coroutines:
   --> Tất cả I/O operations chạy trong coroutines
   --> Dispatchers.IO cho network/database
   --> Dispatchers.Main cho UI updates

4. State Management:
   --> MutableState cho UI reactivity
   --> StateFlow cho data streams
   --> Snapshot listeners cho real-time sync
```

---





