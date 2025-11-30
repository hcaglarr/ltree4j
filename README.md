## Developer Overview

`ltree4j` is a small, focused library that makes PostgreSQL `ltree` usable in **Java/Spring** applications without manually
juggling `String` paths or JDBC details.

- Framework-agnostic: works with Jakarta EE, or Spring Boot as long as you use PostgreSQL and JPA/JDBC.

### When to Use

Use this module if:

- You store hierarchical data in PostgreSQL using the `ltree` type.
- You want **type-safe** handling of paths instead of raw `String` values.
- You already use **Spring Boot + JPA** and prefer integrating `ltree` via entities and repositories.
- You are fine using **native queries** for `ltree` operators (`<@`, `@>`, `~`, …).

### What the Library Provides

- A value object: `LTreePath`
    - Holds and validates `ltree`-compatible paths.
    - Provides helper methods such as `append`, `getParent`, `isAncestorOf`, `isDescendantOf`.
- JPA integration:
    - `LTreePath` is mapped to PostgreSQL `ltree` using an `AttributeConverter`.
    - Works with both:
        - `ltree4j-jakarta` → `jakarta.persistence.*` (Spring Boot 3.x)
        - `ltree4j-javax` → `javax.persistence.*` (Spring Boot 2.x / legacy)
- JDBC integration:
    - Values are sent as native `ltree` types (via `PGobject`), avoiding type mismatch issues.

### Typical Integration Steps

1. **Enable `ltree` in PostgreSQL**

   ```sql
   CREATE EXTENSION IF NOT EXISTS "ltree";

2. Add the Dependency

For Spring Boot 3.x:

 ```xml

<dependency>
    <groupId>com.hcaglar</groupId>
    <artifactId>ltree4j-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For Spring Boot 2.x:

 ```xml

<dependency>
    <groupId>com.hcaglar</groupId>
    <artifactId>ltree4j-javax</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

3. Use LTreePath in Your Entity

 ```java
import com.hcaglar.ltree4j.LTreePath;
import jakarta.persistence.*; // or javax.persistence.* for Boot 2

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_path", columnList = "path", unique = true)})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "path", columnDefinition = "ltree", nullable = false)
    private LTreePath path;

    // getters/setters omitted for brevity
}
 ```

4. Build Paths in Code

 ```java
LTreePath root = LTreePath.of("categories");
LTreePath fullPath = root
        .append("electronics")
        .append("smartphones");
```

5. Use Native Queries for Hierarchy Operations

```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(
        value = "SELECT * FROM categories WHERE path <@ CAST(:pathStr AS ltree)",
        nativeQuery = true
    )
    List<Category> findAllDescendants(@Param("pathStr") String pathStr);

    @Query(
        value = "SELECT * FROM categories WHERE path @> CAST(:pathStr AS ltree)",
        nativeQuery = true
    )
    List<Category> findAllAncestors(@Param("pathStr") String pathStr);
}
```
## 📚 API Reference (`LTreePath`)

The `LTreePath` class is the core of this library and represents a single PostgreSQL `ltree` path value.

| Method                  | Description                                   | Example (Context)                                      | Result                          |
|-------------------------|-----------------------------------------------|--------------------------------------------------------|---------------------------------|
| `of(String)`            | Static factory. Validates and creates path.   | `LTreePath.of("A.B")`                                  | `LTreePath` for `"A.B"`         |
| `append(String)`        | Adds a child node securely.                   | `LTreePath.of("A").append("B")`                        | `LTreePath` for `"A.B"`         |
| `getParent()`           | Returns the parent path or `null` if root.    | `LTreePath.of("A.B").getParent()`                      | `LTreePath` for `"A"`           |
| `isAncestorOf(other)`   | Checks if this path contains the other.       | `LTreePath.of("A").isAncestorOf(LTreePath.of("A.B"))`  | `true`                          |
| `isDescendantOf(other)` | Checks if this path is inside the other.      | `LTreePath.of("A.B").isDescendantOf(LTreePath.of("A"))`| `true`                          |
| `getValue()`            | Returns the raw `String` value.               | `LTreePath.of("A.B").getValue()`                       | `"A.B"`                         |

In short, typical usage:

- To create a new path: `LTreePath.of("root.child.leaf")`
- To add a child level: `path.append("newNode")`
- To navigate to the parent node: `path.getParent()`
- To check relationships: `parent.isAncestorOf(child)` / `child.isDescendantOf(parent)`
- To get the raw value for DB/logging: `path.getValue()`



### ⚠️ Exceptions

`LTreePath` kullanımında yakalanabilecek temel exception:

- **`InvalidLTreePathException`**
    - `LTreePath.of(String)` çağrısında, `null`, boş veya `ltree` regexine uymayan bir değer verildiğinde fırlatılır.
    - `append(String childNode)` çağrısında, `null`, nokta (`.`) içeren veya node pattern’ine (`^[A-Za-z0-9_]+$`) uymayan bir child adı verildiğinde fırlatılır.

Notes and Limitations

- LTreePath is intentionally strict: paths must match PostgreSQL ltree rules (ASCII letters, digits, underscores, and dots as separators).

- Advanced ltree operators are not abstracted; you still write native SQL for those, but you can keep path logic in Java.

- The library focuses on being predictable and type-safe rather than hiding database-specific features.