# Before you push this

Fifteen minutes of work, in order.

1. **Claim the namespace.** `io.github.crazyvibes` works if that's your GitHub username.
   If not, find-and-replace it across every file — it appears in the package path, the
   `namespace`, the `group`, the API dump and the README.

   ```bash
   grep -rl 'crazyvibes' . | xargs sed -i 's/crazyvibes/YOUR_USERNAME/g'
   # then rename the directories under src/main/kotlin and src/test/kotlin
   ```

2. **Add the Gradle wrapper.** Not committed here.

   ```bash
   gradle wrapper --gradle-version 8.9
   git add gradle/wrapper gradlew gradlew.bat
   ```

3. **Regenerate the API dump.** The committed one is written by hand and will not match
   byte-for-byte. Overwrite it with the real thing, and check the diff — reading your own
   API as a flat list of JVM signatures is the fastest way to spot something that should
   have been internal.

   ```bash
   ./gradlew apiDump
   ./gradlew apiCheck   # must pass before you push
   ```

4. **Run the tests.** `./gradlew testDebugUnitTest`

5. **Replace LICENSE** with the full Apache 2.0 text from apache.org.

6. **First commit and tag.**

   ```bash
   git init && git add -A
   git commit -m "BackgroundAudit 0.1.0 — report why background work is killed on this device"
   git tag v0.1.0
   git push -u origin main --tags
   ```

7. **Repo settings on GitHub.** Description: *"Reports at runtime why Android background
   work is likely to be killed on this device — Doze, App Standby, vendor autostart
   managers."* Topics: `android`, `kotlin`, `background-execution`, `doze`,
   `battery-optimization`, `android-sdk`, `location`. Enable Issues. Enable Discussions.

8. **Publishing to Maven Central is optional** and takes a few days of Sonatype
   paperwork. The repo does its job without it — do it later if the project gets traction.

## What to do with it once it's up

Add it to your LinkedIn Featured section and the Projects section of your resume. Link it
in every cold email. Then write the blog post (`week3-blog-post.md`), which is the
distribution channel — the repo is the proof, the post is what gets people to it.
