# Android Coding Guidelines

Standards for project structure, file organization, and code conventions. Structure first — no unnecessary abstractions.

> **How to use with AI:** *"Follow `docs/ANDROID_CODING_GUIDELINES.md` in full."*

---

## 1. Project Structure

### 1.1 Modules

```
project/
├── app/                    # App shell only
├── core/                   # Shared infrastructure
├── feature-<name>/         # One module per feature
├── gradle/
│   └── libs.versions.toml  # All dependency versions
├── build.gradle.kts
└── settings.gradle.kts
```

| Module | Gradle path | What goes here |
|--------|-------------|----------------|
| `app` | `:app` | `Application`, `MainActivity`, `NavGraph`, theme, `AppConstants`, app DI |
| `core` | `:core` | `HttpClient`, `CoreConstants`, `LogUtil`, shared network/logging, core DI |
| `feature-<name>` | `:feature-<name>` | Screen, ViewModel, feature DI |

### 1.2 Dependency Direction

```
app  →  core + feature-*
feature-*  →  core only
core  →  third-party libs only
```

- Features **never** depend on each other.
- Features **never** depend on `app`.
- `app` **never** contains business logic.

### 1.3 Registering a New Module

**`settings.gradle.kts`**
```kotlin
include(":app", ":core", ":feature-login")
```

**`app/build.gradle.kts`**
```kotlin
implementation(project(":feature-login"))
```

**`Application.onCreate()`**
```kotlin
modules(appModules)
modules(coreModules)
modules(loginModules)
```

---

## 2. Package Structure

**Base package:** `com.<company>.<app>`

### 2.1 `app` Module

```
com.<company>.<app>/
├── application/       # <AppName>App.kt
├── di/                # AppModule.kt → appModules
├── navigation/        # NavGraph.kt
├── ui/
│   ├── MainActivity.kt
│   └── theme/         # Color.kt, Type.kt, Theme.kt
└── util/              # AppConstants.kt
```

### 2.2 `core` Module

```
com.<company>.<app>.core/
├── constants/         # CoreConstants.kt
├── di/                # CoreModule.kt, NetworkModule.kt → coreModules
├── network/           # HttpClientProvider.kt
└── util/              # LogUtil.kt
```

### 2.3 `feature-<name>` Module

```
com.<company>.<app>.feature.<name>/
├── di/                # <Name>Module.kt, ViewModelModule.kt → <name>Modules
├── ui/                # <Name>Screen.kt
└── viewModel/         # <Name>ViewModel.kt (+ UiState, Action)
```

No extra layers. No `domain/`, `data/`, `presentation/` folders unless the feature genuinely needs them later.

---

## 3. File & Naming Conventions

| File / Type | Name | Location |
|-------------|------|----------|
| Application class | `<AppName>App.kt` | `app/application/` |
| Main activity | `MainActivity.kt` | `app/ui/` |
| Nav graph | `NavGraph.kt` | `app/navigation/` |
| App constants | `AppConstants.kt` | `app/util/` |
| Core constants | `CoreConstants.kt` | `core/constants/` |
| Theme | `Theme.kt`, `Color.kt`, `Type.kt` | `app/ui/theme/` |
| ViewModel + state | `<Feature>ViewModel.kt` | `feature-<name>/viewModel/` |
| Screen composables | `<Feature>Screen.kt` | `feature-<name>/ui/` |
| DI aggregator | `<Name>Module.kt` | `feature-<name>/di/` |
| ViewModel DI | `ViewModelModule.kt` | `feature-<name>/di/` |
| Network client | `HttpClientProvider.kt` | `core/network/` |
| Unit test | `<ClassUnderTest>Test.kt` | mirrors source package in `src/test/` |

### Naming patterns inside files

| Concept | Pattern | Example |
|---------|---------|---------|
| ViewModel | `<Feature>ViewModel` | `LoginViewModel` |
| UI state | `<Feature>UiState` | `LoginUiState` |
| User action | `<Feature>Action` | `LoginAction` |
| Root composable | `<Feature>ScreenRoot` | `LoginScreenRoot` |
| Content composable | `<Feature>ScreenContent` | `LoginScreenContent` |
| Theme composable | `<AppName>Theme` | `MyAppTheme` |
| DI list | `<scope>Modules` | `appModules`, `coreModules`, `loginModules` |
| DI module | `<scope><Purpose>Module` | `coreNetworkModule`, `viewModelModule` |

---

## 4. No Raw Strings — Constants Only

**Every string literal in code must come from a constant or a string resource. No exceptions.**

### 4.1 Where Strings Live

| String type | Where to define | Example |
|-------------|-----------------|---------|
| Navigation routes | `AppConstants` | `const val NAV_LOGIN = "login"` |
| API paths | `CoreConstants.Api` | `const val PATH_LOGIN = "/auth/login"` |
| HTTP headers | `CoreConstants.Network` | `const val HEADER_AUTH = "Authorization"` |
| Log tags / messages | `CoreConstants.Logging` | `const val TAG_NETWORK = "Network"` |
| Timeouts / thresholds | `CoreConstants.Network` | `const val REQUEST_TIMEOUT_MILLIS = 60_000L` |
| Error messages (code) | `CoreConstants.Errors` or `AppConstants.Errors` | `const val ERR_NETWORK = "Network error"` |
| UI text shown to user | `res/values/strings.xml` | `<string name="login_title">Sign In</string>` |
| Feature-specific non-UI strings | `<Feature>Constants` object in feature module | `const val KEY_TOKEN = "token"` |
| Build-time values | `BuildConfig` | `BuildConfig.API_BASE_URL` |

### 4.2 Rules

```kotlin
// WRONG — never do this
navController.navigate("login")
Text("Sign In")
throw Exception("HTTP error: $code")
Timber.tag("MyTag").d("request failed")
header("Authorization", "Bearer $token")

// CORRECT
navController.navigate(AppConstants.NAV_LOGIN)
Text(stringResource(R.string.login_title))
throw Exception(String.format(CoreConstants.Errors.HTTP_ERROR, code))
Timber.tag(CoreConstants.Logging.TAG_NETWORK).d(CoreConstants.Logging.MSG_REQUEST_FAILED)
header(CoreConstants.Network.HEADER_AUTH, token)
```

- Use `const val` in `object` blocks for compile-time strings.
- Group constants in nested objects by domain (`Network`, `Logging`, `Api`, `Errors`).
- UI-visible text **always** uses `stringResource(R.string.*)` — never hardcoded in composables.
- Format strings use `String.format(Locale.US, constant, args)` — format pattern is also a constant.
- `@Serializable` field names match API contracts and live in DTO files — not scattered as literals in business logic.

### 4.3 Constants File Structure

**`AppConstants.kt`** — app-wide, navigation, feature flags:
```kotlin
object AppConstants {
    object Nav {
        const val LOGIN = "login"
        const val HOME = "home"
    }
}
```

**`CoreConstants.kt`** — network, logging, API:
```kotlin
object CoreConstants {
    object Network {
        const val REQUEST_TIMEOUT_MILLIS = 60_000L
        const val HTTP_STATUS_THRESHOLD = 300
        const val HEADER_CONTENT_TYPE = "Content-Type"
    }
    object Api {
        const val PATH_LOGIN = "/auth/login"
    }
    object Logging {
        const val TAG_NETWORK = "Network"
        const val MSG_REQUEST_FAILED = "Request failed"
    }
    object Errors {
        const val HTTP_ERROR = "HTTP error: %d"
    }
}
```

**`<Feature>Constants.kt`** — only when strings are specific to one feature and don't belong in app/core.

---

## 5. Build Configuration

- All versions in `gradle/libs.versions.toml` — never hardcode in module files.
- Root `build.gradle.kts`: plugins with `apply false` only.
- Repositories via `dependencyResolutionManagement` with `FAIL_ON_PROJECT_REPOS`.
- JVM 11, `compileSdk` = latest stable, `minSdk` = 31.

### Dependencies per module

| Dependency | app | core | feature |
|------------|:---:|:----:|:-------:|
| `project(":core")` | ✓ | — | ✓ |
| Compose + Material 3 | ✓ | — | ✓ |
| Koin + koin-compose | ✓ | ✓ | ✓ |
| Navigation Compose | ✓ | — | — |
| Ktor + Serialization | — | ✓ | — |
| Coroutines + ViewModel | — | — | ✓ |
| Timber | ✓ | ✓ | — |
| JUnit + coroutines-test | ✓ | ✓ | ✓ |
| Compose UI test | ✓ | — | ✓ |

Network stack lives in `core` only. Features do not add Ktor dependencies.

---

## 6. ViewModel

```kotlin
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _navigateNext = MutableStateFlow(false)
    val navigateNext: StateFlow<Boolean> = _navigateNext

    fun handleAction(action: LoginAction) {
        when (action) {
            LoginAction.SUBMIT -> _navigateNext.value = true
            LoginAction.INPUT_CHANGED -> { /* update state */ }
        }
    }

    fun resetNavigation() {
        _navigateNext.value = false
    }
}

data class LoginUiState(
    val title: String = "",  // default from stringResource in UI, not hardcoded here
    val isLoading: Boolean = false,
)

enum class LoginAction {
    SUBMIT,
    INPUT_CHANGED,
}
```

**Rules:**
- `private val _uiState` / `val uiState: StateFlow` — always this pattern.
- `handleAction(action)` — single entry point.
- Navigation via flag `StateFlow` + `resetNavigation()` after handling.
- Async work in `viewModelScope`.
- No `Context`, `NavController`, or `Activity` in ViewModel.
- UiState, Action in the same file as ViewModel.

---

## 7. Compose UI

### 7.1 Screen split

| Composable | Does | Gets |
|------------|------|------|
| `<Feature>ScreenRoot` | ViewModel, collect state, handle navigation side-effects | `NavController`, callbacks |
| `<Feature>ScreenContent` | Renders UI only | `state`, `onAction` |

```kotlin
@Composable
fun LoginScreenRoot(
    onNavigateNext: () -> Unit,
    navController: NavHostController,
) {
    val viewModel: LoginViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateNext by viewModel.navigateNext.collectAsStateWithLifecycle()

    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            onNavigateNext()
            viewModel.resetNavigation()
        }
    }

    LoginScreenContent(
        state = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
) {
    Text(text = stringResource(R.string.login_title))
    Button(onClick = { onAction(LoginAction.SUBMIT) }) {
        Text(text = stringResource(R.string.login_submit))
    }
}
```

**Rules:**
- `collectAsStateWithLifecycle()` for state.
- `LaunchedEffect` for navigation side-effects.
- `*ScreenContent` has no ViewModel, no NavController — testable in isolation.
- All UI text via `stringResource(R.string.*)`.
- Wrap screens in `<AppName>Theme` at activity level.

---

## 8. Navigation

- Single `NavHost` in `NavGraph.kt` inside `app` module.
- Routes defined as constants in `AppConstants.Nav`.
- Feature screens exposed as `*ScreenRoot`.

```kotlin
NavHost(navController, startDestination = AppConstants.Nav.LOGIN) {
    composable(AppConstants.Nav.LOGIN) {
        LoginScreenRoot(
            onNavigateNext = { navController.navigate(AppConstants.Nav.HOME) },
            navController = navController,
        )
    }
}
```

- `navController` passed only to `*ScreenRoot`, never to `*ScreenContent`.
- Navigation callbacks (`onNavigateNext`) decouple ViewModel from NavController.

---

## 9. Dependency Injection (Koin)

```kotlin
// Application.onCreate()
startKoin {
    androidContext(this@MyApp)
    androidLogger()
    modules(appModules, coreModules, loginModules)
}

// core/di/CoreModule.kt
val coreModules = listOf(coreNetworkModule)

val coreNetworkModule = module {
    single { HttpClientProvider.create() }
}

// feature/di/ViewModelModule.kt
val viewModelModule = module {
    viewModelOf(::LoginViewModel)
}

// feature/di/LoginModule.kt
val loginModules = listOf(viewModelModule)
```

| Binding | Scope | Use for |
|---------|-------|---------|
| `single { }` | Singleton | HttpClient, shared services |
| `viewModelOf(::X)` | Per ViewModel | All ViewModels |

---

## 10. Networking (`core` only)

- Single `HttpClient` from `HttpClientProvider.create()` in `core/network/`.
- Provided via Koin `single` in `coreNetworkModule`.
- Timeouts, headers, error messages — all from `CoreConstants`.
- JSON: `ignoreUnknownKeys = true`, `isLenient = true`, `explicitNulls = false`.
- Base URL from `BuildConfig`, not a string literal.
- Do not expose raw exceptions to UI.

---

## 11. Logging

- Timber planted in `Application.onCreate()` — debug builds only.
- All log tags and messages from `CoreConstants.Logging`.
- API logging through `LogUtil` in `core/util/`.
- Never log tokens, passwords, or PII.

---

## 12. Unit Testing

### Location
Mirror source packages: `src/test/java/<same/package>/`

### What to test
| Target | Required |
|--------|----------|
| ViewModel (`handleAction`, state changes, navigation flags) | Yes |
| Utilities in `core` | Yes |
| `*ScreenContent` composables | Yes (Compose UI test) |
| DI modules | No |

### ViewModel test pattern

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun handleAction_submit_setsNavigateNextToTrue() = runTest {
        val viewModel = LoginViewModel()
        viewModel.handleAction(LoginAction.SUBMIT)
        assertTrue(viewModel.navigateNext.value)
    }
}
```

### Compose UI test pattern

Test `*ScreenContent` only — pass state, capture `onAction`:

```kotlin
@Test
fun submitButton_triggersAction() {
    var action: LoginAction? = null
    composeTestRule.setContent {
        LoginScreenContent(state = LoginUiState(), onAction = { action = it })
    }
    composeTestRule.onNodeWithText(context.getString(R.string.login_submit)).performClick()
    assertEquals(LoginAction.SUBMIT, action)
}
```

### Test rules
- `runTest` + `UnconfinedTestDispatcher` for ViewModels.
- One behavior per test.
- No `Thread.sleep()`.
- Test strings via `context.getString(R.string.*)` or constants — not literals.

---

## 13. Adding a New Feature

1. Create `:feature-<name>` module.
2. `include()` in `settings.gradle.kts`.
3. `implementation(project(...))` in `app/build.gradle.kts`.
4. Create packages: `di/`, `ui/`, `viewModel/`.
5. Add `*ViewModel`, `*ScreenRoot`, `*ScreenContent`.
6. Add Koin module; register in `Application`.
7. Add route constant to `AppConstants.Nav`.
8. Add destination in `NavGraph.kt`.
9. Add UI strings to `res/values/strings.xml`.
10. Write ViewModel test + ScreenContent UI test.

---

## 14. Checklist

- [ ] Code in correct module (`app` / `core` / `feature-*`).
- [ ] Package matches structure in section 2.
- [ ] **No raw string literals** — constants or `stringResource` only.
- [ ] Navigation routes in `AppConstants.Nav`.
- [ ] Network/config values in `CoreConstants`.
- [ ] UI text in `strings.xml`.
- [ ] ViewModel uses `StateFlow` pattern with `handleAction`.
- [ ] Screen split into `*ScreenRoot` and `*ScreenContent`.
- [ ] Koin module registered in `Application`.
- [ ] New module in `settings.gradle.kts` and `app/build.gradle.kts`.
- [ ] ViewModel unit test written.
- [ ] ScreenContent UI test written.
- [ ] No secrets in source code.
