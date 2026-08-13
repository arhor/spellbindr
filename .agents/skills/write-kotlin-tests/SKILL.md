---
name: write-kotlin-tests
description: >-
  Write, add, update, review, or fix automated tests for Kotlin production code while following the repository's
  existing test libraries, architecture, source sets, fixtures, and conventions. Use for Kotlin unit tests, coroutine
  or Flow tests, Android instrumentation tests, Compose UI tests, regression tests, missing edge-case coverage, and
  test updates required by production behavior changes.
---

# Write Kotlin Tests

Produce focused Kotlin tests that match the repository and verify observable behavior.

## Inspect before choosing tools

1. Read repository instructions such as `AGENTS.md`.
2. Read the production code, its public contract, and relevant callers or models.
3. Inspect the closest test class and nearby tests in the same package or feature.
4. Inspect module build configuration and version catalogs before selecting JUnit, assertions, mocks, coroutine
   utilities, Android test APIs, or a source set.
5. Reuse installed libraries, rules, fixtures, factories, fakes, and helpers. Do not add a dependency when the
   repository already provides an appropriate tool.
6. If nearby conventions conflict, follow the tests closest to the production owner unless they conflict with the
   mandatory naming and section rules below.

## Choose the owning source set

Place the test beside the owning package in the narrowest suitable source set:

- Use `app/src/test/kotlin` for JVM unit tests that do not require a device.
- Use `app/src/androidTest/kotlin` for Android, instrumentation, or Compose interaction tests.
- Use `app/src/screenshotTest/kotlin` only for visual rendering or screenshot regression coverage.

Confirm available Gradle tasks instead of assuming a task or source set exists.

## Design behavioral coverage

- Test observable inputs, outputs, effects, state transitions, collaborations, or failures.
- Keep each test focused on one behavioral expectation.
- Keep setup relevant to that expectation.
- Cover the happy path plus meaningful boundaries, errors, and state changes without duplicating equivalent cases.
- Reuse realistic fixtures and test doubles when they make intent clearer.
- Prefer a regression test that fails without the fix when addressing a bug.
- Do not expose private implementation details solely for testing.
- Do not weaken an assertion or broaden timing merely to make a failure disappear.
- Ask for clarification when essential business behavior is ambiguous.

## Apply the mandatory Kotlin test format

Name every test with backticks using exactly this semantic pattern:

```text
{function under test name} should {expected behavior} when {given context}
```

Use the public entry point or observable property as the function-under-test name, such as `refreshUser`, `invoke`,
`dispatch`, or `uiState`.

Divide every test body into exactly three visible sections, once each and in this order:

```kotlin
@Test
fun `loadUser should return cached user when cache contains requested user`() {
    // Given
    val cache = fakeCacheWith(user)

    // When
    val result = repository.loadUser(user.id)

    // Then
    assertThat(result).isEqualTo(user)
}
```

Expression-bodied coroutine tests are allowed when the three sections remain inside the test body:

```kotlin
@Test
fun `refreshUser should emit updated user when remote request succeeds`() = runTest {
    // Given
    coEvery { remote.loadUser() } returns user

    // When
    repository.refreshUser()

    // Then
    assertThat(repository.user.value).isEqualTo(user)
}
```

Do not add other section headers such as `Arrange`, `Act`, `Assert`, or a second `Given`.

## Implement and review

1. Prefer the simplest existing assertion or test-double style that expresses the behavior precisely.
2. Control coroutine scheduling with the repository's existing dispatcher rules and coroutine-test utilities.
3. Avoid sleeps, real network calls, wall-clock dependence, random data, and order dependence.
4. Verify mock interactions only when the interaction is part of the observable contract.
5. Review every changed test for:
   - the required backquoted name;
   - exactly one `// Given`, `// When`, and `// Then`, in order;
   - focused setup and a meaningful assertion;
   - deterministic execution;
   - no unnecessary new dependency or helper.

## Run the narrowest relevant test

Run the smallest available Gradle task that covers the changed test:

- Prefer a class or method filter for JVM tests.
- Run the owning module's unit-test task when filtering is unavailable.
- Run the relevant instrumentation task only when a device or emulator is available.
- Run the owning screenshot-test filter for screenshot previews.

If execution is unavailable, still compile or statically validate the narrowest applicable source set when possible,
and report the exact limitation without claiming the tests passed.
