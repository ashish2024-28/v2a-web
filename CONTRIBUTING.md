# Contributing to V2S

## Source organization

- Put shared platform helpers in `core`.
- Put domain objects in `data/model`.
- Put MediaStore/SharedPreferences access in `data/local`.
- Put data composition in `data/repository`.
- Put user-facing feature behavior in the matching feature package.
- Keep Activity classes focused on UI and navigation.

## Naming

Use descriptive names:

- `MediaScanner` instead of `Helper`.
- `PlaylistDataSource` instead of `Store2`.
- `SearchEngine` instead of `SearchUtil`.

## Documentation

Every Java source file should start with a class-level comment describing its role. Every method should have a short comment describing what it does.

## Validation

Before submitting a change:

1. Sync Gradle.
2. Build debug.
3. Run the relevant QA checklist items.
4. Run a clean release build when build configuration changes.
5. Update `CHANGELOG.md` for user-visible behavior.
