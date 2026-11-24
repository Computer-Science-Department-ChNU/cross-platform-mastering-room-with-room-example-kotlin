# Room exercise: from version 1 to version 5

This is the "Practice" section of `room-kmp-lecture-en.adoc`. The code in the repository is at database **version 1**, which covers Practice 1 (a To-Do list on all three platforms). Each later practice task is one database version. You can type each step by hand using the key code below, or apply the ready-made patch, which also includes the UI and tests:

| Step | Practice task | DB version | Migration | Patch |
|---|---|---|---|---|
| 1 | To-Do data layer + UI | 1 | – | already in the code |
| 2 | `Category` one-to-many, FK `SET_NULL` | 2 | automated | `docs/steps/step-2-categories.patch` |
| 3 | Tags, many-to-many | 3 | automated | `docs/steps/step-3-tags.patch` |
| 4 | Rename `title` → `name` | 4 | automated + `AutoMigrationSpec` | `docs/steps/step-4-rename-columns.patch` |
| 5 | Move notes to `task_notes` (one-to-one) | 5 | **manual** | `docs/steps/step-5-task-notes.patch` |

Every step has been tested by applying the patches in order to a clean copy of version 1. After each patch, all tests passed and Android, iOS and Desktop compiled. A database file created at v1 (with a task in it) was reopened after every step, and the task was still there.

## Architecture (all in `shared/src/commonMain`)

```
data/
  local/            Room: AppDatabase, entity/, dao/, relation/, mapper/, Migrations.kt (from step 4)
  model/            Plain models exposed to the UI (Task, Category, Tag) – the UI never sees entities
  repository/       Interfaces (TaskRepository, ...) + Room implementations (RoomTaskRepository, ...)
ui/tasks/           MVVM: TasksViewModel (StateFlow<TasksUiState>) + stateless Compose screens/dialogs
di/Koin.kt          dataModule, uiModule, initKoin(); expect fun platformModule()
```

Only two kinds of code are platform-specific:
- `getDatabaseBuilder()` in `androidMain`, `iosMain` and `jvmMain`, which decides where the database file is stored.
- `platformModule()` in the same three source sets, which provides that builder to Koin.

Koin is started in `MasteringDbApplication` (Android), `main.kt` (Desktop) and `iOSApp.init` (iOS).

Database file locations (delete the file to start from scratch):
- **Desktop (macOS):** `~/Library/Application Support/MasteringDB/mastering_db.db` (`%APPDATA%\MasteringDB` on Windows, `~/.local/share/MasteringDB` on Linux)
- **Android:** app data (*App Inspection → Database Inspector*), or uninstall the app
- **iOS simulator:** the app's `Documents/` directory, or delete the app

## Workflow for every step

```bash
# 0. Run the CURRENT version once and create some data (tasks with descriptions, done/not done, ...)
./gradlew :desktopApp:run

# 1. Apply the next step (or make the changes by hand, see below)
patch -p1 < docs/steps/step-2-categories.patch

# 2. Build -> Room exports shared/schemas/.../2.json (commit it if you use Git!)
# 3. Run the tests (MigrationTest uses the exported JSON files)
./gradlew :shared:jvmTest
./gradlew :shared:jvmTest --tests "*MigrationTest*"      # only migrations
./gradlew :shared:jvmTest --tests "*MigrationTest.migrate1To2*"

# 4. Run the app again WITHOUT deleting the database -> your old data must still be there
./gradlew :desktopApp:run
```

> **Do not go back to a lower version with an existing database file.** If you reverse a patch with `patch -R -p1 < ...`, Room finds a database version newer than the code and throws `IllegalStateException` on startup. To go back, delete the database file as well.

---

## Step 2 – Categories (one-to-many, automated migration 1 → 2)

New table `categories`. `tasks` gets a nullable `category_id` with a foreign key. Deleting a category sets `category_id` to `NULL` in its tasks, so the tasks are kept.

```kotlin
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

// TaskEntity
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["is_done"]), Index(value = ["category_id"])],   // always index FK columns
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["category_id"],
        onDelete = ForeignKey.SET_NULL,
    )],
)
data class TaskEntity(/* ... */ @ColumnInfo(name = "category_id") val categoryId: Long? = null)

data class CategoryWithTasks(
    @Embedded val category: CategoryEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["category_id"]) val tasks: List<TaskEntity>,
)

// CategoryDao
@Transaction
@Query("SELECT * FROM categories ORDER BY name")
fun observeCategoriesWithTasks(): Flow<List<CategoryWithTasks>>

// AppDatabase
@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
```

The patch also adds:
- `CategoryRepository`, plus a Koin binding for it.
- In the UI: a category filter row, a category picker in the task dialog, and a "Manage…" dialog for adding and deleting categories.
- `MigrationTest`, which uses `MigrationTestHelper`, and `CategoryDaoTest`.

**Check:** your old tasks are still there and have no category. Create a category, assign it to a task, then delete the category. The task stays and loses its category.

**Why no default value is needed:** the new column is nullable, so existing rows simply get `NULL`. A new *NOT NULL* column would need `@ColumnInfo(defaultValue = "...")`.

**Why Room recreates `tasks`:** SQLite cannot add a foreign key to an existing table. Look at `shared/build/generated/ksp/jvm/.../AppDatabase_AutoMigration_1_2_Impl.kt` to see the SQL that Room generated.

## Step 3 – Tags (many-to-many, automated migration 2 → 3)

New tables `tags` and the junction table `task_tags`, with a composite PK and `CASCADE` on both foreign keys.

```kotlin
@Entity(
    tableName = "task_tags",
    primaryKeys = ["task_id", "tag_id"],
    indices = [Index(value = ["tag_id"])],
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["task_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tag_id"], onDelete = ForeignKey.CASCADE),
    ],
)
data class TaskTagCrossRef(@ColumnInfo(name = "task_id") val taskId: Long, @ColumnInfo(name = "tag_id") val tagId: Long)

// "task with tags": column names in the junction differ from the parent/child -> set them in Junction
data class TaskDetails(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumns = ["id"], entityColumns = ["id"],
        associateBy = Junction(TaskTagCrossRef::class, parentColumns = ["task_id"], entityColumns = ["tag_id"]),
    )
    val tags: List<TagEntity>,
)

// TaskDao – "tasks by tag" (JOIN through the junction) and "add tags to a task" (atomic)
@Transaction
@Query("""SELECT tasks.* FROM tasks INNER JOIN task_tags ON tasks.id = task_tags.task_id
          WHERE task_tags.tag_id = :tagId AND (:done IS NULL OR tasks.is_done = :done)
          ORDER BY tasks.created_at DESC""")
fun observeByTag(tagId: Long, done: Boolean?): Flow<List<TaskDetails>>

@Transaction
suspend fun setTags(taskId: Long, tagIds: Collection<Long>) {
    removeAllTags(taskId)
    addTags(tagIds.map { TaskTagCrossRef(taskId, it) })
}
```

`TaskDao` list queries now return `TaskDetails`, so they need `@Transaction`. In `AppDatabase`, set `version = 3` and add `AutoMigration(from = 2, to = 3)`.

**Check:** old tasks have no tags. In the task dialog, type a tag name and press "Add", then save. Filter the list by tag. "Delete tag" removes the tag from all tasks through the `CASCADE`.

## Step 4 – Rename `title` → `name` (automated migration + `AutoMigrationSpec`, 3 → 4)

This step also renames `description` → `notes`, because step 5 talks about `notes`.

```kotlin
// data/local/Migrations.kt
@RenameColumn(tableName = "tasks", fromColumnName = "title", toColumnName = "name")
@RenameColumn(tableName = "tasks", fromColumnName = "description", toColumnName = "notes")
class Migration3To4 : AutoMigrationSpec

// TaskEntity:  val name: String,  val notes: String? = null
// AppDatabase: version = 4, AutoMigration(from = 3, to = 4, spec = Migration3To4::class)
```

Try this first: bump the version and add `AutoMigration(3, 4)` **without** the spec. The build fails, and Room asks for an `AutoMigrationSpec`. Without it, a rename looks like "drop column + add column", which would lose data.

Only the entity, the mapper and the repository change. The UI model `Task` keeps `title`/`description`, which shows why entities are mapped to separate models.

`migrate3To4_renamesColumnsAndKeepsRelations` checks that the links in `task_tags` survive. Room recreates the `tasks` table during this migration, but it runs migrations with foreign-key enforcement turned off, so the `CASCADE` does not delete the children.

## Step 5 – Move notes to `task_notes` (one-to-one, manual migration 4 → 5)

Room cannot work out that data must be *copied* from one table to another, so you write this migration by hand.

```kotlin
// One-to-one: the PK is also the FK, so there is at most one note per task
@Entity(
    tableName = "task_notes",
    foreignKeys = [ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["task_id"], onDelete = ForeignKey.CASCADE)],
)
data class TaskNoteEntity(@PrimaryKey @ColumnInfo(name = "task_id") val taskId: Long, val content: String)

// TaskDetails gets:  @Relation(parentColumns = ["id"], entityColumns = ["task_id"]) val note: TaskNoteEntity?
// TaskEntity loses `notes`

val MIGRATION_4_5 = object : Migration(4, 5) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // CREATE TABLE copied from schemas/.../5.json ("createSql")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `task_notes` (`task_id` INTEGER NOT NULL, `content` TEXT NOT NULL, " +
                "PRIMARY KEY(`task_id`), FOREIGN KEY(`task_id`) REFERENCES `tasks`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        connection.execSQL(
            "INSERT INTO `task_notes` (`task_id`, `content`) " +
                "SELECT `id`, `notes` FROM `tasks` WHERE `notes` IS NOT NULL"
        )
        connection.execSQL("ALTER TABLE `tasks` DROP COLUMN `notes`")   // SQLite 3.35+, bundled driver
    }
}

// AppDatabase: version = 5 (no AutoMigration for 4 -> 5), and register the manual migration:
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .addMigrations(MIGRATION_4_5)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
```

**How to get the exact SQL:** change the entities, bump the version, and build once. Room writes `5.json`; copy `createSql` from it. While `MIGRATION_4_5` does not exist yet, the build still exports the schema, but the app fails at runtime.

The patch adds three tests:
- `migrate4To5_movesNotesToSeparateTable`
- `migrateAll_from1ToLatest`, which runs the whole chain and catches gaps in it.
- `appConfiguration_opensVersion1Database`, which opens an old file exactly as the app does. Try removing `.addMigrations(MIGRATION_4_5)`: this test fails with `A migration from 1 to 5 was required but not found`.

**Check:** the descriptions of your old tasks are still shown, but they are now stored in `task_notes`. Open the database in DB Browser for SQLite and compare.
