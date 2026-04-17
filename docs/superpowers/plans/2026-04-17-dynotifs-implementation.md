# Dynotifs Implementation Plan

**Goal:** Build a production-grade Dynamic Island clone for Android named Dynotifs using Kotlin and Jetpack Compose. Use DynamicIslandMusic as architectural baseline but generalize it for all notifications.

**Architecture:** ForegroundService + WindowManager.TYPE_APPLICATION_OVERLAY with manual calibration. NotificationListenerService broadcasts to Compose State Machine with Priority Queue (Calls > Messages > Timers/Media). Manual calibration stored as percentages in DataStore.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, DataStore, Room, Coroutines, WindowManager, NotificationListenerService

---

## File Structure Overview

```
app/
├── src/main/java/com/suprasidh/dynotifs/
│   ├── app/DynotifsApp.kt
│   ├── data/
│   │   ├── repository/
│   │   │   ├── SettingsRepository.kt
│   │   │   └── AppRegistryRepository.kt
│   │   └── datastore/
│   │       └── DynotifsDataStore.kt
│   ├── di/AppModule.kt
│   ├── domain/
│   │   ├── model/
│   │   │   ├── IslandState.kt
│   │   │   ├── NotificationItem.kt
│   │   │   ├── PriorityLevel.kt
│   │   │   └── CalibrationData.kt
│   │   └── usecase/
│   │       ├── ShowIslandUseCase.kt
│   │       ├── HideIslandUseCase.kt
│   │       └── ManageQueueUseCase.kt
│   ├── overlay/
│   │   ├── OverlayWindowManager.kt
│   │   ├── IslandStateMachine.kt
│   │   └── IslandOverlay.kt
│   ├── services/
│   │   ├── DynotifsForegroundService.kt
│   │   ├── DynotifsNotificationService.kt
│   │   └── CalibratedNotificationListener.kt
│   ├── ui/
│   │   ├── main/
│   │   │   ├── MainActivity.kt
│   │   │   └── DynotifsTheme.kt
│   │   ├── onboarding/
│   │   │   ├── OnboardingScreen.kt
│   │   │   └── CalibrationScreen.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── AppRegistryScreen.kt
│   │   └── components/
│   │       ├── IslandPill.kt
│   │       ├── IslandExpanded.kt
│   │       └── TimerDisplay.kt
│   └── util/
│       ├── Constants.kt
│       ├── PermissionsHelper.kt
│       └── Extensions.kt
├── src/main/res/
├── src/test/
│   └── java/com/suprasidh/dynotifs/
│       └── PriorityQueueTest.kt
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/android.yml
```

---

## Module 1: Hardware Calibration Engine

### Task 1.1: Create DataStore for Calibration

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/data/datastore/DynotifsDataStore.kt`
- Create: `app/src/main/java/com/suprasidh/dynotifs/data/model/CalibrationData.kt`

- [ ] **Step 1: Create CalibrationData model**

```kotlin
data class CalibrationData(
    val offsetXPercent: Float = 0.5f,  // 50% of screen width (center)
    val offsetYPercent: Float = 0.02f, // 2% from top
    val widthPercent: Float = 0.30f,  // 30% of screen width
    val heightPercent: Float = 0.05f   // 5% of screen height (aspect ratio)
)
```

- [ ] **Step 2: Create DynotifsDataStore with Preferences**

```kotlin
class DynotifsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: PreferencesDataStore by preferencesDataStore(name = "dynotifs_prefs")
    
    val calibrationFlow: Flow<CalibrationData> = dataStore.data.map { prefs ->
        CalibrationData(
            offsetXPercent = prefs[PreferencesKeys.OFFSET_X] ?: 0.5f,
            offsetYPercent = prefs[PreferencesKeys.OFFSET_Y] ?: 0.02f,
            widthPercent = prefs[PreferencesKeys.WIDTH] ?: 0.30f,
            heightPercent = prefs[PreferencesKeys.HEIGHT] ?: 0.05f
        )
    }
    
    suspend fun updateCalibration(data: CalibrationData) { ... }
}
```

### Task 1.2: Create Calibration UI

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/ui/onboarding/CalibrationScreen.kt`

- [ ] **Step 1: Create Compose Calibration Screen**

```kotlin
@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit,
    viewModel: CalibrationViewModel = hiltViewModel()
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("X Offset")
        Slider(
            value = calibration.offsetXPercent,
            onValueChange = { viewModel.updateXOffset(it) },
            valueRange = 0f..1f
        )
        // Repeat for Y, Width, Height
        Button(onClick = onCalibrationComplete) {
            Text("Apply Calibration")
        }
    }
}
```

---

## Module 2: State Machine & Priority Queue

### Task 2.1: Create Notification Models

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/domain/model/NotificationItem.kt`
- Create: `app/src/main/java/com/suprasidh/dynotifs/domain/model/PriorityLevel.kt`

```kotlin
enum class PriorityLevel {
    TIER_1_CALL,    // Absolute Override - CATEGORY_CALL
    TIER_2_MESSAGE, // Transient - CATEGORY_MESSAGE
    TIER_3_TIMER,   // Persistent - CATEGORY_ALARM, CATEGORY_TIMER
    TIER_4_MEDIA    // Persistent - CATEGORY_TRANSPORT
}

data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: Bitmap?,
    val category: String?,
    val priorityLevel: PriorityLevel,
    val contentIntent: PendingIntent?,
    val actions: List<NotificationAction> = emptyList(),
    val remoteInput: RemoteInput? = null,
    val chronometerBase: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Task 2.2: Create Priority Queue

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/domain/queue/PriorityNotificationQueue.kt`

- [ ] **Step 1: Write Unit Tests**

```kotlin
class PriorityQueueTest {
    @Test
    fun testCallOverridesMessage() {
        val call = createNotificationItem(key = "call", category = CATEGORY_CALL)
        val message = createNotificationItem(key = "msg", category = CATEGORY_MESSAGE)
        
        val queue = PriorityNotificationQueue()
        queue.enqueue(call)
        queue.enqueue(message)
        
        assertEquals(call, queue.peek())
    }
    
    @Test
    fun testTransientMessageAutoCollapse() {
        val message = createNotificationItem(key = "msg", category = CATEGORY_MESSAGE)
        val queue = PriorityNotificationQueue()
        queue.enqueue(message)
        
        queue.startCollapseTimer(message.key, 5000L)
        
        verify(queue, timeout(6000).never()).contains(message.key)
    }
}
```

- [ ] **Step 2: Implement Priority Queue**

```kotlin
class PriorityNotificationQueue {
    private val queue = ArrayDeque<NotificationItem>()
    private val priorityOrder = listOf(
        PriorityLevel.TIER_1_CALL,
        PriorityLevel.TIER_2_MESSAGE,
        PriorityLevel.TIER_3_TIMER,
        PriorityLevel.TIER_4_MEDIA
    )
    
    fun enqueue(item: NotificationItem) {
        val index = queue.indexOfFirst { it.priorityLevel > item.priorityLevel }
        if (index >= 0) queue.add(index, item)
        else queue.addLast(item)
    }
    
    fun peek(): NotificationItem? = queue.firstOrNull()
    fun dequeue(): NotificationItem? = queue.removeFirstOrNull()
    fun cancel(key: String) { queue.removeAll { it.key == key } }
}
```

### Task 2.3: Create Island State Machine

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/overlay/IslandStateMachine.kt`

```kotlin
sealed class IslandState {
    data object Hidden : IslandState()
    data object Pill : IslandState()
    data object Expanded : IslandState()
    data object Expanding : IslandState()
    data object Collapsing : IslandState()
}

class IslandStateMachine @Inject constructor(
    private val queue: PriorityNotificationQueue,
    private val showIsland: ShowIslandUseCase,
    private val hideIsland: HideIslandUseCase
) {
    private val _state = MutableStateFlow<IslandState>(IslandState.Hidden)
    val state: StateFlow<IslandState> = _state
    private val _currentItem = MutableStateFlow<NotificationItem?>(null)
    val currentItem: StateFlow<NotificationItem?> = _currentItem
}
```

---

## Module 3: Overlay & Window Manager

### Task 3.1: Create Overlay Window Manager

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/overlay/OverlayWindowManager.kt`

- [ ] **Step 1: Configure WindowManager LayoutParams**

```kotlin
class OverlayWindowManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DynotifsDataStore
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    private fun getPillLayoutParams(calibration: CalibrationData): LayoutParams {
        val metrics = context.resources.displayMetrics
        val width = (metrics.widthPixels * calibration.widthPercent).toInt()
        val height = (metrics.heightPixels * calibration.heightPercent).toInt()
        val x = (metrics.widthPixels * calibration.offsetXPercent).toInt()
        val y = (metrics.heightPixels * calibration.offsetYPercent).toInt()
        
        return LayoutParams(
            width, height,
            TYPE_APPLICATION_OVERLAY,
            FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            this.x = x
            this.y = y
            layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}
```

### Task 3.2: Handle Orientation Changes

- [ ] **Step 1: Disable in Landscape**

```kotlin
private fun checkOrientation(): Boolean {
    val config = context.resources.configuration
    return config.orientation == Configuration.ORIENTATION_LANDSCAPE
}
```

### Task 3.3: Implement Keyboard Bridge

- [ ] **Step 1: Toggle Focus for Expanded Reply**

```kotlin
fun setFocusable(focusable: Boolean) {
    val currentParams = layoutParams
    if (focusable) {
        currentParams.flags &= ~FLAG_NOT_FOCUSABLE
    } else {
        currentParams.flags |= FLAG_NOT_FOCUSABLE
    }
    wm.updateViewLayout(view, currentParams)
}
```

---

## Module 4: App Registry

### Task 4.1: Create Room Database

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/data/database/AppRegistryDatabase.kt`

```kotlin
@Entity(tableName = "app_registry")
data class RegisteredApp(
    @PrimaryKey val packageName: String,
    val isBlocked: Boolean = false,
    val isMonitored: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
```

### Task 4.2: Create Settings UI

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/ui/settings/AppRegistryScreen.kt`

---

## Module 5: Safety & Anti-Bricking

### Task 5.1: Create Foreground Service with Kill

**Files:**
- Create: `app/src/main/java/com/suprasidh/dynotifs/services/DynotifsForegroundService.kt`

```kotlin
class DynotifsForegroundService : Service() {
    override fun onCreate() {
        startForeground(NOTIF_ID, buildNotification())
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(0, "KILL DYNOTIFS", killPendingIntent)
            .build()
    }
    
    private val killPendingIntent: PendingIntent = PendingIntent.getService(
        this, 0,
        Intent(this, DynotifsForegroundService::class.java).apply { 
            action = ACTION_STOP_SERVICE 
        },
        PendingIntent.FLAG_IMMUTABLE
    )
}
```

### Task 5.2: Wrap WindowManager in Try-Catch

```kotlin
fun safeAddView(view: View, params: LayoutParams) {
    try {
        wm.addView(view, params)
    } catch (e: IllegalArgumentException) {
        log.e("Bad params: ${e.message}")
    } catch (e: WindowManager.BadTokenException) {
        log.e("Permission denied: ${e.message}")
    }
}
```

---

## Module 6: CI/CD Pipeline

### Task 6.1: Create GitHub Actions Workflow

**Files:**
- Create: `.github/workflows/android.yml`

```yaml
name: Android CI

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
      - uses: android-actions/setup-android-sdk@v2
      
      - name: Build APK
        run: ./gradlew assembleRelease
      
      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Dynotifs-V1-QA
          path: app/build/outputs/apk/release/app-release.apk
```

---

## Task Breakdown Summary

| Module | Tasks | Priority |
|--------|------|----------|
| 1. Calibration | 1.1, 1.2 | High |
| 2. State Machine | 2.1, 2.2, 2.3 | High |
| 3. Overlay | 3.1, 3.2, 3.3 | High |
| 4. App Registry | 4.1, 4.2 | Medium |
| 5. Safety | 5.1, 5.2 | Critical |
| 6. CI/CD | 6.1 | High |

---